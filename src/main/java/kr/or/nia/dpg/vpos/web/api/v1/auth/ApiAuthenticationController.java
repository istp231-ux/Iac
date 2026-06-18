package kr.or.nia.dpg.vpos.web.api.v1.auth;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.nia.dpg.vpos.dto.SessionUser;
import kr.or.nia.dpg.vpos.util.SessionUserUtil;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class ApiAuthenticationController {

	@PostMapping("/certificate")
	public ResponseEntity<Map<String, Object>> processCertificateAuth(HttpServletRequest request) {
		log.info("[1차인증] 공동/금융인증 결과 수신. {}", describeSession(request));

		// TODO: 실제 인증 모듈 연동 시 콜백 결과 검증 로직으로 교체
		SessionUserUtil.setFirstAuthCompleted();

		log.info("[1차인증] 1차 인증 완료 처리됨. sessionId={}", getSessionId(request));
		return ResponseEntity.ok(Map.of("success", true, "message", "공동/금융인증이 완료되었습니다."));
	}

	@PostMapping("/simple")
	public ResponseEntity<Map<String, Object>> processSimpleAuth(HttpServletRequest request) {
		log.info("[2차인증] 간편인증 결과 수신. {}", describeSession(request));

		if (!SessionUserUtil.isFirstAuthCompleted()) {
			HttpSession session = request.getSession(false);
			log.error("┌──────────────────────────────────────────────────────────────────────┐");
			log.error("│ ★ [2차인증] 1차 인증 미완료 상태에서 2차 인증 시도 — 원인 분석 시작 ★ │");
			log.error("├──────────────────────────────────────────────────────────────────────┤");
			log.error("│ sessionId          = {}", session != null ? session.getId() : "NO_SESSION");
			log.error("│ session.isNew      = {}", session != null ? session.isNew() : "N/A");
			log.error("│ requestedSessionId = {}", request.getRequestedSessionId());
			log.error("│ sessionIdValid     = {}", request.isRequestedSessionIdValid());
			log.error("│ fromCookie         = {}", request.isRequestedSessionIdFromCookie());
			log.error("│ scheme             = {}", request.getScheme());
			log.error("│ isSecure           = {}", request.isSecure());
			log.error("│ X-Forwarded-Proto  = {}", request.getHeader("X-Forwarded-Proto"));
			log.error("│ X-Forwarded-For    = {}", request.getHeader("X-Forwarded-For"));
			log.error("│ remoteAddr         = {}", request.getRemoteAddr());
			log.error("├──────────────────────────────────────────────────────────────────────┤");

			if (request.getRequestedSessionId() == null) {
				log.error("│ [진단결과:우리서버/인프라] 브라우저가 JSESSIONID 쿠키를 보내지 않음.");
				log.error("│ → 확인1: 1차인증 응답에 Set-Cookie가 내려갔는지 (세션진단:OUT 로그 확인)");
				log.error("│ → 확인2: Apache/AJP 프록시가 Set-Cookie를 제거하고 있지 않은지");
				log.error("│ → 확인3: Secure 쿠키 + HTTP 접속 조합이 아닌지 (application-api.yml의 secure 값 확인)");
				log.error("│ → 확인4: Cookie Path=/vpos 인데 요청 경로가 다르지 않은지");
			} else if (!request.isRequestedSessionIdValid()) {
				log.error("│ [진단결과:우리서버] 브라우저가 보낸 세션ID가 서버에서 유효하지 않음.");
				log.error("│ → 확인1: 서버가 여러 대인 경우 LB Sticky Session 설정 확인");
				log.error("│ → 확인2: 세션 타임아웃(20분) 경과 여부");
				log.error("│ → 확인3: 서버 재기동으로 인한 세션 초기화 여부");
			} else if (session != null && session.isNew()) {
				log.error("│ [진단결과:우리서버] 세션ID는 유효한데 새 세션이 생성됨.");
				log.error("│ → changeSessionId() 호출로 세션이 교체되었을 가능성");
			} else {
				log.error("│ [진단결과:우리서버/코드] 세션은 정상인데 1차인증 플래그가 없음.");
				log.error("│ → 1차인증 경로에서 setFirstAuthCompleted()가 호출되지 않았을 가능성");
			}
			log.error("└──────────────────────────────────────────────────────────────────────┘");
		} else {
			log.info("[2차인증] 1차 인증 완료 확인됨. sessionId={}", getSessionId(request));
		}

		// TODO: 실제 인증 모듈 연동 시 콜백 결과 검증 로직으로 교체
		SessionUserUtil.setSecondAuthCompleted();

		// TODO: 실제 인증 결과에서 받은 사용자 정보로 교체
		SessionUser sessionUser = new SessionUser();
		sessionUser.setUserName("홍길동");
		sessionUser.setSsn("9111111234567");
		sessionUser.setMobileNumber("01012345678");
		sessionUser.setEmail("gildong-hong@testhub.com");
		sessionUser.setDi("TestDi12345678");

		SessionUserUtil.setSessionUser(sessionUser);
		log.info("[2차인증] 2차 인증 완료, 사용자 세션 저장 완료.");

		return ResponseEntity.ok(Map.of("success", true, "message", "인증이 모두 완료되었습니다."));
	}

	private String describeSession(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		return String.format(
				"sessionId=%s, isNew=%s, requestedSessionId=%s, sessionIdValid=%s, "
				+ "fromCookie=%s, scheme=%s, isSecure=%s, remoteAddr=%s",
				session != null ? session.getId() : "NO_SESSION",
				session != null ? String.valueOf(session.isNew()) : "N/A",
				request.getRequestedSessionId(),
				request.isRequestedSessionIdValid(),
				request.isRequestedSessionIdFromCookie(),
				request.getScheme(),
				request.isSecure(),
				request.getRemoteAddr());
	}

	private String getSessionId(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		return session != null ? session.getId() : "NO_SESSION";
	}
}
