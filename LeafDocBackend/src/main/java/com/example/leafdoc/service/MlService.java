package com.example.leafdoc.service;

import com.example.leafdoc.exceptions.MlServiceResponseException;
import com.example.leafdoc.exceptions.MlServiceUnavailableException;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
@Service
public class MlService {
    private final RestClient restClient;
    private final DataSize maxFileSize;
    private final Set<String> allowedFileTypes;

    public MlService(
            @Qualifier("mlRestClient") RestClient restClient,
            @Value("${app.ml.upload.max-size:5MB}") DataSize maxFileSize,
            @Value("${app.ml.upload.allowed-types}") Set<String> allowedTypes
    ) {
        this.restClient = restClient;
        this.maxFileSize = maxFileSize;
        this.allowedFileTypes = allowedTypes;
    }

    public Map<String, Object> check(){

        try {
           return restClient
                   .get()
                   .uri("/ml/health")
                   .retrieve()
                   .body(new ParameterizedTypeReference<>() {});

        } catch (ResourceAccessException e) {
            throw new MlServiceUnavailableException();
        }
    }

    public Map<String, Object> predict(MultipartFile file) {

        if ( file == null || file.isEmpty() ) {
            //throw new IllegalArgumentException("No file uploaded");
            log.debug("File is empty");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please upload an image"
            );
        }

        if (file.getSize() > maxFileSize.toBytes()){
            log.debug("File is too large");
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "Image exceeds maximum size of " + maxFileSize
            );
        }

        log.debug(file.getContentType());
        log.debug(file.getName());
        log.debug(file.getOriginalFilename());
        log.debug(String.valueOf(file.getSize()));
        log.debug(file.getClass().toString());

        String ext = file.getOriginalFilename()
                .substring(
                        file.getOriginalFilename()
                                .lastIndexOf(".") + 1
                )
                .toLowerCase();

        if (!allowedFileTypes.contains(ext)){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please upload an correct image"
            );
        }
        MultipartBodyBuilder body = new MultipartBodyBuilder();

        body.part("file", file.getResource())
                .filename(file.getOriginalFilename());

        try {
            return restClient.post()
                            .uri("/ml/predict")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .body(body.build())
                            .retrieve()
                            .body(new ParameterizedTypeReference<>() {});
        } catch (ResourceAccessException e) {
            throw new MlServiceUnavailableException();
        }
    }
}
