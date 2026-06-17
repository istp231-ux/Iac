package kr.or.nia.dpg.vpos.config.properties.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// TODO 추후 삭제 필요
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "data.api.certifi")
public class ApiCertifiProperties {

    private String baseUrl;
    private String apiKey;

}
