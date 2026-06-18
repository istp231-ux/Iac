package kr.or.nia.dpg.vpos.web.view;

import kr.or.nia.dpg.vpos.enums.exception.VpsExceptionType;
import kr.or.nia.dpg.vpos.exception.VpsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "kr.or.nia.dpg.vpos.web.view")
@Slf4j
public class ViewGlobalExceptionHandler {

    @ExceptionHandler(VpsException.class)
    public String handleVpsException(VpsException ex, Model model) {
        VpsExceptionType type = ex.getType();

        if(type != VpsExceptionType.UNAUTHORIZED){
            log.error("[View예외:{}] type={}, message={}, detail={}, api={}",
                    type.getOrigin().name(),
                    type.name(), type.getMessage(), ex.getDetail(),
                    ex.getExternalApiName() != null ? ex.getExternalApiName() : "N/A");
            throw ex;
        }

        log.warn("[View예외:세션만료] type={}, message={}", type.name(), type.getMessage());

        model.addAttribute("message", "세션이 만료되었습니다. 처음 화면으로 이동합니다.");
        model.addAttribute("redirectUrl", "/vpos/main");
         return "view/session-timeout-msg-modal";
    }

}
