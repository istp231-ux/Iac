package kr.or.nia.dpg.vpos.web.api.v1.auth;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.nia.dpg.vpos.dto.SessionUser;
import kr.or.nia.dpg.vpos.util.SessionUserUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 2Factor 인증 결과 처리 RestController
 * <p>인증 모듈(공동/금융인증, 간편인증)의 콜백 결과를 받아 검증 후 세션에 저장하는 역할</p>
 * <p>현재는 인증 모듈 미연동 상태로 임시 데이터로 처리</p>
 * - 만일 해당 로직 불필요한 경우 삭제 가능
 *
 * <p><b>[세션 연동 진단]</b> "1차 인증 완료 후 다음 요청에서 1차 미완료로 조회됨" 증상을 추적하기 위해
 * 각 인증 요청 진입 시점에 브라우저가 보낸 세션 쿠키 상태를 로그로 남긴다.
 * 1차 요청에서 발급한 JSESSIONID가 2차 요청까지 왕복되는지를 한 줄로 판별할 수 있다.
 * (개인정보는 기록하지 않으며 세션 식별자/쿠키 상태/원격 IP만 남긴다.)</p>
 */
@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
public class ApiAuthenticationController {

	/**
	 * 1차 인증(공동인증/금융인증) 결과 처리
	 * <p>인증 모듈로부터 콜백을 받아 결과를 검증하고 세션에 저장함</p>
	 * <p>TODO: 실제 인증 모듈 연동 시 콜백 파라미터 및 검증 로직 추가 필요</p>
	 *
	 * @return 인증 처리 결과
	 */
	@PostMapping("/certificate")
	public ResponseEntity<Map<String, Object>> processCertificateAuth(HttpServletRequest request) {
		log.info("[1차인증] 공동/금융인증 결과 수신. {}", describeIncomingSession(request));

		// TODO: 실제 인증 모듈 연동 시 콜백 결과 검증 로직으로 교체
		SessionUserUtil.setFirstAuthCompleted();

		log.info("[1차인증] 1차 인증 완료 처리됨.");
		return ResponseEntity.ok(Map.of("success", true, "message", "공동/금융인증이 완료되었습니다."));
	}

	/**
	 * 2차 인증(간편인증) 결과 처리 - 현재는 휴대폰인증 결과를 처리(임시)
	 * <p>인증 모듈로부터 콜백을 받아 결과를 검증하고 세션에 저장함</p>
	 * <p>1차 인증이 완료된 상태에서만 호출되어야 함</p>
	 * <p>TODO: 실제 인증 모듈 연동 시 콜백 파라미터 및 검증 로직 추가 필요</p>
	 *
	 * @return 인증 처리 결과
	 */
	@PostMapping("/simple")
	public ResponseEntity<Map<String, Object>> processSimpleAuth(HttpServletRequest request) {
		log.info("[2차인증] 간편인증 결과 수신. {}", describeIncomingSession(request));

		// [세션 연동 진단] 1차 인증 요청에서 저장한 세션이 이번 2차 인증 요청까지 유지되는지 확인한다.
		// 인증 모듈 미연동 상태이므로 흐름을 막지 않고(아래 차단 로직은 주석 유지) 진단 로그만 남긴다.
		// isFirstAuthCompleted() 내부에서 세션ID/쿠키 상태 등 상세 진단 로그가 함께 기록된다.
		if (!SessionUserUtil.isFirstAuthCompleted()) {
			log.warn("[2차인증] 2차 인증 시점에 1차 인증 상태가 유지되지 않음 -> 세션 연동 실패 의심. "
					+ "직전 [1차인증] 로그의 sessionId와 비교 필요.");
		}

		/*if (!SessionUserUtil.isFirstAuthCompleted()) {
			log.warn("1차 인증 미완료 상태에서 2차 인증 시도");
			return ResponseEntity.badRequest().body(Map.of("success", false, "message", "공동/금융인증을 먼저 완료해주세요."));
		}*/

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

	/**
	 * 요청 진입 시점의 세션 쿠키 왕복 상태를 진단용 문자열로 만든다.
	 * <p>개인정보(주민번호/이름/CI/DI/연락처)는 포함하지 않는다.</p>
	 * <ul>
	 *   <li>requestedSessionId : 브라우저가 보낸 세션 ID (null이면 쿠키 미전달)</li>
	 *   <li>sessionIdValid : 보낸 세션 ID가 서버에서 유효한지 (false면 만료/변경됨)</li>
	 *   <li>fromCookie : 세션 ID가 쿠키 경로로 왔는지 (false면 비정상 경로)</li>
	 * </ul>
	 */
	private String describeIncomingSession(HttpServletRequest request) {
		return String.format(
				"requestedSessionId=%s, sessionIdValid=%s, fromCookie=%s, remoteAddr=%s",
				request.getRequestedSessionId(),
				request.isRequestedSessionIdValid(),
				request.isRequestedSessionIdFromCookie(),
				request.getRemoteAddr());
	}
}
