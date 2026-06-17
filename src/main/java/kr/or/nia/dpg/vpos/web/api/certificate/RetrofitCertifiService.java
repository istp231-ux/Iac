package kr.or.nia.dpg.vpos.web.api.certificate;

import kr.or.nia.dpg.vpos.entity.api.ApiResponse;
import kr.or.nia.dpg.vpos.entity.api.PersonalVO;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

// TODO 추후 삭제 필요
public interface RetrofitCertifiService {

    @GET("/IF-VPS-001")
    Call<ApiResponse<List<PersonalVO>>> searchPerson();

    @GET("/IF-VPS-002")
    Call<ApiResponse<List<PersonalVO>>> searchPersonMobile();

}
