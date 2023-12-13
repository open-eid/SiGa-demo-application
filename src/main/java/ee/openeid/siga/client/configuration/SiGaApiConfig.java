package ee.openeid.siga.client.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Optional;

@Configuration
public class SiGaApiConfig {

    @Bean
    SSLContext sigaApiSslContext(SiGaDemoProperties.SigaApi sigaApiProperties) {
        try {
            return new SSLContextBuilder()
                    .loadTrustMaterial(loadTrustStore(sigaApiProperties), null)
                    .build();
        } catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to create SiGa API SSL context", e);
        }
    }

    private static KeyStore loadTrustStore(SiGaDemoProperties.SigaApi sigaApiProperties) {
        String trustStoreType = Optional
                .ofNullable(sigaApiProperties.trustStoreType())
                .filter(StringUtils::isNotBlank)
                .orElse(KeyStore.getDefaultType());

        KeyStore trustStore;
        try {
            trustStore = KeyStore.getInstance(trustStoreType);
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Failed to create keystore of type: " + trustStoreType, e);
        }

        Resource trustStoreResource = sigaApiProperties.trustStore();
        try (InputStream in = trustStoreResource.getInputStream()) {
            trustStore.load(in, sigaApiProperties.trustStorePassword());
        } catch (CertificateException | IOException | NoSuchAlgorithmException | NullPointerException e) {
            throw new IllegalStateException("Failed to load truststore: " + trustStoreResource, e);
        }

        return trustStore;
    }

}
