package kr.or.nia.dpg.vpos.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import kr.or.nia.dpg.vpos.common.ApiLogContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 요청 종료 시 {@link ApiLogContext}(ThreadLocal)를 정리하는 필터.
 *
 * <p>외부 API는 {@code Call.execute()}(동기)로 호출되어 Tomcat 요청 스레드에서 실행되므로,
 * 인터셉터가 ThreadLocal에 저장한 traceId/httpStts는 같은 스레드가 다음 요청에 재사용될 때까지 남는다.
 * 이를 정리하지 않으면 이전 요청의 로그 컨텍스트가 무관한 요청에 섞여 로그 상관관계가 어긋난다.</p>
 *
 * <p>요청 처리 흐름이 끝난 직후 {@code finally}에서 clear()하여 스레드풀 재사용에 따른 누수를 방지한다.</p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiLogContextFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			filterChain.doFilter(request, response);
		} finally {
			try {
				ApiLogContext.clear();
			} catch (Exception e) {
				// clear 실패가 응답 처리를 막아서는 안 된다.
				log.warn("[ApiLogContextFilter] ApiLogContext 정리 중 오류 - exType={}, error={}", e.getClass().getSimpleName(), e.getMessage());
			}
		}
	}
}
