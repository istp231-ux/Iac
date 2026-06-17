package kr.or.nia.dpg.vpos.entity.api.common;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
