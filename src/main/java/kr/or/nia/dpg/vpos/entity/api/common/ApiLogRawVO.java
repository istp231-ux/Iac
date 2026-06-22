package kr.or.nia.dpg.vpos.entity.api.common;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 외부 API 응답 원본(암호화된) 로그 VO. traceId로 Parsed 로그와 연결된다. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiLogRawVO {

	private Long logSn;
	private String traceId;
	private String rawRspns;
	private LocalDateTime createdDt;
}
