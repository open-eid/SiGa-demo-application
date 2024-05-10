package ee.openeid.siga.client.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "siga")
public record SiGaDemoProperties(SigaClient client, SigaApi api) {

    @ConfigurationProperties(prefix = "siga.client")
    public record SigaClient(Hmac hmac) {
    }

    @ConfigurationProperties(prefix = "siga.client.hmac")
    public record Hmac(String algorithm, String serviceUuid, String sharedSigningKey) {
    }

    @ConfigurationProperties(prefix = "siga.api")
    public record SigaApi(
            String uri,
            Resource trustStore,
            char[] trustStorePassword,
            String trustStoreType
    ) {
        @Override
        public String uri() {
            return uri + "/";
        }
    }
}
