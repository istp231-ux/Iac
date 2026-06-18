package kr.or.nia.dpg.vpos.config;

import kr.or.nia.dpg.vpos.config.properties.TomcatAjpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.ajp.AjpNioProtocol;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

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
        ajpConnector.setSecure(ajpProperties.isSecure());
        ajpConnector.setScheme(ajpProperties.getScheme());
        ajpConnector.setAllowTrace(ajpProperties.isAllowTrace());

        AjpNioProtocol protocol = (AjpNioProtocol) ajpConnector.getProtocolHandler();
        protocol.setSecretRequired(false);

        factory.addAdditionalTomcatConnectors(ajpConnector);

        log.info("[TomcatAjpConfig] AJP 커넥터 등록 완료 - port={}, scheme={}, secure={}, protocol={}",
                ajpProperties.getPort(), ajpProperties.getScheme(),
                ajpProperties.isSecure(), ajpProperties.getProtocol());
    }
}
