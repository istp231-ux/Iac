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
public class ApiLogParsedVO {

	/** 기본키 (Auto_Increment) */
	private Long logSn;

	/** 요청 추적용 UUID - raw 테이블 외래키 */
	private String traceId;

	/** 요청 유형 */
	private String rqtType;

	/** 기관명 - InstitutionType */
	private String instCd;

	/** 서비스 구분 - ActionType - TOKEN_ISSUE 일 때는 null */
	private String actType;

	/** 서비스 구분 - ServiceType */
	private String srvcType;

	/** HTTP 응답 상태 코드 */
	private int httpStts;

	/** HTTP 통신 성공/실패 여부 (Y/N) */
	private String commRslt;

	/** 처리 결과 코드 - 각 기관에서 정의한 코드 */
	private String rsltCd;

	/** 처리 결과 메시지 - 각 기관에서 전달한 메시지 */
	private String rsltMsg;

	/** 발생한 에러 메시지 소스 식별 - 허브(H) or 기관(I) */
	private String errorSource;

	/** 로그 생성 일시 (DB에서 NOW()로 자동 입력) */
	private LocalDateTime createdDt;
}
