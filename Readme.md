<img src="docs/img/Co-funded_by_the_European_Union.jpg" width="350" height="200" alt="Co-funded by the European Union">

# Signature Gateway demo application

Demo application is provided to study the flows and integration of Signature Gateway. **NB! This is not meant to be used
in production!**

Functionality:

* Authorization
* Container conversion to and from HASHCODE form
* Mobile-ID signing
* ID card signing (with Web eID in front end)
* Smart-ID signing
* Signature validation
* ASiC container upload and augmentation

## How to set up

SiGa demo application is not a standalone system, it requires either
the [SiGa application](https://github.com/open-eid/SiGa) to already be running on your machine or pointing the demo app
towards sample APIs.

**Preconditions**:

1. **Java JDK 17** - to compile and run SiGa demo
2. **SiGa parent project** - Can be found [here](https://github.com/open-eid/SiGa)

### Option 1: Running SiGa locally with Docker

1. Docker must be installed and running.
2. Build SiGa demo application docker image:
```
./mvnw clean spring-boot:build-image
```
3. Then, follow the "Running SiGa with Docker" instructions at [SiGa webapp](https://github.com/open-eid/SiGa) to run both apps at the same
time.

If everything was successful, open up the browser at `https://siga-demo.localhost:9443/`.

### Option 2: Running standalone, against Docker Compose or an external SiGa instance

1. Build this project:

```bash
./mvnw clean install
```

2. The default
   [application.yml](https://github.com/open-eid/SiGa-demo-application/blob/master/src/main/resources/application.yml)
   does not set any of the properties below — they're either environment-specific or a secret, so every
   deployment target must supply them itself rather than inherit a bundled default. Spring Boot supports several ways
   to do this — see [Spring Boot's Externalized Configuration guide](https://docs.spring.io/spring-boot/reference/features/external-config.html)
   for the full picture. The example at point 3 uses `-D` system properties for simplicity.

| Parameter | Mandatory | Description | Example |
|---|---|---|---|
| `siga.api.uri` | Y | SiGa server URL. | `https://siga.localhost:8443` |
| `siga.api.trust-store` | Y | Truststore the demo app uses to trust the SiGa server's TLS certificate. Generated alongside the keystore below for the Docker Compose case. | `file:/path/to/SiGa/docker/tls/siga-demo/siga-demo.truststore.p12` |
| `siga.api.trust-store-password` | Y | Password for the truststore above. | `changeit` |
| `siga.api.trust-store-type` | N | Truststore format for `siga.api.trust-store` above. Defaults to the JVM's own `KeyStore.getDefaultType()` (`PKCS12` on modern JDKs) if unset — only needs overriding for a non-PKCS12 truststore. | `PKCS12` |
| `siga.client.hmac.service-uuid` | Y | HMAC client identifier issued by SiGa — see the [SiGa Authorization wiki](https://github.com/open-eid/SiGa/wiki/Authorization). | `a7fd7728-a3ea-4975-bfab-f240a67e894f` |
| `siga.client.hmac.shared-signing-key` | Y | Shared HMAC signing key for the client above. | `746573745365637265744b6579303031` |

   To point at an external or sample SiGa API instead of a local Docker Compose instance, substitute your own
   `siga.api.uri`, trust-store, and HMAC credentials for the example values above.

3. Run the built jar with the required overrides (adjust the `docker/tls/...` paths below to wherever you checked
   out the [SiGa parent project](https://github.com/open-eid/SiGa)):

```bash
java -Dserver.port=9443 \
     -Dserver.ssl.enabled=true \
     -Dserver.ssl.key-store=file:/path/to/SiGa/docker/tls/siga-demo/siga-demo.localhost.keystore.p12 \
     -Dserver.ssl.key-store-password=changeit \
     -Dserver.ssl.key-alias=siga-demo.localhost \
     -Dsiga.api.uri=https://siga.localhost:8443 \
     -Dsiga.api.trust-store=file:/path/to/SiGa/docker/tls/siga-demo/siga-demo.truststore.p12 \
     -Dsiga.api.trust-store-password=changeit \
     -Dsiga.client.hmac.service-uuid=a7fd7728-a3ea-4975-bfab-f240a67e894f \
     -Dsiga.client.hmac.shared-signing-key=746573745365637265744b6579303031 \
     -jar target/siga-demo-application-X.X.X.jar
```

Now application is accessible at https://siga-demo.localhost:9443/ (or plain `http://localhost:8080/` if
`server.port` and `server.ssl.enabled` are left unset).

### SiGa demo configuration

Example `application.yml` file can be seen [here](src/main/resources/application.yml).
Common Spring Boot properties are
described [here](https://docs.spring.io/spring-boot/appendix/application-properties/index.html).

| Parameter                                 | Mandatory | Description       | Example |
|-------------------------------------------|-----------|-------------------|---------|
| spring.servlet.multipart.max-file-size    | N         | Max file size.    | `20MB`  |
| spring.servlet.multipart.max-request-size | N         | Max request size. | `35MB`  |

## How to use

Before every signing the webpage needs to be reloaded and files uploaded.

With Docker setup, Signature Gateway is in TEST mode. Meaning it is possible to sign only with TEST ID-card, TEST
Mobile-ID or TEST Smart-ID.

* TEST ID-cards can be ordered [here](https://portal.skidsolutions.eu/order/certificates?tab=test-card).
* TEST Mobile-ID numbers can be
  found [here](https://github.com/SK-EID/MID/wiki/Test-number-for-automated-testing-in-DEMO).
* TEST Smart-ID numbers can be
  found [here](https://github.com/SK-EID/smart-id-documentation/wiki/Environment-technical-parameters#test-accounts-for-automated-testing).
