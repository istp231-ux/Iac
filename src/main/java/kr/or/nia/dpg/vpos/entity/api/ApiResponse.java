package kr.or.nia.dpg.vpos.entity.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
