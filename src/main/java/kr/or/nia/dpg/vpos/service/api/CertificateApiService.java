package kr.or.nia.dpg.vpos.service.api;

import kr.or.nia.dpg.vpos.entity.api.PersonalVO;

import java.util.List;

/**
 * 인증서 API 서비스 인터페이스.
 * <p>인증 방식별(공동인증, 휴대전화, 아이핀, 원패스) 외부 API 호출을 추상화한다.</p>
 */
// TODO 추후 삭제 필요
public interface CertificateApiService {

	/** 공동인증서 기반 사용자 조회 (1차 인증) */
	List<PersonalVO> certificateSearch(String personalNumber);

	/** 휴대전화 인증 기반 사용자 조회 (2차 인증) */
	List<PersonalVO> phoneSearch(String personalNumber);

	/** 아이핀 인증 기반 사용자 조회 */
	List<PersonalVO> pbaSearch(String personalNumber);

	/** 디지털원패스 인증 기반 사용자 조회 */
	List<PersonalVO> onepassSearch(String personalNumber);

	/** 개인정보 노출자 사고예방시스템 등록 */
	String updateExposureText(String exposureText, String vctmAplySn);



}
