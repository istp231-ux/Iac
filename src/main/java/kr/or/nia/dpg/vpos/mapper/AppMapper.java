package kr.or.nia.dpg.vpos.mapper;

import kr.or.nia.dpg.vpos.entity.api.AppVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface AppMapper {
	// TODO 불필요한 mapper 삭제 해야함
    void insert(String CI);

    void apiinsert(Map<String, Object> result);

    void errorinsert(Map<String, Object> result);

    void update(String CI);

    void mobile(String CI, String mobile);

    void loan(String CI, String loan);

    void account(String CI, String account);

    void accident(String CI, String accident);

    void payment(String CI, String payment);

    void delete(String CI);

    AppVO search(String CI);

    String findFnstNmByCd(String code);


}
