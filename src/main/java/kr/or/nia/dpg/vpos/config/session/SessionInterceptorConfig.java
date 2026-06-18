package kr.or.nia.dpg.vpos.config.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import kr.or.nia.dpg.vpos.interceptor.session.SessionCheckInterceptor;
import lombok.RequiredArgsConstructor;

/**
 * 세션 인터셉터 등록 설정
 * <p>인증이 필요한 페이지에 대해 세션 체크를 일괄 적용함</p>
 * <p>TODO: 2FA 모듈 연동 후 경로 확정 시 활성화</p>
 */
//@Configuration
@RequiredArgsConstructor
public class SessionInterceptorConfig implements WebMvcConfigurer {

	private static final Logger log = LoggerFactory.getLogger(SessionInterceptorConfig.class);

	private final SessionCheckInterceptor sessionCheckInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		try {
			registry.addInterceptor(sessionCheckInterceptor)
			.addPathPatterns("/**")
			.excludePathPatterns(
					"",							// 루트 경로
					"/",						// 루트 경로
					"/main",					// 메인 페이지 (인증 전 접근 필요)
					"/api/v1/auth/**",			// 인증 요청 api
					"/view/terms/**",			// 개인정보 취급 동의 페이지
					"/view/auth/**",			// 인증 페이지
					"/css/**",					// 정적 리소스
					"/font/**",
					"/img/**",
					"/js/**",
					"/scss/**",
					"/webfonts/**",
					"/error/**"
					);
			log.info("[SessionInterceptorConfig] 세션 인터셉터 등록 완료");
		} catch (Exception e) {
			log.error("[SessionInterceptorConfig] 세션 인터셉터 등록 실패 - exType={}, error={}", e.getClass().getSimpleName(), e.getMessage());
		}
	}

}
