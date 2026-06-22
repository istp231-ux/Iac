package kr.or.nia.dpg.vpos.config.properties.tomcat;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Tomcat AJP 커넥터 설정 프로퍼티.
 *
 * <p>application-ajp.yml의 {@code data.tomcat.ajp.*} 값을 바인딩한다.
 * enabled/secure/allowTrace는 {@link Boolean} 래퍼 타입으로 선언하여
 * 미설정 시 null을 허용한다.</p>
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "data.tomcat.ajp")
public class TomcatAjpProperties {
	private Boolean enabled;
	private String scheme;
	private String protocol;
	private int port;
	private Boolean secure;
	private Boolean allowTrace;
}
