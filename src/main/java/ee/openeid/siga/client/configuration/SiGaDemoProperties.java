package ee.openeid.siga.client.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "siga")
public record SiGaDemoProperties(SigaClient client, SigaApi api) {

    @ConfigurationProperties(prefix = "siga.client")
    public record SigaClient(Hmac hmac) {
    }

    @ConfigurationProperties(prefix = "siga.client.hmac")
    public record Hmac(String algorithm, String serviceUuid, String sharedSigningKey) {
    }

    @ConfigurationProperties(prefix = "siga.api")
    public record SigaApi(String uri, String trustStore, String trustStorePassword) {
        @Override
        public String uri() {
            return uri + "/";
        }
    }
}
