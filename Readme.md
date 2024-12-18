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

### Option 2: Running with external APIs

1. Open up the
   [application.properties](https://github.com/open-eid/SiGa-demo-application/blob/master/src/main/resources/application.properties)
   file and change the following properties accordingly:

```
siga.api.uri=https://siga.localhost:8443/siga
siga.api.trust-store=file:/path/to/siga_server_truststore.p12
siga.api.trust-store-password=changeit
siga.api.trust-store-type=PKCS12
siga.client.hmac.algorithm=HmacSHA256
siga.client.hmac.service-uuid=a7fd7728-a3ea-4975-bfab-f240a67e894f
siga.client.hmac.shared-signing-key=746573745365637265744b6579303031
```

| Parameter           | Description | Example |
|---------------------|-------------|---------|
| siga.api.uri        | SIGA server URL (without slash symbol in the end) | `https://siga.localhost:8443/siga` |
| siga.api.trust-store | Location of the trustore containing server's certificate or CA (path without quotes symbol) | `classpath:siga_server_truststore.p12` or `file:/path/to/siga_server_truststore.p12` |
| siga.api.trust-store-password | Password of the trustore containing server's certificate or CA. | `changeit` |
| siga.api.trust-store-type | Type of the trustore containing server's certificate or CA. Defaults to system default if not provided. | `PKCS12` |
| siga.client.hmac.algorithm | More info can be found [here](https://github.com/open-eid/SiGa/wiki/Authorization) | `HmacSHA256` |
| siga.client.hmac.service-uuid | More info can be found [here](https://github.com/open-eid/SiGa/wiki/Authorization) | `a7fd7728-a3ea-4975-bfab-f240a67e894f` |
| siga.client.hmac.shared-signing-key | More info can be found [here](https://github.com/open-eid/SiGa/wiki/Authorization) | `746573745365637265744b6579303031` |

2. Build this project

```bash
./mvnw clean install
```

3. Run compiled JAR (found in target folder)

```bash
java -jar siga-demo-application-X.X.X.jar
```

Now application is accessible at https://siga-demo.localhost:9443/.

### SiGa demo configuration

Example `application.properties` file can be seen [here](src/main/resources/application.properties).
Common Spring Boot properties are
described [here](https://docs.spring.io/spring-boot/docs/2.7.8/reference/html/application-properties.html).

| Parameter                                 | Mandatory | Description       | Example |
|-------------------------------------------|-----------|-------------------|---------|
| spring.servlet.multipart.max-file-size    | N         | Max file size.    | `20MB`  |
| spring.servlet.multipart.max-request-size | N         | Max request size. | `35MB`  |

## How to use

Before every signing the webapage needs to be reloaded and files uploaded.

With Docker setup, Signature Gateway is in TEST mode. Meaning it is possible to sign only with TEST ID-card, TEST
Mobile-ID or TEST Smart-ID.

* TEST ID-cards can be ordered [here](https://portal.skidsolutions.eu/order/certificates?tab=test-card).
* TEST Mobile-ID numbers can be
  found [here](https://github.com/SK-EID/MID/wiki/Test-number-for-automated-testing-in-DEMO).
* TEST Smart-ID numbers can be
  found [here](https://github.com/SK-EID/smart-id-documentation/wiki/Environment-technical-parameters#test-accounts-for-automated-testing).
