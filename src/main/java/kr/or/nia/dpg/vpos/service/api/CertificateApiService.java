package kr.or.nia.dpg.vpos.service.api;

import kr.or.nia.dpg.vpos.entity.api.PersonalVO;

import java.util.List;

// TODO 추후 삭제 필요
public interface CertificateApiService {
	// 공동인증 사용자 조회
	List<PersonalVO> certificateSearch(String personalNumber);

    // 휴대폰인증 사용자 조회
	List<PersonalVO> phoneSearch(String personalNumber);

    // 아이핀인증 사용자 조회
	List<PersonalVO> pbaSearch(String personalNumber);

    // 원패스인증 사용자 조회
	List<PersonalVO>  onepassSearch(String personalNumber);

    // 개인정보 노출자 사고예방시스템 등록
	String updateExposureText(String exposureText, String vctmAplySn);



}
