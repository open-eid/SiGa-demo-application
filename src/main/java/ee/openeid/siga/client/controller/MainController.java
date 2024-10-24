package ee.openeid.siga.client.controller;

import ee.openeid.siga.client.hashcode.HashcodeContainer;
import ee.openeid.siga.client.model.AsicContainerWrapper;
import ee.openeid.siga.client.model.AugmentationRequest;
import ee.openeid.siga.client.model.FinalizeRemoteSigningRequest;
import ee.openeid.siga.client.model.HashcodeContainerWrapper;
import ee.openeid.siga.client.model.MobileSigningRequest;
import ee.openeid.siga.client.model.PrepareRemoteSigningRequest;
import ee.openeid.siga.client.model.SmartIdSigningRequest;
import ee.openeid.siga.client.service.ContainerService;
import ee.openeid.siga.client.service.SigaApiClientService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Map;


@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {
    private static final String START_PAGE_VIEW_NAME = "index";
    private final SigaApiClientService sigaApiClientService;
    private final ContainerService containerService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String startPage(final Model model) {
        return START_PAGE_VIEW_NAME;
    }

    @PostMapping(value = "/convert-container", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public HashcodeContainerWrapper convertContainerToHashcodeContainer(MultipartHttpServletRequest request) {
        Map<String, MultipartFile> fileMap = request.getFileMap();
        log.info("Number of files uploaded: {}", fileMap.size());
        return sigaApiClientService.convertAndUploadHashcodeContainer(fileMap);
    }

    @PostMapping(value = "/create-hashcode-container", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    @SneakyThrows
    public HashcodeContainerWrapper createHashcodeContainerFromFiles(MultipartHttpServletRequest request) {
        Map<String, MultipartFile> fileMap = request.getFileMap();
        log.info("Number of files uploaded: {}", fileMap.size());
        return sigaApiClientService.createHashcodeContainer(fileMap.values());
    }

    @PostMapping(value = "/create-container", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    @SneakyThrows
    public AsicContainerWrapper createContainerFromFiles(MultipartHttpServletRequest request) {
        Map<String, MultipartFile> fileMap = request.getFileMap();
        log.info("Number of files uploaded: {}", fileMap.size());
        return sigaApiClientService.createAsicContainer(fileMap.values());
    }

    @PostMapping(value = "/upload-container", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    @SneakyThrows
    public AsicContainerWrapper uploadAsicContainer(MultipartHttpServletRequest request) {
        Map<String, MultipartFile> fileMap = request.getFileMap();
        log.info("Number of files uploaded: {}", fileMap.size());
        return sigaApiClientService.uploadAsicContainer(fileMap);
    }

    @GetMapping(value = "/download/hashcode/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity downloadHashcodeContainer(@PathVariable("id") String id) {
        HashcodeContainerWrapper cachedFile = containerService.getHashcodeContainer(id);
        log.info("Downloading hashcode container {} with id {}", cachedFile.getFileName(), cachedFile.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cachedFile.getFileName() + "\"")
                .body(cachedFile.getHashcodeContainer());
    }

    @GetMapping(value = "/download/regular/{type}/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public ResponseEntity downloadRegularContainer(@PathVariable("type") String type, @PathVariable("id") String id) {
        String fileName;
        byte[] container;
        if ("ASIC".equals(type)) {
            AsicContainerWrapper cachedFile = containerService.getAsicContainer(id);
            container = cachedFile.getContainer();
            fileName = cachedFile.getName();
        } else {
            HashcodeContainerWrapper cachedFile = containerService.getHashcodeContainer(id);
            log.info("Downloading regular container {} with id {}", cachedFile.getFileName(), cachedFile.getId());

            HashcodeContainer hashcodeContainer = HashcodeContainer.fromHashcodeContainerBuilder()
                    .container(cachedFile.getHashcodeContainer())
                    .regularDataFiles(cachedFile.getOriginalDataFiles())
                    .build();
            container = hashcodeContainer.getRegularContainer();
            fileName = cachedFile.getFileName();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(container);
    }

    @PostMapping(value = "/smartid-signing", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SmartIdSigningRequest startSmartIdSigning(@RequestBody SmartIdSigningRequest request) {
        log.info("Smart-ID signing request: {}", request);
        sigaApiClientService.startSmartIdSigningFlow(request);
        return request;
    }

    @PostMapping(value = "/mobile-signing", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public MobileSigningRequest startMobileSigning(@RequestBody MobileSigningRequest request) {
        log.info("Mobile signing request: {}", request);
        sigaApiClientService.startMobileSigningFlow(request);
        return request;
    }

    @PostMapping(value = "/prepare-remote-signing", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity prepareRemoteSigning(@RequestBody PrepareRemoteSigningRequest request) {
        log.info("Prepare remote signing request: {}", request);
        return ResponseEntity.ok(sigaApiClientService.prepareRemoteSigning(request));
    }

    @PostMapping(value = "/finalize-remote-signing", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity finalizeRemoteSigning(@RequestBody FinalizeRemoteSigningRequest request) {
        log.info("Finalize remote signing request: {}", request);
        sigaApiClientService.finalizeRemoteSigning(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/augment-container", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity augmentContainer(@RequestBody AugmentationRequest request) {
        log.info("Augmentation request: {}", request);
        sigaApiClientService.startAugmentationFlow(request);
        return ResponseEntity.ok().build();
    }
}
