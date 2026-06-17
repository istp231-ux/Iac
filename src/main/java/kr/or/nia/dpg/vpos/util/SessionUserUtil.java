package kr.or.nia.dpg.vpos.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import kr.or.nia.dpg.vpos.dto.SessionUser;
import kr.or.nia.dpg.vpos.enums.exception.VpsExceptionType;
import kr.or.nia.dpg.vpos.exception.VpsException;

/**
 * HTTP 세션 기반 사용자 정보 관리 유틸리티 클래스
 *
 * 본인인증(2-factor) 완료 후 세션에 사용자 정보를 저장하고
 * 프로세스 진행 중 사용자 정보 조회 및 인증 여부 확인 기능을 제공
 *
 * <p>세션 라이프사이클:</p>
 * <ol>
 * 	<li>본인인증 완료 -> {@link #setSessionUser(SessionUser)}</li>
 * 	<li>프로세스 진행 중 -> {@link #getSessionUser()}, {@link #getRequiredSessionUser()}</li>
 * 	<li>인증여부 확인 -> {@link #isCertificatedUser()}</li>
 * 	<li>프로세스 완료 -> {@link #invalidateSession()}</li>
 * </ol>
 *
 * <p><b>[세션 연동 진단 로그]</b><br>
 * "1차 인증을 완료했는데 다음 요청에서 미완료로 조회되는" 증상은 대부분 두 요청이
 * 같은 세션을 공유하지 못하는 경우(세션 쿠키 미왕복, 세션 ID 변경, 신규 세션 생성)에 발생한다.
 * 이를 추적할 수 있도록 인증 정보를 저장(set)/조회(is·get)하는 시점에 다음 정보를 함께 남긴다.
 * <ul>
 *   <li>sessionId : 서버가 현재 사용 중인 세션 ID (set 시점과 check 시점이 다르면 세션이 바뀐 것)</li>
 *   <li>isNew : 이번 요청에서 세션이 새로 생성되었는지 (true면 직전 세션이 유실됨)</li>
 *   <li>requestedSessionId : 브라우저가 쿠키로 보낸 세션 ID (null이면 쿠키 자체가 안 옴)</li>
 *   <li>sessionIdValid : 브라우저가 보낸 세션 ID가 서버에서 유효한지 (false면 만료/변경됨)</li>
 *   <li>fromCookie : 세션 ID가 쿠키에서 왔는지 (false면 URL rewriting 등 비정상 경로)</li>
 * </ul>
 * </p>
 */
public class SessionUserUtil {

	private static final Logger log = LoggerFactory.getLogger(SessionUserUtil.class);

    private static final String SESSION_USER_KEY = "sessionUser";

    private static final String FIRST_AUTH_KEY = "firstAuthCompleted";
    private static final String SECOND_AUTH_KEY = "secondAuthCompleted";

    // 인스턴스 생성 방지
    private SessionUserUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 세션에서 사용자 정보 조회
     *
     * @return 세션에 저장된 사용자 정보, 없으면 null
     */
    public static SessionUser getSessionUser() {
        HttpServletRequest request = getCurrentRequest();
        HttpSession session = (request != null) ? request.getSession(false) : null;
        if (session == null) {
            log.warn("[getSessionUser] 세션 없음 -> 미인증 처리. {}", sessionDiag(request));
            return null;
        }

        Object sessionUser = session.getAttribute(SESSION_USER_KEY);
        if (!(sessionUser instanceof SessionUser)) {
            log.warn("[getSessionUser] 세션은 있으나 사용자 정보 없음. sessionId={}, isNew={}, attrType={}",
                    session.getId(), session.isNew(),
                    sessionUser == null ? "null" : sessionUser.getClass().getSimpleName());
            return null;
        }

        log.debug("[getSessionUser] 사용자 정보 조회 성공. sessionId={}", session.getId());
        return (SessionUser) sessionUser;
    }

    /**
     * 세션에서 사용자 정보를 조회하되, 없으면 예외를 던짐
     * <p>본인인증이 반드시 완료된 상태에서만 호출해야함</p>
     *
     * @return 세션에 저장된 사용자 정보
     */
    public static SessionUser getRequiredSessionUser() {
        SessionUser sessionUser = getSessionUser();
        if (sessionUser == null) {
            log.warn("[getRequiredSessionUser] 인증된 사용자 정보가 없어 UNAUTHORIZED 처리. {}",
                    sessionDiag(getCurrentRequest()));
            throw new VpsException(VpsExceptionType.UNAUTHORIZED);
        }
        return sessionUser;
    }

    /**
     * 본인인증 완료 후 사용자 정보를 세션에 저장함
     * <p>세션이 존재하지 않을 경우 새로 생성하여 저장함</p>
     *
     * @param sessionUser 저장할 사용자 정보
     */
    public static void setSessionUser(SessionUser sessionUser) {
        if (sessionUser == null) {
            log.warn("[setSessionUser] 저장할 사용자 정보가 null입니다.");
            return;
        }

        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            log.error("[setSessionUser] 요청 컨텍스트가 없어 세션에 사용자 정보를 저장할 수 없습니다.");
            throw new VpsException(VpsExceptionType.UNAUTHORIZED);
        }

        // changeSessionId()는 세션이 존재해야 동작하므로 먼저 세션을 확보한다.
        HttpSession session = request.getSession(true);
        String oldSessionId = session.getId();

        // Spring Security 미사용 환경에서 세션 고정 공격(Session Fixation) 방지를 위해 세션 ID 재생성
        // 주의: 세션 ID가 바뀌므로 브라우저가 새 JSESSIONID 쿠키를 받지 못하면 다음 요청에서 세션이 유실된다.
        request.changeSessionId();
        session = request.getSession(false);
        String newSessionId = (session != null) ? session.getId() : null;

        if (session == null) {
            log.error("[setSessionUser] 세션 ID 재생성 후 세션이 사라졌습니다. oldSessionId={}", oldSessionId);
            throw new VpsException(VpsExceptionType.UNAUTHORIZED);
        }

        session.setAttribute(SESSION_USER_KEY, sessionUser);
        log.info("[setSessionUser] 사용자 정보 저장 완료. oldSessionId={} -> newSessionId={}, uri={}",
                oldSessionId, newSessionId, request.getRequestURI());
    }

    /**
     * 1차 인증(공동인증/금융인증) 완료 처리
     * <p>세션이 존재하지 않을 경우 새로 생성함</p>
     */
    public static void setFirstAuthCompleted() {
        setAuthFlag(FIRST_AUTH_KEY, "1차");
    }

    /**
     * 2차 인증(간편인증) 완료 처리
     * <p>세션이 존재하지 않을 경우 새로 생성함</p>
     */
    public static void setSecondAuthCompleted() {
        setAuthFlag(SECOND_AUTH_KEY, "2차");
    }

    /**
     * 1차 인증 완료 여부 확인
     *
     * @return 1차 인증 완료 시 true, 미인증 시 false
     */
    public static boolean isFirstAuthCompleted() {
        return isAuthFlagCompleted(FIRST_AUTH_KEY, "1차");
    }

    /**
     * 2차 인증 완료 여부 확인
     *
     * @return 2차 인증 완료 시 true, 미인증 시 false
     */
    public static boolean isSecondAuthCompleted() {
        return isAuthFlagCompleted(SECOND_AUTH_KEY, "2차");
    }

    /**
     * 1차, 2차 인증 모두 완료 여부 확인
     *
     * @return 모두 완료 시 true, 하나라도 미완료 시 false
     */
    public static boolean isAllAuthenticated() {
        return isFirstAuthCompleted() && isSecondAuthCompleted();
    }

    /**
     * 현재 사용자의 본인인증 여부를 확인
     *
     * @return 본인인증 완료 시 true, 미인증시 false
     */
    public static boolean isCertificatedUser() {
        return getSessionUser() != null;
    }

    /**
     * 현재 세션을 무효화하여 저장된 모든 사용자 정보를 제거함
     * <p>프로세스 완료 후 호출하여 세션 데이터를 정리함</p>
     */
    public static void invalidateSession() {
        HttpSession session = getHttpSession();
        if (session == null) {
            log.warn("[invalidateSession] 무효화할 세션이 존재하지 않습니다.");
            return;
        }
        String sessionId = session.getId();
        try {
            session.invalidate();
            log.info("[invalidateSession] 세션 무효화 완료. sessionId={}", sessionId);
        } catch (IllegalStateException e) {
            // 이미 무효화된 세션
            log.warn("[invalidateSession] 이미 무효화된 세션입니다. sessionId={}", sessionId);
        }
    }

    /**
     * 인증 완료 플래그를 세션에 저장하고, 세션 연동 진단 정보를 로그로 남긴다.
     *
     * @param key        세션 속성 키
     * @param authLabel  로그 표기용 인증 단계 명칭(예: "1차")
     */
    private static void setAuthFlag(String key, String authLabel) {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            log.error("[set{}AuthCompleted] 요청 컨텍스트가 없어 인증 완료 처리를 할 수 없습니다.", authLabel);
            throw new VpsException(VpsExceptionType.UNAUTHORIZED);
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(key, Boolean.TRUE);
        log.info("[set{}AuthCompleted] {} 인증 완료 처리. sessionId={}, isNew={}, uri={}",
                authLabel, authLabel, session.getId(), session.isNew(), request.getRequestURI());
    }

    /**
     * 인증 완료 플래그를 조회하고, 미완료로 판정될 경우 그 원인을 추적할 수 있는 진단 정보를 로그로 남긴다.
     *
     * @param key        세션 속성 키
     * @param authLabel  로그 표기용 인증 단계 명칭(예: "1차")
     * @return 인증 완료 시 true
     */
    private static boolean isAuthFlagCompleted(String key, String authLabel) {
        HttpServletRequest request = getCurrentRequest();
        HttpSession session = (request != null) ? request.getSession(false) : null;

        if (session == null) {
            // 세션 자체가 없음 = 직전 요청의 세션이 유실됨. 원인 추적을 위해 쿠키/세션 ID 상태를 남긴다.
            log.warn("[is{}AuthCompleted] 세션 없음 -> 미완료 처리. {}", authLabel, sessionDiag(request));
            return false;
        }

        Object value = session.getAttribute(key);
        boolean completed = Boolean.TRUE.equals(value);
        if (!completed) {
            log.warn("[is{}AuthCompleted] 세션은 있으나 인증 플래그 미설정 -> 미완료 처리. "
                            + "sessionId={}, isNew={}, value={}, {}",
                    authLabel, session.getId(), session.isNew(), value, sessionDiag(request));
        } else {
            log.info("[is{}AuthCompleted] {} 인증 완료 확인. sessionId={}",
                    authLabel, authLabel, session.getId());
        }
        return completed;
    }

    /**
     * 현재 요청의 HttpSession을 가져옴
     *
     * @return HttpSession 객체, 세션이 없으면 null
     */
    private static HttpSession getHttpSession() {
        HttpServletRequest request = getCurrentRequest();
        return (request != null) ? request.getSession(false) : null;
    }

    /**
     * 현재 요청 객체를 가져옴. 요청 컨텍스트가 없으면(스케줄러/비동기 등) null을 반환.
     */
    private static HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes requestAttributes =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return requestAttributes.getRequest();
        } catch (IllegalStateException e) {
            log.warn("[SessionUserUtil] 현재 요청 컨텍스트가 존재하지 않습니다. (비요청 스레드에서 호출)");
            return null;
        }
    }

    /**
     * 세션 연동 실패 원인 추적용 요청/쿠키 진단 문자열을 생성한다.
     */
    private static String sessionDiag(HttpServletRequest request) {
        if (request == null) {
            return "request=null (요청 컨텍스트 없음)";
        }
        return String.format(
                "uri=%s, requestedSessionId=%s, sessionIdValid=%s, fromCookie=%s, fromURL=%s, remoteAddr=%s",
                request.getRequestURI(),
                request.getRequestedSessionId(),
                request.isRequestedSessionIdValid(),
                request.isRequestedSessionIdFromCookie(),
                request.isRequestedSessionIdFromURL(),
                request.getRemoteAddr());
    }
}
