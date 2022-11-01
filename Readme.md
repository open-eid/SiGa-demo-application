![EU Regional Development Fund](docs/img/EL_Regionaalarengu_Fond_horisontaalne-vaike.jpeg)

# Signature Gateway demo application

Demo application is provided to study the flows and integration of Signature Gateway. **NB! This is not meant to be used in production!**

Functionality:

* Authorization
* Container conversion to and from HASHCODE form
* Mobile-ID signing
* ID card signing (with HWCrypto in front end)
* Smart-ID signing
* Signature validation

## How to set up

SiGa demo application is not a standalone system, it requires either the [SiGa application](https://github.com/open-eid/SiGa) to already be running on your machine or pointing the demo app towards sample APIs.

### Option 1: Running with SiGa locally

To build the docker image run the following command:

```
./mvnw spring-boot:build-image
```

Then, follow the Docker instructions at [SiGa webapp](https://github.com/open-eid/SiGa) to run both apps at the same time.

If everything was successful, open up the browser at https://localhost:9443.

### Option 2: Running with external APIs

To run the application with external APIs, open up the [application.properties](https://github.com/open-eid/SiGa-demo-application/blob/master/src/main/resources/application.properties) file and change the following properties accordingly:

```
siga.api.uri=https://siga.localhost:8443/siga
siga.api.trustStore=classpath:siga_server_truststore.p12
siga.api.trustStorePassword=changeit
siga.client.hmac.algorithm=HmacSHA256
siga.client.hmac.service-uuid=a7fd7728-a3ea-4975-bfab-f240a67e894f
siga.client.hmac.shared-signing-key=746573745365637265744b6579303031
```

## How to use

With Docker setup, Signature Gateway is in TEST mode. Meaning it is possible to sign only with TEST ID-card, TEST Mobile-ID or TEST Smart-ID.

* TEST ID-cards can be ordered [here](https://www.skidsolutions.eu/teenused/testkaardid/). 
* TEST Mobile-ID numbers can be found [here](https://github.com/SK-EID/MID/wiki/Test-number-for-automated-testing-in-DEMO).
* TEST Smart-ID numbers can be found [here](https://github.com/SK-EID/smart-id-documentation/wiki/Environment-technical-parameters#test-accounts-for-automated-testing).
