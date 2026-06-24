package kr.or.nia.dpg.vpos.entity.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 외부 API 공통 응답 래퍼.
 *
 * <p>resultCode "000"이 성공이며, 그 외는 외부 시스템 정의 오류 코드이다.</p>
 */
// TODO 삭제 필요
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

	private String resultCode;
	private String resultMsg;
	private T result;
}
