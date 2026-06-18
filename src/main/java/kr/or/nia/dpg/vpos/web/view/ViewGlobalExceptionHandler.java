package kr.or.nia.dpg.vpos.web.view;

import kr.or.nia.dpg.vpos.enums.exception.VpsExceptionType;
import kr.or.nia.dpg.vpos.exception.VpsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@ControllerAdvice(basePackages = "kr.or.nia.dpg.vpos.web.view")
@Slf4j
public class ViewGlobalExceptionHandler {

    @ExceptionHandler(VpsException.class)
    public String handleVpsException(VpsException ex, Model model, HttpServletRequest request) {
        VpsExceptionType type = ex.getType();
        String reqInfo = describeRequest(request);

        if(type != VpsExceptionType.UNAUTHORIZED){
            log.error("[View예외:{}] type={}, message={}, detail={}, api={}, {}",
                    type.getOrigin().name(),
                    type.name(), type.getMessage(), ex.getDetail(),
                    ex.getExternalApiName() != null ? ex.getExternalApiName() : "N/A",
                    reqInfo);
            throw ex;
        }

        log.warn("[View예외:세션만료] type={}, message={}, {}", type.name(), type.getMessage(), reqInfo);

        model.addAttribute("message", "세션이 만료되었습니다. 처음 화면으로 이동합니다.");
        model.addAttribute("redirectUrl", "/vpos/main");
         return "view/session-timeout-msg-modal";
    }

    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, Model model, HttpServletRequest request) {
        log.error("[View예외:내부서버] 예상치 못한 오류 - exType={}, error={}, {}",
                ex.getClass().getSimpleName(), ex.getMessage(), describeRequest(request));

        model.addAttribute("message", "처리 중 오류가 발생했습니다.");
        model.addAttribute("redirectUrl", "/vpos/main");
        return "view/session-timeout-msg-modal";
    }

    private String describeRequest(HttpServletRequest request) {
        if (request == null) {
            return "request=null";
        }
        HttpSession session = request.getSession(false);
        return String.format("uri=%s, method=%s, remoteAddr=%s, sessionId=%s, isNew=%s, "
                        + "requestedSessionId=%s, sessionIdValid=%s, fromCookie=%s, "
                        + "scheme=%s, isSecure=%s, X-Forwarded-Proto=%s",
                request.getRequestURI(),
                request.getMethod(),
                request.getRemoteAddr(),
                session != null ? session.getId() : "NO_SESSION",
                session != null ? String.valueOf(session.isNew()) : "N/A",
                request.getRequestedSessionId(),
                request.isRequestedSessionIdValid(),
                request.isRequestedSessionIdFromCookie(),
                request.getScheme(),
                request.isSecure(),
                request.getHeader("X-Forwarded-Proto"));
    }
}
