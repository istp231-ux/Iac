package kr.or.nia.dpg.vpos.service.api.impl;

import kr.or.nia.dpg.vpos.entity.api.PersonalVO;
import kr.or.nia.dpg.vpos.service.api.CertificateApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 인증서 API 서비스 구현체.
 *
 * <p>현재 모든 메서드가 더미 데이터를 반환한다.
 * 실제 외부 API 연동 시 Retrofit 설정(RetrofitConfig) 및
 * executeApiCall() 공통 메서드를 복원하여 교체 필요.</p>
 */
// TODO 추후 삭제 필요
@Slf4j
@Service
public class CertificateApiServiceImpl implements CertificateApiService {

	@Override
	public List<PersonalVO> certificateSearch(String personalNumber){
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
	public List<PersonalVO> onepassSearch(String personalNumber){
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
	public String updateExposureText(String exposureText, String vctmAplySn) {
		return exposureText;
	}

}
