package kr.or.nia.dpg.vpos.config.properties.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "data.api.fss")
public class ApiFssProperties {

    private String baseUrl;
    private String apiKey;
    private String fssApiKey;
    private String financeCd;
    private String exposureAccidentInsertUrl;
    private String exposureAccidentCancelUrl;
}
