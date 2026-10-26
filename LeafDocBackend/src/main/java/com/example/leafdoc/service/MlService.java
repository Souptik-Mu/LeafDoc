package com.example.leafdoc.service;

import com.example.leafdoc.exceptions.MlServiceResponseException;
import com.example.leafdoc.exceptions.MlServiceUnavailableException;
import com.example.leafdoc.util.ImageFile;
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

    public MlService(
            @Qualifier("mlRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
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

        ImageFile image = new ImageFile(file); // auto validates the image, if anything goes south throws tantrum(Exception)

        log.debug(file.getContentType());
        log.debug(file.getName());
        log.debug(file.getOriginalFilename());
        log.debug(String.valueOf(file.getSize()));
        log.debug(file.getClass().toString());

        MultipartBodyBuilder body = new MultipartBodyBuilder();

        body.part("file", image.file().getResource())
                .filename(file.getOriginalFilename())
                .contentType(
                        MediaType.parseMediaType(image.getContentType())
                );;

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
