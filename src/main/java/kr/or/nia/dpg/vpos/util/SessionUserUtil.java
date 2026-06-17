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
        try {
            HttpSession session = getHttpSession();
            if (session == null) {
                log.debug("[SessionUserUtil.getSessionUser] 세션이 존재하지 않습니다.");
                return null;
            }

            Object sessionUser = session.getAttribute(SESSION_USER_KEY);
            if (!(sessionUser instanceof SessionUser)) {
                return null;
            }

            return (SessionUser) sessionUser;
        } catch (Exception e) {
            log.error("[SessionUserUtil.getSessionUser] 세션 사용자 정보 조회 실패", e);
            return null;
        }
    }

    /**
     * 세션에서 사용자 정보를 조회하되, 없으면 예외를 던짐
     * <p>본인인증이 반드시 완료된 상태에서만 호출해야함</p>
     *
     * @return 세션에 저장된 사용자 정보
     */
    public static SessionUser getRequiredSessionUser() {
        try {
            SessionUser sessionUser = getSessionUser();
            if (sessionUser == null) {
                log.warn("[SessionUserUtil.getRequiredSessionUser] 인증된 사용자 정보가 없습니다.");
                throw new VpsException(VpsExceptionType.UNAUTHORIZED);
            }

            return sessionUser;
        } catch (VpsException e) {
            throw e;
        } catch (Exception e) {
            log.error("[SessionUserUtil.getRequiredSessionUser] 세션 사용자 정보 조회 중 오류 발생", e);
            throw new VpsException(VpsExceptionType.UNAUTHORIZED);
        }
    }

    /**
     * 본인인증 완료 후 사용자 정보를 세션에 저장함
     * <p>세션이 존재하지 않을 경우 새로 생성하여 저장함</p>
     *
     * @param sessionUser 저장할 사용자 정보
     */
    public static void setSessionUser(SessionUser sessionUser) {
        try {
            if (sessionUser == null) {
                log.warn("[SessionUserUtil.setSessionUser] 저장할 사용자 정보가 null입니다.");
                return;
            }
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = requestAttributes.getRequest();
            // Spring Security 미사용 환경에서 세션 고정 공격(Session Fixation) 방지를 위해 세션 ID 재생성
            request.changeSessionId();
            HttpSession httpSession = request.getSession(true);
            httpSession.setAttribute(SESSION_USER_KEY, sessionUser);
            log.info("[SessionUserUtil.setSessionUser] 세션 사용자 정보 저장 완료");
        } catch (Exception e) {
            log.error("[SessionUserUtil.setSessionUser] 세션 사용자 정보 저장 실패", e);
        }
    }

    /**
     * 1차 인증(공동인증/금융인증) 완료 처리
     * <p>세션이 존재하지 않을 경우 새로 생성함</p>
     */
    public static void setFirstAuthCompleted() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = requestAttributes.getRequest().getSession(true);
            session.setAttribute(FIRST_AUTH_KEY, true);
            log.info("[SessionUserUtil.setFirstAuthCompleted] 1차 인증 완료 처리");
        } catch (Exception e) {
            log.error("[SessionUserUtil.setFirstAuthCompleted] 1차 인증 완료 처리 실패", e);
        }
    }

    /**
     * 2차 인증(간편인증) 완료 처리
     * <p>세션이 존재하지 않을 경우 새로 생성함</p>
     */
    public static void setSecondAuthCompleted() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = requestAttributes.getRequest().getSession(true);
            session.setAttribute(SECOND_AUTH_KEY, true);
            log.info("[SessionUserUtil.setSecondAuthCompleted] 2차 인증 완료 처리");
        } catch (Exception e) {
            log.error("[SessionUserUtil.setSecondAuthCompleted] 2차 인증 완료 처리 실패", e);
        }
    }

    /**
     * 1차 인증 완료 여부 확인
     *
     * @return 1차 인증 완료 시 true, 미인증 시 false
     */
    public static boolean isFirstAuthCompleted() {
        try {
            HttpSession session = getHttpSession();
            if (session == null) {
                log.debug("[SessionUserUtil.isFirstAuthCompleted] 세션이 존재하지 않습니다.");
                return false;
            }
            return Boolean.TRUE.equals(session.getAttribute(FIRST_AUTH_KEY));
        } catch (Exception e) {
            log.error("[SessionUserUtil.isFirstAuthCompleted] 1차 인증 여부 확인 실패", e);
            return false;
        }
    }

    /**
     * 2차 인증 완료 여부 확인
     *
     * @return 2차 인증 완료 시 true, 미인증 시 false
     */
    public static boolean isSecondAuthCompleted() {
        try {
            HttpSession session = getHttpSession();
            if (session == null) {
                log.debug("[SessionUserUtil.isSecondAuthCompleted] 세션이 존재하지 않습니다.");
                return false;
            }
            return Boolean.TRUE.equals(session.getAttribute(SECOND_AUTH_KEY));
        } catch (Exception e) {
            log.error("[SessionUserUtil.isSecondAuthCompleted] 2차 인증 여부 확인 실패", e);
            return false;
        }
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
        try {
            HttpSession session = getHttpSession();
            if (session != null) {
                session.invalidate();
                log.info("[SessionUserUtil.invalidateSession] 세션 무효화 완료");
            } else {
                log.warn("[SessionUserUtil.invalidateSession] 무효화할 세션이 존재하지 않습니다.");
            }
        } catch (Exception e) {
            log.error("[SessionUserUtil.invalidateSession] 세션 무효화 실패", e);
        }
    }

    /**
     * 현재 요청의 HttpSession을 가져옴
     *
     * @return HttpSession 객체, 세션이 없으면 null
     */
    private static HttpSession getHttpSession() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            return requestAttributes.getRequest().getSession(false);
        } catch (IllegalStateException e) {
            log.warn("[SessionUserUtil.getHttpSession] 요청 컨텍스트가 존재하지 않습니다.", e);
            return null;
        } catch (Exception e) {
            log.error("[SessionUserUtil.getHttpSession] HttpSession 조회 실패", e);
            return null;
        }
    }
}
