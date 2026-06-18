package kr.or.nia.dpg.vpos.config.properties.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "data.api.kftc")
public class ApiKftcProperties {

    private String baseUrl;
    private String apiKey;
    private Account account = new Account();

    @Getter
    @Setter
    public static class Account {
        private Freeze freeze = new Freeze();
    }

    @Getter
    @Setter
    public static class Freeze {
        private String baseUrl;
        private String apiOrgCode;
        private String clientId;
        private String clientSecret;
        private String accessTokenUrl;
        private String freezableAccountsUrl;
        private String freezeAccountsUrl;
    }
}
