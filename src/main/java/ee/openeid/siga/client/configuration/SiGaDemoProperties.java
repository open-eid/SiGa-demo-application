package ee.openeid.siga.client.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "siga")
public record SiGaDemoProperties(SigaClient client, SigaApi api) {

    @ConstructorBinding
    @ConfigurationProperties(prefix = "siga.client")
    public record SigaClient(Hmac hmac) {
    }

    @ConstructorBinding
    @ConfigurationProperties(prefix = "siga.client.hmac")
    public record Hmac(String algorithm, String serviceUuid, String sharedSigningKey) {
    }

    @ConstructorBinding
    @ConfigurationProperties(prefix = "siga.api")
    public record SigaApi(String uri, String trustStore, String trustStorePassword) {
        @Override
        public String uri() {
            return uri + "/";
        }
    }
}
