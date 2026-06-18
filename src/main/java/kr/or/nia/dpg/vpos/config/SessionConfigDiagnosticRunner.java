package kr.or.nia.dpg.vpos.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.server.Cookie;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 애플리케이션 기동 완료 시점에 "실제로 적용된" 세션 쿠키 설정을 로그로 남기는 진단 러너.
 *
 * <p>"1차 인증 후 2차 인증 시 1차 미완료로 조회됨" 증상의 1순위 원인은
 * <b>secure 쿠키 + HTTP 접속</b> 조합이다. (Secure 쿠키는 HTTPS에서만 브라우저가 전송하므로,
 * HTTP로 접속하면 JSESSIONID 쿠키가 다음 요청에 실리지 않아 세션이 매 요청 새로 생성된다.)</p>
 *
 * <p>설정 파일이 여러 개(application.yml=secure:true, application-api.yml=secure:false)이고
 * 프로필 override로 값이 뒤바뀌므로, "지금 이 서버 인스턴스에 최종 반영된 값"을 직접 찍어야
 * 추측 없이 원인을 단정할 수 있다. ServerProperties는 모든 프로필/override가 병합된 최종 결과다.</p>
 *
 * <p>이 로그 한 줄이면 다음을 즉시 판정할 수 있다.</p>
 * <ul>
 *   <li>cookie.secure=true 인데 HTTP로 접속 중 → <b>이것이 원인</b> (api 프로필 미적용)</li>
 *   <li>cookie.secure=false 로 정상 적용 → secure는 원인 아님 (AJP scheme/LB sticky 등 다른 원인)</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionConfigDiagnosticRunner {

	private final ServerProperties serverProperties;
	private final Environment environment;

	@EventListener(ApplicationReadyEvent.class)
	public void logEffectiveSessionConfig() {
		ServerProperties.Servlet servlet = serverProperties.getServlet();
		String contextPath = (servlet != null) ? servlet.getContextPath() : null;

		Session session = (servlet != null) ? servlet.getSession() : null;
		Duration timeout = (session != null) ? session.getTimeout() : null;

		Boolean secure = null;
		Boolean httpOnly = null;
		Object sameSite = null;
		if (session != null && session.getCookie() != null) {
			Cookie cookie = session.getCookie();
			secure = cookie.getSecure();
			httpOnly = cookie.getHttpOnly();
			sameSite = cookie.getSameSite();
		}

		String[] activeProfiles = environment.getActiveProfiles();
		String profiles = (activeProfiles == null || activeProfiles.length == 0)
				? "(none/default)" : String.join(",", activeProfiles);

		log.info("==================== [세션설정 진단] 기동 시점 최종 반영값 ====================");
		log.info("[세션설정] activeProfiles   = {}", profiles);
		log.info("[세션설정] server.port      = {}", serverProperties.getPort());
		log.info("[세션설정] context-path     = {}", contextPath);
		log.info("[세션설정] session.timeout  = {}", timeout);
		log.info("[세션설정] cookie.secure    = {}", secure);
		log.info("[세션설정] cookie.http-only = {}", httpOnly);
		log.info("[세션설정] cookie.same-site = {}", sameSite);

		if (Boolean.TRUE.equals(secure)) {
			log.warn("[세션설정] ⚠ cookie.secure=true → HTTP(비암호화) 접속 시 브라우저가 JSESSIONID 쿠키를 "
					+ "전송하지 않습니다. 1차 인증 후 2차 인증에서 세션이 유실되어 '1차 미완료'가 발생합니다.");
			log.warn("[세션설정] ⚠ 내부 개발서버를 HTTP로 접속한다면 이것이 바로 원인입니다. "
					+ "해결: api 프로필 활성화(secure:false) 또는 HTTPS 접속.");
		} else if (Boolean.FALSE.equals(secure)) {
			log.info("[세션설정] ✓ cookie.secure=false → HTTP에서도 쿠키 전송 정상. secure는 원인 아님. "
					+ "세션이 계속 끊기면 AJP scheme 불일치/LB sticky session을 확인하세요.");
		} else {
			log.info("[세션설정] cookie.secure=미설정(서버 기본값). 컨테이너 기본 동작을 따릅니다.");
		}
		log.info("======================================================================");
	}
}
