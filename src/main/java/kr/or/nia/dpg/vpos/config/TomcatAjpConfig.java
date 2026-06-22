package kr.or.nia.dpg.vpos.config;

import kr.or.nia.dpg.vpos.config.properties.tomcat.TomcatAjpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AjpNioProtocol;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * Tomcat AJP 커넥터 설정.
 *
 * <p>{@code data.tomcat.ajp.enabled=true}일 때만 활성화되며,
 * Apache/Nginx 리버스 프록시에서 AJP 프로토콜로 Tomcat에 연결할 수 있도록
 * 추가 커넥터를 등록한다.</p>
 *
 * <p>scheme/secure 설정은 프록시 뒤에서 Cookie의 Secure 플래그 동작에 영향을 주므로
 * application-ajp.yml의 값과 실제 접속 프로토콜(HTTP/HTTPS)을 반드시 일치시켜야 한다.</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "data.tomcat.ajp.enabled", havingValue = "true")
public class TomcatAjpConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private final TomcatAjpProperties ajpProperties;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        Connector ajpConnector = new Connector(ajpProperties.getProtocol());
        ajpConnector.setPort(ajpProperties.getPort());
        ajpConnector.setSecure(Boolean.TRUE.equals(ajpProperties.getSecure()));
        ajpConnector.setScheme(ajpProperties.getScheme());
        ajpConnector.setAllowTrace(Boolean.TRUE.equals(ajpProperties.getAllowTrace()));

        AjpNioProtocol protocol = (AjpNioProtocol) ajpConnector.getProtocolHandler();
        protocol.setSecretRequired(false);

        factory.addAdditionalTomcatConnectors(ajpConnector);

        log.info("[TomcatAjpConfig] AJP 커넥터 등록 완료 - port={}, scheme={}, secure={}, protocol={}",
                ajpProperties.getPort(), ajpProperties.getScheme(),
                ajpProperties.getSecure(), ajpProperties.getProtocol());
    }
}
