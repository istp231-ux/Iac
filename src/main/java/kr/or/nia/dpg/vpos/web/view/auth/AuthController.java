package kr.or.nia.dpg.vpos.web.view.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 인증 화면(뷰) 컨트롤러.
 * <p>본인인증 페이지를 렌더링한다. 실제 인증 처리는 {@code /api/v1/auth/**}에서 수행된다.</p>
 */
@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/view/auth")
public class AuthController {

    @GetMapping("/certification")
    public String viewCertificationPage() {
        log.info("[인증화면] 본인인증 페이지 진입");
        return "view/auth/certification";
    }

}
