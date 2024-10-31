package ee.openeid.siga.client.service;

import ee.openeid.siga.client.configuration.SiGaDemoProperties;
import ee.openeid.siga.client.hashcode.HashcodeContainer;
import ee.openeid.siga.client.hmac.HmacTokenAuthorizationHeaderInterceptor;
import ee.openeid.siga.client.model.AsicContainerWrapper;
import ee.openeid.siga.client.model.FinalizeRemoteSigningRequest;
import ee.openeid.siga.client.model.GetContainerMobileIdSigningStatusResponse;
import ee.openeid.siga.client.model.HashcodeContainerWrapper;
import ee.openeid.siga.client.model.MobileSigningRequest;
import ee.openeid.siga.client.model.PrepareRemoteSigningRequest;
import ee.openeid.siga.client.model.PrepareRemoteSigningResponse;
import ee.openeid.siga.client.model.ProcessingStatus;
import ee.openeid.siga.client.model.SmartIdCertificateChoiceStatusResponseWrapper;
import ee.openeid.siga.client.model.SmartIdSigningRequest;
import ee.openeid.siga.webapp.json.AugmentContainerSignaturesResponse;
import ee.openeid.siga.webapp.json.CreateContainerMobileIdSigningRequest;
import ee.openeid.siga.webapp.json.CreateContainerMobileIdSigningResponse;
import ee.openeid.siga.webapp.json.CreateContainerRemoteSigningRequest;
import ee.openeid.siga.webapp.json.CreateContainerRemoteSigningResponse;
import ee.openeid.siga.webapp.json.CreateContainerRequest;
import ee.openeid.siga.webapp.json.CreateContainerResponse;
import ee.openeid.siga.webapp.json.CreateContainerSmartIdCertificateChoiceRequest;
import ee.openeid.siga.webapp.json.CreateContainerSmartIdCertificateChoiceResponse;
import ee.openeid.siga.webapp.json.CreateContainerSmartIdSigningRequest;
import ee.openeid.siga.webapp.json.CreateContainerSmartIdSigningResponse;
import ee.openeid.siga.webapp.json.CreateHashcodeContainerMobileIdSigningRequest;
import ee.openeid.siga.webapp.json.CreateHashcodeContainerMobileIdSigningResponse;
import ee.openeid.siga.webapp.json.CreateHashcodeContainerRemoteSigningRequest;
import ee.openeid.siga.webapp.json.CreateHashcodeContainerRemoteSigningResponse;
import ee.openeid.siga.webapp.json.CreateHashcodeContainerRequest;
import ee.openeid.siga.webapp.json.CreateHashcodeContainerResponse;
import ee.openeid.siga.webapp.json.CreateHashcodeContainerSmartIdCertificateChoiceRequest;
import ee.openeid.siga.webapp.json.CreateHashcodeContainerSmartIdCertificateChoiceResponse;
import ee.openeid.siga.webapp.json.CreateHashcodeContainerSmartIdSigningRequest;
import ee.openeid.siga.webapp.json.CreateHashcodeContainerSmartIdSigningResponse;
import ee.openeid.siga.webapp.json.DeleteContainerResponse;
import ee.openeid.siga.webapp.json.DeleteHashcodeContainerResponse;
import ee.openeid.siga.webapp.json.GetContainerResponse;
import ee.openeid.siga.webapp.json.GetContainerSignaturesResponse;
import ee.openeid.siga.webapp.json.GetContainerSmartIdCertificateChoiceStatusResponse;
import ee.openeid.siga.webapp.json.GetContainerSmartIdSigningStatusResponse;
import ee.openeid.siga.webapp.json.GetContainerValidationReportResponse;
import ee.openeid.siga.webapp.json.GetHashcodeContainerResponse;
import ee.openeid.siga.webapp.json.GetHashcodeContainerSignaturesResponse;
import ee.openeid.siga.webapp.json.GetHashcodeContainerValidationReportResponse;
import ee.openeid.siga.webapp.json.UpdateContainerRemoteSigningRequest;
import ee.openeid.siga.webapp.json.UpdateContainerRemoteSigningResponse;
import ee.openeid.siga.webapp.json.UpdateHashcodeContainerRemoteSigningRequest;
import ee.openeid.siga.webapp.json.UpdateHashcodeContainerRemoteSigningResponse;
import ee.openeid.siga.webapp.json.UploadContainerRequest;
import ee.openeid.siga.webapp.json.UploadContainerResponse;
import ee.openeid.siga.webapp.json.UploadHashcodeContainerRequest;
import ee.openeid.siga.webapp.json.UploadHashcodeContainerResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerBuilder;
import org.digidoc4j.ContainerOpener;
import org.digidoc4j.impl.asic.AsicContainer;
import org.digidoc4j.impl.asic.asice.AsicEContainer;
import org.digidoc4j.impl.asic.asice.AsicEContainerBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ee.openeid.siga.client.hashcode.NonHashcodeContainerValidator.assertNonHashcodeContainer;
import static ee.openeid.siga.client.hashcode.HashcodesDataFileCreator.createHashcodeDataFile;
import static java.text.MessageFormat.format;
import static org.apache.tomcat.util.codec.binary.Base64.encodeBase64String;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@Slf4j
@Service
@RequestScope
@RequiredArgsConstructor
public class SigaApiClientService {

    private static final String ASIC_ENDPOINT = "containers";
    private static final String HASHCODE_ENDPOINT = "hashcodecontainers";
    private static final String RESULT_OK = "OK";
    private static final String SIGNATURE_PROFILE_LT = "LT";
    private final ContainerService containerService;
    private final SimpMessageSendingOperations messagingTemplate;
    private final SSLContext sigaApiSslContext;
    private final RestTemplateBuilder restTemplateBuilder;
    private final SiGaDemoProperties sigaProperties;
    private RestTemplate restTemplate;
    private String websocketChannelId;

    @SneakyThrows
    @PostConstruct
    private void setUpRestTemplate() {
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sigaApiSslContext, NoopHostnameVerifier.INSTANCE);
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setSSLSocketFactory(socketFactory)
                .build();

        HttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
        SiGaDemoProperties.Hmac hmac = sigaProperties.client().hmac();
        restTemplate = restTemplateBuilder
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .interceptors(new HmacTokenAuthorizationHeaderInterceptor(sigaProperties.api().uri(), hmac.algorithm(), hmac.serviceUuid(), hmac.sharedSigningKey()))
                .errorHandler(new RestTemplateResponseErrorHandler()).build();
    }

    @Async
    @SneakyThrows
    public void startMobileSigningFlow(MobileSigningRequest mobileSigningRequest) {
        setUpClientNotificationChannel(mobileSigningRequest.getContainerId());

        if (MobileSigningRequest.ContainerType.HASHCODE == mobileSigningRequest.getContainerType()) {
            startHashcodeMobileIdSigningFlow(mobileSigningRequest);
        } else {
            startAsicMobileIdSigningFlow(mobileSigningRequest);
        }
    }

    @Async
    @SneakyThrows
    public void startSmartIdSigningFlow(SmartIdSigningRequest smartIdSigningRequest) {
        setUpClientNotificationChannel(smartIdSigningRequest.getContainerId());

        if (SmartIdSigningRequest.ContainerType.HASHCODE == smartIdSigningRequest.getContainerType()) {
            startHashcodeSmartIdSigningFlow(smartIdSigningRequest);
        } else {
            startAsicSmartIdSigningFlow(smartIdSigningRequest);
        }
    }

    private void startAsicSmartIdSigningFlow(SmartIdSigningRequest smartIdSigningRequest) {
        String containerId = smartIdSigningRequest.getContainerId();

        getSignatureList(ASIC_ENDPOINT, containerId, GetContainerSignaturesResponse.class);
        CreateContainerSmartIdCertificateChoiceRequest smartIdRequest = createAsicSmartIdRequest(smartIdSigningRequest);
        CreateContainerSmartIdCertificateChoiceResponse smartIdResponse = prepareSmartIdCertificateSelection(ASIC_ENDPOINT, smartIdRequest, containerId, CreateContainerSmartIdCertificateChoiceResponse.class);

        String generatedCertificateId = smartIdResponse.getGeneratedCertificateId();
        if (StringUtils.isNotBlank(generatedCertificateId)) {
            startAsicSmartIdSigning(containerId, generatedCertificateId);
        }
    }

    private void startAsicSmartIdSigning(String containerId, String generatedCertificateId) {
        SmartIdCertificateChoiceStatusResponseWrapper wrapper = getSmartIdCertificateSelectionStatus(ASIC_ENDPOINT, containerId, generatedCertificateId);
        if (!wrapper.isPollingSuccess()) {
            return;
        }

        String documentNumber = wrapper.getResponse().getDocumentNumber();
        CreateContainerSmartIdSigningRequest smartIdSignatureSigningRequest = createSmartIdSigningRequest(documentNumber);
        CreateContainerSmartIdSigningResponse smartIdSignatureSigningResponse = prepareSmartIdSignatureSigning(ASIC_ENDPOINT, smartIdSignatureSigningRequest, containerId, CreateContainerSmartIdSigningResponse.class);
        String generatedSignatureId = smartIdSignatureSigningResponse.getGeneratedSignatureId();

        if (StringUtils.isNotBlank(generatedSignatureId)) {
            if (getSmartIdSigningStatus(ASIC_ENDPOINT, containerId, generatedSignatureId)) {
                endAsicContainerFlow(containerId);
            }
        }
    }

    private void startHashcodeMobileIdSigningFlow(MobileSigningRequest mobileSigningRequest) {
        String containerId = mobileSigningRequest.getContainerId();

        getSignatureList(HASHCODE_ENDPOINT, containerId, GetHashcodeContainerSignaturesResponse.class);
        CreateHashcodeContainerMobileIdSigningRequest mobileIdRequest = createHashcodeMobileIdRequest(mobileSigningRequest);
        CreateHashcodeContainerMobileIdSigningResponse mobileIdResponse =
                prepareMobileIdSignatureSigning(HASHCODE_ENDPOINT, containerId, CreateHashcodeContainerMobileIdSigningResponse.class, mobileIdRequest);

        String generatedSignatureId = mobileIdResponse.getGeneratedSignatureId();
        if (StringUtils.isNotBlank(generatedSignatureId)) {
            if (getMobileSigningStatus(HASHCODE_ENDPOINT, containerId, generatedSignatureId)) {
                endHashcodeContainerFlow(containerId);
            }
        }
    }

    private void endHashcodeContainerFlow(String containerId) {
        getContainerValidation(HASHCODE_ENDPOINT, containerId, GetHashcodeContainerValidationReportResponse.class);
        GetHashcodeContainerResponse getContainerResponse = getContainer(HASHCODE_ENDPOINT, containerId, GetHashcodeContainerResponse.class);
        HashcodeContainerWrapper container = containerService.getHashcodeContainer(containerId);
        containerService.cacheHashcodeContainer(containerId, container.getFileName(), Base64.getDecoder().decode(getContainerResponse.getContainer()), container.getOriginalDataFiles());
        deleteContainer(HASHCODE_ENDPOINT, containerId, DeleteHashcodeContainerResponse.class);
    }

    private void startAsicMobileIdSigningFlow(MobileSigningRequest mobileSigningRequest) {
        String containerId = mobileSigningRequest.getContainerId();

        getSignatureList(ASIC_ENDPOINT, containerId, GetContainerSignaturesResponse.class);
        CreateContainerMobileIdSigningRequest mobileIdRequest = createAsicMobileIdRequest(mobileSigningRequest);
        CreateContainerMobileIdSigningResponse mobileIdResponse =
                prepareMobileIdSignatureSigning(ASIC_ENDPOINT, containerId, CreateContainerMobileIdSigningResponse.class, mobileIdRequest);

        String generatedSignatureId = mobileIdResponse.getGeneratedSignatureId();
        if (StringUtils.isNotBlank(generatedSignatureId)) {
            if (getMobileSigningStatus(ASIC_ENDPOINT, containerId, generatedSignatureId)) {
                endAsicContainerFlow(containerId);
            }
        }
    }

    private void endAsicContainerFlow(String containerId) {
        getContainerValidation(ASIC_ENDPOINT, containerId, GetContainerValidationReportResponse.class);
        GetContainerResponse getContainerResponse = getContainer(ASIC_ENDPOINT, containerId, GetContainerResponse.class);
        AsicContainerWrapper container = containerService.getAsicContainer(containerId);
        containerService.cacheAsicContainer(containerId, container.getName(), Base64.getDecoder().decode(getContainerResponse.getContainer()));
        deleteContainer(ASIC_ENDPOINT, containerId, DeleteContainerResponse.class);
    }

    private void setUpClientNotificationChannel(String fileId) {
        websocketChannelId = "/progress/" + fileId;
    }

    public PrepareRemoteSigningResponse prepareRemoteSigning(PrepareRemoteSigningRequest prepareRemoteSigningRequest) {
        setUpClientNotificationChannel(prepareRemoteSigningRequest.getContainerId());

        if (MobileSigningRequest.ContainerType.HASHCODE == prepareRemoteSigningRequest.getContainerType()) {
            return prepareHashcodeContainerRemoteSigning(prepareRemoteSigningRequest);
        } else {
            return prepareAsicContainerRemoteSigning(prepareRemoteSigningRequest);
        }
    }

    private PrepareRemoteSigningResponse prepareHashcodeContainerRemoteSigning(PrepareRemoteSigningRequest prepareRemoteSigningRequest) {
        String containerId = prepareRemoteSigningRequest.getContainerId();

        getSignatureList(HASHCODE_ENDPOINT, containerId, GetContainerSignaturesResponse.class);

        CreateHashcodeContainerRemoteSigningRequest remoteSigningRequest = createHashcodeContainerRemoteSigningRequest(prepareRemoteSigningRequest);
        CreateHashcodeContainerRemoteSigningResponse remoteSigningResponse = prepareContainerRemoteSigning(HASHCODE_ENDPOINT, containerId, CreateHashcodeContainerRemoteSigningResponse.class, remoteSigningRequest);

        return PrepareRemoteSigningResponse.from(remoteSigningResponse);
    }

    private CreateHashcodeContainerRemoteSigningRequest createHashcodeContainerRemoteSigningRequest(PrepareRemoteSigningRequest prepareRemoteSigningRequest) {
        CreateHashcodeContainerRemoteSigningRequest request = new CreateHashcodeContainerRemoteSigningRequest();
        request.setSigningCertificate(prepareRemoteSigningRequest.getCertificate());
        request.setSignatureProfile(SIGNATURE_PROFILE_LT);
        return request;
    }

    private PrepareRemoteSigningResponse prepareAsicContainerRemoteSigning(PrepareRemoteSigningRequest prepareRemoteSigningRequest) {
        String containerId = prepareRemoteSigningRequest.getContainerId();

        getSignatureList(ASIC_ENDPOINT, containerId, GetContainerSignaturesResponse.class);

        CreateContainerRemoteSigningRequest remoteSigningRequest = createAsicContainerRemoteSigningRequest(prepareRemoteSigningRequest);
        CreateContainerRemoteSigningResponse remoteSigningResponse = prepareContainerRemoteSigning(ASIC_ENDPOINT, containerId, CreateContainerRemoteSigningResponse.class, remoteSigningRequest);

        return PrepareRemoteSigningResponse.from(remoteSigningResponse);
    }

    private CreateContainerRemoteSigningRequest createAsicContainerRemoteSigningRequest(PrepareRemoteSigningRequest prepareRemoteSigningRequest) {
        CreateContainerRemoteSigningRequest request = new CreateContainerRemoteSigningRequest();
        request.setSigningCertificate(prepareRemoteSigningRequest.getCertificate());
        request.setSignatureProfile(SIGNATURE_PROFILE_LT);
        return request;
    }

    private <T> T prepareContainerRemoteSigning(String containerEndpoint, String containerId, Class<T> clazz, Object request) {
        String endpoint = getSigaApiUri(containerEndpoint, containerId, "remotesigning");
        T response = restTemplate.postForObject(endpoint, request, clazz);
        sendStatus(POST, endpoint, request, response);
        return response;
    }

    public void finalizeRemoteSigning(FinalizeRemoteSigningRequest finalizeRemoteSigningRequest) {
        setUpClientNotificationChannel(finalizeRemoteSigningRequest.getContainerId());

        if (MobileSigningRequest.ContainerType.HASHCODE == finalizeRemoteSigningRequest.getContainerType()) {
            finalizeHashcodeContainerRemoteSigning(finalizeRemoteSigningRequest);
        } else {
            finalizeAsicContainerRemoteSigning(finalizeRemoteSigningRequest);
        }
    }

    private void finalizeHashcodeContainerRemoteSigning(FinalizeRemoteSigningRequest finalizeRemoteSigningRequest) {
        String containerId = finalizeRemoteSigningRequest.getContainerId();

        UpdateHashcodeContainerRemoteSigningRequest remoteSigningRequest = new UpdateHashcodeContainerRemoteSigningRequest();
        remoteSigningRequest.setSignatureValue(encodeBase64String(finalizeRemoteSigningRequest.getSignature()));

        HttpEntity<UpdateHashcodeContainerRemoteSigningRequest> request = new HttpEntity<>(remoteSigningRequest);

        UpdateHashcodeContainerRemoteSigningResponse response = finalizeContainerRemoteSignature(HASHCODE_ENDPOINT,
                containerId, finalizeRemoteSigningRequest.getSignatureId(), UpdateHashcodeContainerRemoteSigningResponse.class, request);

        if (RESULT_OK.equals(response.getResult())) {
            endHashcodeContainerFlow(containerId);
        }
    }

    private void finalizeAsicContainerRemoteSigning(FinalizeRemoteSigningRequest finalizeRemoteSigningRequest) {
        String containerId = finalizeRemoteSigningRequest.getContainerId();

        UpdateContainerRemoteSigningRequest remoteSigningRequest = new UpdateContainerRemoteSigningRequest();
        remoteSigningRequest.setSignatureValue(encodeBase64String(finalizeRemoteSigningRequest.getSignature()));

        HttpEntity<UpdateContainerRemoteSigningRequest> request = new HttpEntity<>(remoteSigningRequest);

        UpdateContainerRemoteSigningResponse response = finalizeContainerRemoteSignature(ASIC_ENDPOINT,
                containerId, finalizeRemoteSigningRequest.getSignatureId(), UpdateContainerRemoteSigningResponse.class, request);

        if (RESULT_OK.equals(response.getResult())) {
            endAsicContainerFlow(containerId);
        }
    }

    private <T> T finalizeContainerRemoteSignature(String containerEndpoint, String containerId, String generatedSignatureId, Class<T> clazz, HttpEntity<?> request) {
        String endpoint = getSigaApiUri(containerEndpoint, containerId, "remotesigning", generatedSignatureId);
        T response = restTemplate.exchange(endpoint, PUT, request, clazz).getBody();
        sendStatus(PUT, endpoint, request.getBody(), response);
        return response;
    }

    @SneakyThrows
    public AsicContainerWrapper createAsicContainer(Collection<MultipartFile> files) {
        CreateContainerRequest request = new CreateContainerRequest();
        for (MultipartFile file : files) {
            log.info("Processing file: {}", file.getOriginalFilename());
            ee.openeid.siga.webapp.json.DataFile dataFile = new ee.openeid.siga.webapp.json.DataFile();
            dataFile.setFileContent(new String(Base64.getEncoder().encode(file.getBytes())));
            dataFile.setFileName(file.getOriginalFilename());
            request.getDataFiles().add(dataFile);
        }
        request.setContainerName("container.asice");
        CreateContainerResponse createContainerResponse = restTemplate.postForObject(fromUriString(sigaProperties.api().uri()).path("containers").build().toUriString(), request, CreateContainerResponse.class);
        String containerId = createContainerResponse.getContainerId();
        GetContainerResponse getContainerResponse = restTemplate.getForObject(getSigaApiUri(ASIC_ENDPOINT, containerId), GetContainerResponse.class);
        return containerService.cacheAsicContainer(containerId, getContainerResponse.getContainerName(), getContainerResponse.getContainer().getBytes());
    }

    @SneakyThrows
    public HashcodeContainerWrapper createHashcodeContainer(Collection<MultipartFile> files) {
        CreateHashcodeContainerRequest request = new CreateHashcodeContainerRequest();
        Map<String, byte[]> originalDataFiles = new HashMap<>();
        for (MultipartFile file : files) {
            log.info("Processing file: {}", file.getOriginalFilename());
            request.getDataFiles().add(createHashcodeDataFile(file.getOriginalFilename(), file.getSize(), file.getBytes()).convertToRequest());
            originalDataFiles.put(file.getOriginalFilename(), file.getBytes());
        }
        CreateHashcodeContainerResponse createContainerResponse = restTemplate.postForObject(fromUriString(sigaProperties.api().uri()).path("hashcodecontainers").build().toUriString(), request, CreateHashcodeContainerResponse.class);
        String containerId = createContainerResponse.getContainerId();
        GetHashcodeContainerResponse getContainerResponse = restTemplate.getForObject(getSigaApiUri(HASHCODE_ENDPOINT, containerId), GetHashcodeContainerResponse.class);
        log.info("Created container with id {}", containerId);
        return containerService.cacheHashcodeContainer(containerId, containerId + ".asice", Base64.getDecoder().decode(getContainerResponse.getContainer()), originalDataFiles);
    }

    @SneakyThrows
    public HashcodeContainerWrapper convertAndUploadHashcodeContainer(Map<String, MultipartFile> fileMap) {
        MultipartFile file = fileMap.entrySet().iterator().next().getValue();
        HashcodeContainer hashcodeContainer = convertToHashcodeContainer(file);
        UploadHashcodeContainerResponse response = uploadHashcodeContainer(hashcodeContainer);

        String containerId = response.getContainerId();
        GetHashcodeContainerResponse getContainerResponse = restTemplate.getForObject(getSigaApiUri(HASHCODE_ENDPOINT, containerId), GetHashcodeContainerResponse.class);
        log.info("Uploaded hashcode container with id {}", containerId);
        return containerService.cacheHashcodeContainer(containerId, file.getOriginalFilename(), Base64.getDecoder().decode(getContainerResponse.getContainer()), hashcodeContainer.getRegularDataFiles());
    }

    @SneakyThrows
    public AsicContainerWrapper uploadAsicContainer(Map<String, MultipartFile> fileMap) {
        MultipartFile file = fileMap.entrySet().iterator().next().getValue();
        String containerName = file.getOriginalFilename();
        byte[] container = file.getBytes();
        assertNonHashcodeContainer(container);
        UploadContainerResponse response = uploadContainer(container, containerName);

        String containerId = response.getContainerId();
        GetContainerResponse getContainerResponse = restTemplate.getForObject(getSigaApiUri(ASIC_ENDPOINT, containerId), GetContainerResponse.class);
        log.info("Uploaded container with id {}", containerId);
        return containerService.cacheAsicContainer(containerId, getContainerResponse.getContainerName(), getContainerResponse.getContainer().getBytes());
    }

    private HashcodeContainer convertToHashcodeContainer(MultipartFile file) throws IOException {
        log.info("Converting container: {}", file.getOriginalFilename());
        return HashcodeContainer.fromRegularContainerBuilder()
                .container(file.getBytes())
                .build();
    }

    private UploadHashcodeContainerResponse uploadHashcodeContainer(HashcodeContainer hashcodeContainer) {
        String endpoint = fromUriString(sigaProperties.api().uri()).path("upload/hashcodecontainers").build().toUriString();
        String encodedContainerContent = encodeBase64String(hashcodeContainer.getHashcodeContainer());
        UploadHashcodeContainerRequest request = new UploadHashcodeContainerRequest();
        request.setContainer(encodedContainerContent);
        return restTemplate.postForObject(endpoint, request, UploadHashcodeContainerResponse.class);
    }

    private UploadContainerResponse uploadContainer(byte[] container, String containerName) {
        String endpoint = fromUriString(sigaProperties.api().uri()).path("upload/containers").build().toUriString();
        String encodedContainerContent = encodeBase64String(container);
        UploadContainerRequest request = new UploadContainerRequest();
        request.setContainer(encodedContainerContent);
        request.setContainerName(containerName);
        return restTemplate.postForObject(endpoint, request, UploadContainerResponse.class);
    }

    private <T> void getSignatureList(String containerEndpoint, String containerId, Class<T> clazz) {
        String endpoint = getSigaApiUri(containerEndpoint, containerId, "signatures");
        T response = restTemplate.getForObject(endpoint, clazz);
        sendStatus(GET, endpoint, response);
    }

    private <T> T prepareMobileIdSignatureSigning(String containerEndpoint, String containerId, Class<T> clazz, Object request) {
        String endpoint = getSigaApiUri(containerEndpoint, containerId, "mobileidsigning");
        T response = restTemplate.postForObject(endpoint, request, clazz);
        sendStatus(POST, endpoint, request, response);
        return response;
    }

    private <T> T prepareSmartIdSignatureSigning(String url, Object request, String containerId, Class<T> responseType) {
        String endpoint = getSigaApiUri(url, containerId, "smartidsigning");
        T response = restTemplate.postForObject(endpoint, request, responseType);
        sendStatus(POST, endpoint, request, response);
        return response;
    }

    @SneakyThrows
    private boolean getMobileSigningStatus(String containerEndpoint, String containerId, String generatedSignatureId) {
        String endpoint = getSigaApiUri(containerEndpoint, containerId, "mobileidsigning", generatedSignatureId, "status");
        GetContainerMobileIdSigningStatusResponse response;
        for (int i = 0; i < 6; i++) {
            response = restTemplate.getForObject(endpoint, GetContainerMobileIdSigningStatusResponse.class);
            sendStatus(GET, endpoint, response);
            if (!"SIGNATURE".equals(response.getMidStatus())) {
                Thread.sleep(5000);
            } else {
                return true;
            }
        }
        return false;
    }

    private <T> void getContainerValidation(String containerEndpoint, String containerId, Class<T> clazz) {
        String endpoint = getSigaApiUri(containerEndpoint, containerId, "validationreport");
        T response = restTemplate.getForObject(endpoint, clazz);
        sendStatus(GET, endpoint, response);
    }

    private <T> T getContainer(String containerEndpoint, String containerId, Class<T> clazz) {
        String endpoint = getSigaApiUri(containerEndpoint, containerId);
        T response = restTemplate.getForObject(endpoint, clazz);

        ProcessingStatus processingStatus = ProcessingStatus.builder()
                .containerReadyForDownload(true)
                .requestMethod(GET.name())
                .apiEndpoint(endpoint)
                .apiResponseObject(response).build();
        messagingTemplate.convertAndSend(websocketChannelId, processingStatus);

        return response;
    }

    private <T> void deleteContainer(String containerEndpoint, String containerId, Class<T> clazz) {
        String endpoint = getSigaApiUri(containerEndpoint, containerId);
        ResponseEntity<T> response = restTemplate.exchange(endpoint, DELETE, null, clazz);
        sendStatus(DELETE, endpoint, response.getStatusCode());
    }

    private void sendError(String message, String... messageArgs) {
        ProcessingStatus processingStatus = ProcessingStatus.builder().errorMessage(format(message, (Object[]) messageArgs)).build();
        messagingTemplate.convertAndSend(websocketChannelId, processingStatus);
    }

    private void sendStatus(HttpMethod requestMethod, String apiEndpoint, Object apiResponseObj) {
        sendStatus(requestMethod, apiEndpoint, null, apiResponseObj);
    }

    private void sendStatus(HttpMethod requestMethod, String apiEndpoint, Object apiRequestObj, Object apiResponseObj) {
        ProcessingStatus processingStatus = ProcessingStatus.builder()
                .requestMethod(requestMethod.name())
                .apiEndpoint(apiEndpoint)
                .apiRequestObject(apiRequestObj)
                .apiResponseObject(apiResponseObj).build();
        messagingTemplate.convertAndSend(websocketChannelId, processingStatus);
    }

    private String getSigaApiUri(String containerPath, String... pathSegments) {
        return fromUriString(sigaProperties.api().uri()).path(containerPath).pathSegment(pathSegments).build().toUriString();
    }

    private CreateHashcodeContainerMobileIdSigningRequest createHashcodeMobileIdRequest(MobileSigningRequest mobileSigningRequest) {
        CreateHashcodeContainerMobileIdSigningRequest request = new CreateHashcodeContainerMobileIdSigningRequest();
        request.setMessageToDisplay("SiGa DEMO app");
        request.setSignatureProfile(SIGNATURE_PROFILE_LT);
        request.setPersonIdentifier(mobileSigningRequest.getPersonIdentifier());
        request.setLanguage("EST");
        request.setPhoneNo(mobileSigningRequest.getPhoneNr());
        return request;
    }

    private CreateContainerMobileIdSigningRequest createAsicMobileIdRequest(MobileSigningRequest mobileSigningRequest) {
        CreateContainerMobileIdSigningRequest request = new CreateContainerMobileIdSigningRequest();
        request.setMessageToDisplay("SiGa DEMO app");
        request.setSignatureProfile(SIGNATURE_PROFILE_LT);
        request.setPersonIdentifier(mobileSigningRequest.getPersonIdentifier());
        request.setLanguage("EST");
        request.setPhoneNo(mobileSigningRequest.getPhoneNr());
        return request;
    }

    private CreateHashcodeContainerSmartIdCertificateChoiceRequest createHashcodeSmartIdRequest(SmartIdSigningRequest smartIdSigningRequest) {
        CreateHashcodeContainerSmartIdCertificateChoiceRequest request = new CreateHashcodeContainerSmartIdCertificateChoiceRequest();
        request.setPersonIdentifier(smartIdSigningRequest.getPersonIdentifier());
        request.setCountry(smartIdSigningRequest.getCountry());
        return request;
    }

    private CreateContainerSmartIdCertificateChoiceRequest createAsicSmartIdRequest(SmartIdSigningRequest smartIdSigningRequest) {
        CreateContainerSmartIdCertificateChoiceRequest request = new CreateContainerSmartIdCertificateChoiceRequest();
        request.setPersonIdentifier(smartIdSigningRequest.getPersonIdentifier());
        request.setCountry(smartIdSigningRequest.getCountry());
        return request;
    }

    private void startHashcodeSmartIdSigningFlow(SmartIdSigningRequest smartIdSigningRequest) {
        String containerId = smartIdSigningRequest.getContainerId();

        getSignatureList(HASHCODE_ENDPOINT, containerId, GetHashcodeContainerSignaturesResponse.class);
        CreateHashcodeContainerSmartIdCertificateChoiceRequest smartIdRequest = createHashcodeSmartIdRequest(smartIdSigningRequest);
        CreateHashcodeContainerSmartIdCertificateChoiceResponse smartIdResponse = prepareSmartIdCertificateSelection(HASHCODE_ENDPOINT, smartIdRequest, containerId, CreateHashcodeContainerSmartIdCertificateChoiceResponse.class);

        String generatedCertificateId = smartIdResponse.getGeneratedCertificateId();
        if (StringUtils.isNotBlank(generatedCertificateId)) {
            startHashcodeSmartIdSigning(containerId, generatedCertificateId);
        }
    }

    private <T> T prepareSmartIdCertificateSelection(String url, Object request, String containerId, Class<T> responseType) {
        String endpoint = getSigaApiUri(url, containerId, "smartidsigning/certificatechoice");
        T response = restTemplate.postForObject(endpoint, request, responseType);
        sendStatus(POST, endpoint, request, response);
        return response;
    }

    private void startHashcodeSmartIdSigning(String containerId, String generatedCertificateId) {
        SmartIdCertificateChoiceStatusResponseWrapper wrapper = getSmartIdCertificateSelectionStatus(HASHCODE_ENDPOINT, containerId, generatedCertificateId);
        if (!wrapper.isPollingSuccess()) {
            return;
        }

        String documentNumber = wrapper.getResponse().getDocumentNumber();
        CreateHashcodeContainerSmartIdSigningRequest smartIdSignatureSigningRequest = createHashcodeSmartIdSigningRequest(documentNumber);
        CreateHashcodeContainerSmartIdSigningResponse smartIdSignatureSigningResponse = prepareSmartIdSignatureSigning(HASHCODE_ENDPOINT, smartIdSignatureSigningRequest, containerId, CreateHashcodeContainerSmartIdSigningResponse.class);

        String generatedSignatureId = smartIdSignatureSigningResponse.getGeneratedSignatureId();

        if (StringUtils.isNotBlank(generatedSignatureId)) {
            if (getSmartIdSigningStatus(HASHCODE_ENDPOINT, containerId, generatedSignatureId)) {
                endHashcodeContainerFlow(containerId);
            }
        }
    }

    @SneakyThrows
    private SmartIdCertificateChoiceStatusResponseWrapper getSmartIdCertificateSelectionStatus(String containerEndpoint, String containerId, String generatedSignatureId) {
        String endpoint = getSigaApiUri(containerEndpoint, containerId, "smartidsigning/certificatechoice", generatedSignatureId, "status");
        SmartIdCertificateChoiceStatusResponseWrapper wrapper = new SmartIdCertificateChoiceStatusResponseWrapper();
        GetContainerSmartIdCertificateChoiceStatusResponse response;
        for (int i = 0; i < 18; i++) {
            response = restTemplate.getForObject(endpoint, GetContainerSmartIdCertificateChoiceStatusResponse.class);
            sendStatus(GET, endpoint, response);
            if (!"CERTIFICATE".equals(response.getSidStatus())) {
                Thread.sleep(5000);
            } else {
                wrapper.setPollingSuccess(true);
                wrapper.setResponse(response);
                return wrapper;
            }
        }
        wrapper.setPollingSuccess(false);
        return wrapper;
    }

    private CreateHashcodeContainerSmartIdSigningRequest createHashcodeSmartIdSigningRequest(String documentNumber) {
        CreateHashcodeContainerSmartIdSigningRequest request = new CreateHashcodeContainerSmartIdSigningRequest();
        request.setMessageToDisplay("SiGa DEMO app");
        request.setSignatureProfile(SIGNATURE_PROFILE_LT);
        request.setDocumentNumber(documentNumber);
        return request;
    }

    private CreateContainerSmartIdSigningRequest createSmartIdSigningRequest(String documentNumber) {
        CreateContainerSmartIdSigningRequest request = new CreateContainerSmartIdSigningRequest();
        request.setMessageToDisplay("SiGa DEMO app");
        request.setSignatureProfile(SIGNATURE_PROFILE_LT);
        request.setDocumentNumber(documentNumber);
        return request;
    }

    @SneakyThrows
    private boolean getSmartIdSigningStatus(String containerEndpoint, String containerId, String generatedSignatureId) {
        String endpoint = getSigaApiUri(containerEndpoint, containerId, "smartidsigning", generatedSignatureId, "status");
        GetContainerSmartIdSigningStatusResponse response;
        for (int i = 0; i < 6; i++) {
            response = restTemplate.getForObject(endpoint, GetContainerSmartIdSigningStatusResponse.class);
            sendStatus(GET, endpoint, response);
            if (!"SIGNATURE".equals(response.getSidStatus())) {
                Thread.sleep(5000);
            } else {
                return true;
            }
        }
        return false;
    }

    class RestTemplateResponseErrorHandler implements ResponseErrorHandler {

        @Override
        public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
            log.info("HttpResponse: {}, {}", httpResponse.getStatusCode(), httpResponse.getStatusText());
            return !httpResponse.getStatusCode().is2xxSuccessful();
        }

        @Override
        public void handleError(ClientHttpResponse httpResponse) throws IOException {
            try {
                sendError(format("Unable to process container: {0}, {1}", httpResponse.getStatusCode(), httpResponse.getStatusText()));
            } catch (RuntimeException ex) {
                // happens when websocket is not yet initaited
                // propagate http error to client
                throw new HttpClientErrorException(
                    httpResponse.getStatusCode(), 
                    new String(httpResponse.getBody().readAllBytes())
                );
            }
        }
    }
}
