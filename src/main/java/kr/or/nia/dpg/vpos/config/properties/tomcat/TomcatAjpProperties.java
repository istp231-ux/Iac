package kr.or.nia.dpg.vpos.config.properties.tomcat;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
