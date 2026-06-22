package kr.or.nia.dpg.vpos.web.api.certificate;

import kr.or.nia.dpg.vpos.entity.api.ApiResponse;
import kr.or.nia.dpg.vpos.entity.api.PersonalVO;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

/**
 * 인증서 외부 API Retrofit 인터페이스 정의.
 * <p>IF-VPS-001: 공동인증 사용자 조회, IF-VPS-002: 휴대전화/아이핀/원패스 사용자 조회.</p>
 */
// TODO 추후 삭제 필요
public interface RetrofitCertifiService {

    @GET("/IF-VPS-001")
    Call<ApiResponse<List<PersonalVO>>> searchPerson();

    @GET("/IF-VPS-002")
    Call<ApiResponse<List<PersonalVO>>> searchPersonMobile();

}
