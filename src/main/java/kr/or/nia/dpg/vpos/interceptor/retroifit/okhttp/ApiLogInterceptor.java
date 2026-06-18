package kr.or.nia.dpg.vpos.interceptor.retroifit.okhttp;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.UUID;

import javax.net.ssl.SSLException;
import javax.validation.constraints.NotNull;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import kr.or.nia.dpg.vpos.common.ApiLogContext;
import kr.or.nia.dpg.vpos.entity.api.common.ApiLogRawVO;
import kr.or.nia.dpg.vpos.mapper.ApiResponseLogMapper;
import kr.or.nia.dpg.vpos.util.crypto.aria.ARIACryptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * OkHttp Interceptor - API 응답 원본을 캡처해서 암호화 후 DB에 저장하고,
 * traceId/httpStts를 {@link ApiLogContext}(ThreadLocal)에 보관한다.
 *
 * <p>설계 원칙</p>
 * <ul>
 *   <li>OkHttp 계약상 {@code intercept()}는 절대 null을 반환하면 안 된다(반환하면 호출 체인에서 NPE).
 *       통신 실패는 {@code IOException}을 그대로 전파한다.</li>
 *   <li>로깅/저장 실패가 실제 API 호출을 깨뜨려서는 안 된다. 응답을 정상 수신한 뒤의
 *       본문 읽기·암호화·DB 저장 단계의 예외는 모두 흡수하고 원본 응답을 반환한다.</li>
 *   <li>ThreadLocal({@link ApiLogContext})은 요청 종료 시 반드시 clear() 되어야 한다
 *       (ApiLogContextFilter에서 처리).</li>
 * </ul>
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiLogInterceptor implements Interceptor {

	/** peekBody로 읽어들일 응답 본문 최대 크기 (1MB) */
	private static final long MAX_PEEK_BYTES = 1024L * 1024;

	private final ApiResponseLogMapper apiLogMapper;
	private final ARIACryptor ariaCryptor;

	@NotNull
	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();
		String traceId = UUID.randomUUID().toString();
		long startTime = System.currentTimeMillis();

		Response response;
		try {
			response = chain.proceed(request);
		} catch (SocketTimeoutException e) {
			long elapsed = System.currentTimeMillis() - startTime;
			log.error("[ApiLogInterceptor] [원인:외부서버] API 응답 시간 초과 - traceId={}, url={}, elapsed={}ms",
					traceId, request.url(), elapsed);
			safeSaveRawLog(traceId, "SocketTimeoutException: " + e.getMessage());
			ApiLogContext.set(new ApiLogContext.LogData(traceId, 0));
			throw e;
		} catch (ConnectException e) {
			long elapsed = System.currentTimeMillis() - startTime;
			log.error("[ApiLogInterceptor] [원인:외부서버] API 서버 연결 실패 - traceId={}, url={}, elapsed={}ms, error={}",
					traceId, request.url(), elapsed, e.getMessage());
			safeSaveRawLog(traceId, "ConnectException: " + e.getMessage());
			ApiLogContext.set(new ApiLogContext.LogData(traceId, 0));
			throw e;
		} catch (UnknownHostException e) {
			log.error("[ApiLogInterceptor] [원인:네트워크] DNS 조회 실패 - traceId={}, url={}, host={}",
					traceId, request.url(), e.getMessage());
			safeSaveRawLog(traceId, "UnknownHostException: " + e.getMessage());
			ApiLogContext.set(new ApiLogContext.LogData(traceId, 0));
			throw e;
		} catch (SSLException e) {
			log.error("[ApiLogInterceptor] [원인:외부서버] SSL 통신 오류 - traceId={}, url={}, error={}",
					traceId, request.url(), e.getMessage(), e);
			safeSaveRawLog(traceId, "SSLException: " + e.getMessage());
			ApiLogContext.set(new ApiLogContext.LogData(traceId, 0));
			throw e;
		} catch (IOException e) {
			long elapsed = System.currentTimeMillis() - startTime;
			log.error("[ApiLogInterceptor] [원인:불명] API 호출 IO 오류 - traceId={}, url={}, elapsed={}ms, exType={}, error={}",
					traceId, request.url(), elapsed, e.getClass().getSimpleName(), e.getMessage(), e);
			safeSaveRawLog(traceId, e.getClass().getSimpleName() + ": " + e.getMessage());
			ApiLogContext.set(new ApiLogContext.LogData(traceId, 0));
			throw e;
		}

		// 응답 로깅은 어떤 경우에도 본 호출(인증 흐름)을 깨뜨려서는 안 된다 -> 예외 전부 흡수.
		try {
			String rawResponse = readBodySafely(response);
			log.info("[ApiLogInterceptor] 응답 수신 - traceId={}, httpStts={}, url={}",
					traceId, response.code(), request.url());

			safeSaveRawLog(traceId, rawResponse);
			ApiLogContext.set(new ApiLogContext.LogData(traceId, response.code()));
		} catch (Exception e) {
			// 본 호출에는 영향 없음. 최소한 상태 코드라도 컨텍스트에 남긴다.
			log.error("[ApiLogInterceptor] 응답 로깅 처리 중 오류(API 호출에는 영향 없음) - traceId={}", traceId, e);
			ApiLogContext.set(new ApiLogContext.LogData(traceId, response.code()));
		}

		return response;
	}

	/**
	 * 원본 스트림을 소비하지 않도록 peekBody로 응답 본문을 복사해 읽는다.
	 */
	private String readBodySafely(Response response) throws IOException {
		ResponseBody body = response.peekBody(MAX_PEEK_BYTES);
		return body.string();
	}

	/**
	 * Raw 응답을 암호화하여 DB에 저장한다. 저장 실패는 흡수한다(로그만 남김).
	 */
	private void safeSaveRawLog(String traceId, String rawResponse) {
		try {
			String encrypted = ariaCryptor.encrypt(rawResponse, "log");

			ApiLogRawVO rawVO = new ApiLogRawVO();
			rawVO.setTraceId(traceId);
			rawVO.setRawRspns(encrypted);

			apiLogMapper.insertRawLog(rawVO);
		} catch (DataAccessException e) {
			log.error("[ApiLogInterceptor] API 로그 DB 저장 실패 - traceId={}", traceId, e);
		} catch (Exception e) {
			log.error("[ApiLogInterceptor] API 로그 암호화/저장 중 오류 - traceId={}", traceId, e);
		}
	}

}
