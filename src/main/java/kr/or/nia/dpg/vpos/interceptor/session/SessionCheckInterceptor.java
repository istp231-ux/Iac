package kr.or.nia.dpg.vpos.interceptor.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import kr.or.nia.dpg.vpos.util.SessionUserUtil;

/**
 * 세션 인증 여부를 확인하는 인터셉터
 * <p> 본인인증이 완료되지 않은 사용자의 페이지 접근을 차단함</p>
 */
@Component
public class SessionCheckInterceptor implements HandlerInterceptor {

	private static final Logger log = LoggerFactory.getLogger(SessionCheckInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		try {
			// 브라우저 캐시 방지
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.setHeader("Pragma", "no-cache");
			response.setDateHeader("Expires", 0);

			// 인증이 완료되지 않은 사용자는 메인으로 redirect
			if (!SessionUserUtil.isCertificatedUser()) {
				log.info("[SessionCheckInterceptor] 미인증 사용자 접근 차단 - URI: {}, RemoteAddr: {}",
						request.getRequestURI(), request.getRemoteAddr());
				response.sendRedirect(request.getContextPath() + "/main");
				return false;
			}
			return true;
		} catch (Exception e) {
			log.error("[SessionCheckInterceptor] 세션 인증 확인 중 오류 발생 - URI: {}, RemoteAddr: {}, exType={}, error={}",
					request.getRequestURI(), request.getRemoteAddr(), e.getClass().getSimpleName(), e.getMessage());
			response.sendRedirect(request.getContextPath() + "/main");
			return false;
		}
	}
}
