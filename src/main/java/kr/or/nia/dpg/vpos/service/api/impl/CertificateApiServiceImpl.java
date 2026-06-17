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
import java.util.ArrayList;
import java.util.List;

// TODO 추후 삭제 필요
@Slf4j
@Service
public class CertificateApiServiceImpl implements CertificateApiService {


	private final RetrofitCertifiService retrofitCertifiService;


    public CertificateApiServiceImpl(RetrofitCertifiService retrofitCertifiService) {
        this.retrofitCertifiService = retrofitCertifiService;
    }

	/**
	 * 공동인증 API호출
	 */
	@Override
	public List<PersonalVO> certificateSearch(String personalNumber){
		/*
        Call<ApiResponse<List<PersonalVO>>> call = retrofitCertifiService.searchPerson();
        try {
        	Response<ApiResponse<List<PersonalVO>>> response = call.execute();
        	return handleApiResponse(response, "certificateSearch");
        } catch (SocketTimeoutException e) {
            log.error("[certificateSearch] API 응답 시간 초과", e);
            throw new VpsException(VpsExceptionType.API_TIMEOUT);
        } catch (ConnectException e) {
            log.error("[certificateSearch] API 서버 연결 실패", e);
            throw new VpsException(VpsExceptionType.API_CONNECTION_REFUSED);
        } catch (IOException e) {
            log.error("[certificateSearch] API 호출 중 IO 오류 발생", e);
            throw new VpsException(VpsExceptionType.API_SERVER_ERROR);
        }
		*/

		List<PersonalVO> list = new ArrayList<>();
		PersonalVO vo = new PersonalVO();
		vo.setCI("testToken12345");
		vo.setPersonalNumber(personalNumber);
		vo.setPersonalName("홍길동");

		list.add(vo);
		return list;
	}


	/**
	 * 휴대폰인증 API호출
	 */
	@Override
	public List<PersonalVO> phoneSearch(String personalNumber) {
		/*
        Call<ApiResponse<List<PersonalVO>>> call = retrofitCertifiService.searchPersonMobile();
        try {
        	Response<ApiResponse<List<PersonalVO>>> response = call.execute();
        	return handleApiResponse(response, "phoneSearch");
        } catch (SocketTimeoutException e) {
            log.error("[phoneSearch] API 응답 시간 초과", e);
            throw new VpsException(VpsExceptionType.API_TIMEOUT);
        } catch (ConnectException e) {
            log.error("[phoneSearch] API 서버 연결 실패", e);
            throw new VpsException(VpsExceptionType.API_CONNECTION_REFUSED);
        } catch (IOException e) {
            log.error("[phoneSearch] API 호출 중 IO 오류 발생", e);
            throw new VpsException(VpsExceptionType.API_SERVER_ERROR);
        }
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

	/**
	 * 아이핀 API호출
	 */
	@Override
	public List<PersonalVO> pbaSearch(String personalNumber){
        Call<ApiResponse<List<PersonalVO>>> call = retrofitCertifiService.searchPersonMobile();
        try {
        	Response<ApiResponse<List<PersonalVO>>> response = call.execute();
        	return handleApiResponse(response, "pbaSearch");
        } catch (SocketTimeoutException e) {
            log.error("[pbaSearch] API 응답 시간 초과", e);
            throw new VpsException(VpsExceptionType.API_TIMEOUT);
        } catch (ConnectException e) {
            log.error("[pbaSearch] API 서버 연결 실패", e);
            throw new VpsException(VpsExceptionType.API_CONNECTION_REFUSED);
        } catch (IOException e) {
            log.error("[pbaSearch] API 호출 중 IO 오류 발생", e);
            throw new VpsException(VpsExceptionType.API_SERVER_ERROR);
        }
	}

	/**
	 * 원패스 API호출
	 */
	@Override
	public List<PersonalVO> onepassSearch(String personalNumber){
        Call<ApiResponse<List<PersonalVO>>> call = retrofitCertifiService.searchPersonMobile();
        try {
        	Response<ApiResponse<List<PersonalVO>>> response = call.execute();
        	return handleApiResponse(response, "onepassSearch");
        } catch (SocketTimeoutException e) {
            log.error("[onepassSearch] API 응답 시간 초과", e);
            throw new VpsException(VpsExceptionType.API_TIMEOUT);
        } catch (ConnectException e) {
            log.error("[onepassSearch] API 서버 연결 실패", e);
            throw new VpsException(VpsExceptionType.API_CONNECTION_REFUSED);
        } catch (IOException e) {
            log.error("[onepassSearch] API 호출 중 IO 오류 발생", e);
            throw new VpsException(VpsExceptionType.API_SERVER_ERROR);
        }
	}

	/**
	 * 개인정보 노출자 사고예방시스템 등록
	 */
	@Override
	public String updateExposureText(String exposureText, String vctmAplySn) {
		return exposureText;
	}

	/**
	 * API 응답 공통 처리
	 */
	private List<PersonalVO> handleApiResponse(Response<ApiResponse<List<PersonalVO>>> response, String methodName) {
		if (response == null || !response.isSuccessful()) {
			log.error("[{}] API 응답 실패 - HTTP 상태: {}", methodName,
					response != null ? response.code() : "null");
			throw new VpsException(VpsExceptionType.API_SERVER_ERROR);
		}

		ApiResponse<List<PersonalVO>> res = response.body();
		if (res == null) {
			log.error("[{}] API 응답 본문이 null입니다.", methodName);
			throw new VpsException(VpsExceptionType.API_EMPTY_RESPONSE);
		}

		if ("000".equals(res.getResultCode())) {
			log.info("[{}] API 호출 성공", methodName);
			List<PersonalVO> result = res.getResult();
			// 호출부의 NPE 방지를 위해 result가 null이면 빈 목록을 반환한다.
			return result != null ? result : new ArrayList<>();
		} else {
			log.warn("[{}] API 응답 실패 - 응답코드: {}, 응답메시지: {}",
					methodName, res.getResultCode(), res.getResultMsg());
			throw new VpsException(VpsExceptionType.API_SERVER_ERROR, res.getResultMsg());
		}
	}

}
