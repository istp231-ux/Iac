package kr.or.nia.dpg.vpos.service.api.impl;

import kr.or.nia.dpg.vpos.entity.api.ApiResponse;
import kr.or.nia.dpg.vpos.entity.api.PersonalVO;
import kr.or.nia.dpg.vpos.enums.exception.VpsExceptionType;
import kr.or.nia.dpg.vpos.exception.VpsException;
import kr.or.nia.dpg.vpos.service.api.CertificateApiService;
import kr.or.nia.dpg.vpos.web.api.certificate.RetrofitCertifiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

/**
 * 인증서 API 서비스 구현체.
 *
 * <p>외부 인증서 API(공동인증, 휴대전화, 아이핀, 원패스)를 Retrofit으로 호출하고,
 * 통신 오류를 5종(SocketTimeout, Connect, UnknownHost, SSL, IOException)으로 분류하여
 * 우리 서버/외부 서버/네트워크 원인을 서버 로그에 명확히 기록한다.</p>
 *
 * <p>certificateSearch, phoneSearch는 현재 TODO(더미 데이터)로 구현됨.
 * 실제 API 연동 시 주석 해제하여 executeApiCall()로 교체 필요.</p>
 */
// TODO 추후 삭제 필요
@Slf4j
@Service
public class CertificateApiServiceImpl implements CertificateApiService {

	/** 로그에 표시되는 API 식별 명칭 */
	private static final String API_NAME = "인증서API(certifi)";

	private final RetrofitCertifiService retrofitCertifiService;

    public CertificateApiServiceImpl(RetrofitCertifiService retrofitCertifiService) {
        this.retrofitCertifiService = retrofitCertifiService;
    }

	@Override
	public List<PersonalVO> certificateSearch(String personalNumber){
		/*
        Call<ApiResponse<List<PersonalVO>>> call = retrofitCertifiService.searchPerson();
        return executeApiCall(call, "certificateSearch", "/IF-VPS-001");
		*/

		List<PersonalVO> list = new ArrayList<>();
		PersonalVO vo = new PersonalVO();
		vo.setCI("testToken12345");
		vo.setPersonalNumber(personalNumber);
		vo.setPersonalName("홍길동");

		list.add(vo);
		return list;
	}

	@Override
	public List<PersonalVO> phoneSearch(String personalNumber) {
		/*
        Call<ApiResponse<List<PersonalVO>>> call = retrofitCertifiService.searchPersonMobile();
        return executeApiCall(call, "phoneSearch", "/IF-VPS-002");
		*/
		List<PersonalVO> list = new ArrayList<>();
		PersonalVO vo = new PersonalVO();
		vo.setVctmAplySn(1);
		vo.setCI("testToken12345");
		vo.setDI("testToken12345");
		vo.setPersonalNumber(personalNumber);
		vo.setPersonalName("홍길동");
		vo.setPersonalPhoneNumber("01012345678");

		list.add(vo);
		return list;
	}

	@Override
	public List<PersonalVO> pbaSearch(String personalNumber){
        Call<ApiResponse<List<PersonalVO>>> call = retrofitCertifiService.searchPersonMobile();
        return executeApiCall(call, "pbaSearch", "/IF-VPS-002");
	}

	@Override
	public List<PersonalVO> onepassSearch(String personalNumber){
        Call<ApiResponse<List<PersonalVO>>> call = retrofitCertifiService.searchPersonMobile();
        return executeApiCall(call, "onepassSearch", "/IF-VPS-002");
	}

	@Override
	public String updateExposureText(String exposureText, String vctmAplySn) {
		return exposureText;
	}

	/**
	 * Retrofit API 호출 공통 메서드.
	 * 통신 오류를 종류별로 분류하여 우리 서버/외부 서버/네트워크 원인을 서버 로그에 남긴다.
	 */
	private List<PersonalVO> executeApiCall(
			Call<ApiResponse<List<PersonalVO>>> call, String methodName, String apiPath) {

		log.info("[{}] 외부 API 호출 시작 - api={}, path={}", methodName, API_NAME, apiPath);
		long startTime = System.currentTimeMillis();

		try {
			Response<ApiResponse<List<PersonalVO>>> response = call.execute();
			long elapsed = System.currentTimeMillis() - startTime;
			log.info("[{}] 외부 API 응답 수신 - api={}, path={}, httpStatus={}, elapsed={}ms",
					methodName, API_NAME, apiPath, response.code(), elapsed);

			return handleApiResponse(response, methodName, apiPath);

		} catch (SocketTimeoutException e) {
			long elapsed = System.currentTimeMillis() - startTime;
			log.error("[{}] [원인:외부서버] API 응답 시간 초과 - api={}, path={}, elapsed={}ms, error={}",
					methodName, API_NAME, apiPath, elapsed, e.getMessage());
			throw new VpsException(VpsExceptionType.API_TIMEOUT,
					String.format("path=%s, elapsed=%dms", apiPath, elapsed), API_NAME);

		} catch (ConnectException e) {
			long elapsed = System.currentTimeMillis() - startTime;
			log.error("[{}] [원인:외부서버] API 서버 연결 실패(서버 다운 또는 포트 미오픈) - api={}, path={}, elapsed={}ms, error={}",
					methodName, API_NAME, apiPath, elapsed, e.getMessage());
			throw new VpsException(VpsExceptionType.API_CONNECTION_REFUSED,
					String.format("path=%s, error=%s", apiPath, e.getMessage()), API_NAME);

		} catch (UnknownHostException e) {
			log.error("[{}] [원인:네트워크] DNS 조회 실패(호스트명 확인 필요) - api={}, path={}, host={}",
					methodName, API_NAME, apiPath, e.getMessage());
			throw new VpsException(VpsExceptionType.API_DNS_ERROR,
					String.format("path=%s, host=%s", apiPath, e.getMessage()), API_NAME);

		} catch (SSLException e) {
			log.error("[{}] [원인:외부서버] SSL/TLS 통신 오류(인증서 만료/불일치 확인 필요) - api={}, path={}, error={}",
					methodName, API_NAME, apiPath, e.getMessage(), e);
			throw new VpsException(VpsExceptionType.API_SSL_ERROR,
					String.format("path=%s, error=%s", apiPath, e.getMessage()), API_NAME);

		} catch (IOException e) {
			long elapsed = System.currentTimeMillis() - startTime;
			log.error("[{}] [원인:불명] API 호출 중 IO 오류 - api={}, path={}, elapsed={}ms, exType={}, error={}",
					methodName, API_NAME, apiPath, elapsed, e.getClass().getSimpleName(), e.getMessage(), e);
			throw new VpsException(VpsExceptionType.API_SERVER_ERROR,
					String.format("path=%s, ioType=%s, error=%s", apiPath, e.getClass().getSimpleName(), e.getMessage()), API_NAME);
		}
	}

	/**
	 * API 응답 공통 처리.
	 * HTTP 상태코드와 응답 결과코드(resultCode)를 분리하여 어디서 에러가 발생했는지 명확히 로깅한다.
	 * <ul>
	 *   <li>HTTP 4xx/5xx → [원인:외부서버] API HTTP 오류 응답</li>
	 *   <li>body == null → [원인:외부서버] API 응답 본문 파싱 결과 null</li>
	 *   <li>resultCode != "000" → [원인:외부서버] API 응답 결과코드 실패</li>
	 * </ul>
	 */
	private List<PersonalVO> handleApiResponse(
			Response<ApiResponse<List<PersonalVO>>> response, String methodName, String apiPath) {

		if (response == null) {
			log.error("[{}] [원인:내부서버] API 응답 객체가 null - api={}, path={}", methodName, API_NAME, apiPath);
			throw new VpsException(VpsExceptionType.API_EMPTY_RESPONSE,
					"response=null, path=" + apiPath, API_NAME);
		}

		if (!response.isSuccessful()) {
			String errorBody = null;
			try {
				if (response.errorBody() != null) {
					errorBody = response.errorBody().string();
				}
			} catch (IOException ignored) {
			}
			log.error("[{}] [원인:외부서버] API HTTP 오류 응답 - api={}, path={}, httpStatus={}, errorBody={}",
					methodName, API_NAME, apiPath, response.code(), errorBody);
			throw new VpsException(VpsExceptionType.API_SERVER_ERROR,
					String.format("path=%s, httpStatus=%d, errorBody=%s", apiPath, response.code(), errorBody), API_NAME);
		}

		ApiResponse<List<PersonalVO>> res = response.body();
		if (res == null) {
			log.error("[{}] [원인:외부서버] API 응답 본문 파싱 결과 null(응답 형식 불일치) - api={}, path={}, httpStatus={}",
					methodName, API_NAME, apiPath, response.code());
			throw new VpsException(VpsExceptionType.API_EMPTY_RESPONSE,
					"body=null, path=" + apiPath, API_NAME);
		}

		if ("000".equals(res.getResultCode())) {
			log.info("[{}] 외부 API 처리 성공 - api={}, path={}", methodName, API_NAME, apiPath);
			List<PersonalVO> result = res.getResult();
			return result != null ? result : new ArrayList<>();
		} else {
			log.warn("[{}] [원인:외부서버] API 응답 결과코드 실패 - api={}, path={}, resultCode={}, resultMsg={}",
					methodName, API_NAME, apiPath, res.getResultCode(), res.getResultMsg());
			throw new VpsException(VpsExceptionType.API_RESULT_CODE_ERROR,
					String.format("path=%s, resultCode=%s, resultMsg=%s",
							apiPath, res.getResultCode(), res.getResultMsg()), API_NAME);
		}
	}

}
