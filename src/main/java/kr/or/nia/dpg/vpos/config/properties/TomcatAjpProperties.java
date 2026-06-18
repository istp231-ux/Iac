package kr.or.nia.dpg.vpos.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "data.tomcat.ajp")
public class TomcatAjpProperties {

    private boolean enabled = false;
    private String scheme = "http";
    private String protocol = "AJP/1.3";
    private int port = 21105;
    private boolean secure = false;
    private boolean allowTrace = false;
}
