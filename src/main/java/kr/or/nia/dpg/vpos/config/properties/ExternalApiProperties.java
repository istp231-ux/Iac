package kr.or.nia.dpg.vpos.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "external.api")
public class ExternalApiProperties {

    private String vposUrl;
}
