package kr.or.nia.dpg.vpos.mapper;

import kr.or.nia.dpg.vpos.entity.api.*;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TempMapper {
	List<AccountVO> accountSearch(PersonalVO personalVO);
	List<LoanVO> loanSearch(PersonalVO personalVO);

	void accountUpdate(AccountVO accountVO);
	void loanUpdate(LoanVO loanVO);

	List<MobileVO> mobileSearch(PersonalVO personalVO);
	List<PaymentVO> paymentSearch(PersonalVO personalVO);
	void mobileUpdate(MobileVO mobileVO);

	// 인증로그인 정보
	PersonalVO searchPersonal(String personalNumber);
	String searchVctmAplySn(String CI);
	void updatePersonal(PersonalVO personalVO);
	void insertPersonal(PersonalVO personalVO);
	// 개인정보 노출자 사고예방시스템 등록
	void insertExposureText(String exposureText, String vctmAplySn);


}
