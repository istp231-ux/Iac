package kr.or.nia.dpg.vpos.interceptor.retroifit.okhttp;

import java.io.IOException;
import java.util.UUID;

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

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiLogInterceptor implements Interceptor {

	private final ApiResponseLogMapper apiLogMapper;
	private final ARIACryptor ariaCryptor;

	@NotNull
	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();
		String traceId = UUID.randomUUID().toString();

		try {
			Response response = chain.proceed(request);

			String rawResponse = "";
			ResponseBody body = response.peekBody(1024L * 1024);
			if (body != null) {
				rawResponse = body.string();
			}
			log.info("[ApiLogInterceptor] 응답 수신 - traceId={}, httpStts={}, url={}",
					traceId, response.code(), request.url());

			saveRawLog(traceId, rawResponse);
			ApiLogContext.set(new ApiLogContext.LogData(traceId, response.code()));

			return response;

		} catch (IOException e) {
			log.error("[ApiLogInterceptor] API 호출 중 IO 오류 - traceId={}, url={}", traceId, request.url(), e);
			saveRawLog(traceId, "IOException: " + e.getMessage());
			ApiLogContext.set(new ApiLogContext.LogData(traceId, 0));
			throw e;
		}
	}

	private void saveRawLog(String traceId, String rawResponse) {
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
