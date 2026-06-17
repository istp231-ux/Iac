package kr.or.nia.dpg.vpos.filter;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * 세션 쿠키 왕복 진단 필터.
 *
 * <p>"1차 인증 완료 후 다음 요청에서 1차 미완료로 조회됨" 증상의 원인을 좁히기 위해,
 * 인증 흐름 경로에 한해 다음을 한 줄씩 남긴다.</p>
 * <ul>
 *   <li>요청 진입 : 브라우저가 보낸 JSESSIONID 쿠키 값과 requestedSessionId(유효성 포함)</li>
 *   <li>응답 종료 : 서버가 이번 응답에서 내려보낸 Set-Cookie(JSESSIONID) 존재 여부</li>
 * </ul>
 *
 * <p>이 둘을 시간순으로 비교하면 원인이 명확히 갈린다.</p>
 * <ul>
 *   <li>1차 응답에 Set-Cookie 있음 + 2차 요청에 쿠키 없음 → <b>브라우저/프록시가 쿠키를 떨굼</b>
 *       (https/http 혼용, SameSite, 도메인/Path, 프록시의 Set-Cookie 제거)</li>
 *   <li>1차 응답에 Set-Cookie 자체가 없음 → <b>서버 측 세션/쿠키 설정 문제</b></li>
 *   <li>2차 요청에 쿠키는 왔으나 sessionIdValid=false → <b>해당 세션을 가진 인스턴스가 다름</b>
 *       (로드밸런서 sticky session 미설정)</li>
 * </ul>
 *
 * <p>JSESSIONID는 세션 식별자일 뿐 개인정보가 아니므로 기록한다. 정적 리소스 등 잡음을 피하기 위해
 * 인증 흐름 경로(/api/, /view/)에 대해서만 로깅한다.</p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class SessionCookieDiagnosticFilter extends OncePerRequestFilter {

	private static final String SESSION_COOKIE_NAME = "JSESSIONID";
	private static final String SET_COOKIE_HEADER = "Set-Cookie";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		boolean diagnose = isAuthFlowPath(request.getRequestURI());

		if (diagnose) {
			log.info("[세션진단:IN ] uri={}, 브라우저쿠키JSESSIONID={}, requestedSessionId={}, valid={}, fromCookie={}, remoteAddr={}",
					request.getRequestURI(),
					extractSessionCookie(request),
					request.getRequestedSessionId(),
					request.isRequestedSessionIdValid(),
					request.isRequestedSessionIdFromCookie(),
					request.getRemoteAddr());
		}

		try {
			filterChain.doFilter(request, response);
		} finally {
			if (diagnose) {
				log.info("[세션진단:OUT] uri={}, 응답SetCookie(JSESSIONID)={}",
						request.getRequestURI(), describeSetCookie(response));
			}
		}
	}

	/**
	 * 인증 흐름과 무관한 정적 리소스 등은 진단 대상에서 제외한다.
	 */
	private boolean isAuthFlowPath(String uri) {
		if (uri == null) {
			return false;
		}
		return uri.startsWith("/api/") || uri.startsWith("/view/");
	}

	/**
	 * 요청 쿠키에서 JSESSIONID 값을 추출한다. 없으면 "none".
	 */
	private String extractSessionCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return "none";
		}
		for (Cookie cookie : cookies) {
			if (SESSION_COOKIE_NAME.equalsIgnoreCase(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return "none";
	}

	/**
	 * 응답에 JSESSIONID Set-Cookie가 포함됐는지 진단 문자열로 만든다.
	 * <p>쿠키 속성(Secure/SameSite/Domain/Path)도 함께 노출되어 설정 문제를 바로 식별할 수 있다.</p>
	 */
	private String describeSetCookie(HttpServletResponse response) {
		Collection<String> setCookies = response.getHeaders(SET_COOKIE_HEADER);
		if (setCookies == null || setCookies.isEmpty()) {
			return "없음(이번 응답에서 세션쿠키 미발급)";
		}
		for (String value : setCookies) {
			if (value != null && value.regionMatches(true, 0, SESSION_COOKIE_NAME, 0, SESSION_COOKIE_NAME.length())) {
				return value;
			}
		}
		return "없음(JSESSIONID 외 다른 쿠키만 발급)";
	}
}
