package kr.or.nia.dpg.vpos.mapper;

import org.apache.ibatis.annotations.Mapper;

import kr.or.nia.dpg.vpos.entity.api.common.ApiLogParsedVO;
import kr.or.nia.dpg.vpos.entity.api.common.ApiLogRawVO;

/** 외부 API 응답 로그(Raw/Parsed) DB 저장 매퍼. */
@Mapper
public interface ApiResponseLogMapper {

	void insertRawLog(ApiLogRawVO vo);

	void insertParsedLog(ApiLogParsedVO vo);
}
