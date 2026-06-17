package kr.or.nia.dpg.vpos.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OkHttp Interceptor에서 캡처한 traceId, httpStts를 Service 레이어까지 전달하기 위한 ThreadLocal
 *
 *  - Raw 응답은 Interceptor에서 바로 DB에 저장
 *  - 요청이 끝나면 반드시 clear() 호출 필요 (메모리 누수 방지)
 */
public class ApiLogContext {

	private ApiLogContext() {}

	private static final ThreadLocal<LogData> CONTEXT = new ThreadLocal<>();

	public static void set(LogData data) {
		CONTEXT.set(data);
	}

	public static LogData get() {
		return CONTEXT.get();
	}

	/**
	 * ThreadLocal 데이터 제거
	 * ThreadPool 재사용 시 이전 요청 데이터가 남지 않도록 반드시 호출해야함
	 */
	public static void clear() {
		CONTEXT.remove();
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LogData {
		private String traceId;
		private int httpStts;
	}

}
