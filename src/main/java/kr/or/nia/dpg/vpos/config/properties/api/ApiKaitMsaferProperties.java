package kr.or.nia.dpg.vpos.config.properties.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "data.api.kait.msafer")
public class ApiKaitMsaferProperties {

    private String baseUrl;
    private String clientId;
    private String apiKey;
    private String companyCode;
    private String authorization;
    private String inquirySubscriptionUrl;
    private String protectionChangeStatusUrl;
}
