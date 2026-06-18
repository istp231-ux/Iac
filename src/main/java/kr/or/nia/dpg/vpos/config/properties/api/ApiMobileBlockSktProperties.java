package kr.or.nia.dpg.vpos.config.properties.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "data.api.mobile.micro.payments.block.skt")
public class ApiMobileBlockSktProperties {

    private String baseUrl;
    private String pbszUserKey;
    private String apiKey;
    private Path path = new Path();

    @Getter
    @Setter
    public static class Path {
        private String mobileListSkt;
        private String blockMicroPaymentsSkt;
    }
}
