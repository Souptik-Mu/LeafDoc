package com.example.leafdoc.client;

import com.example.leafdoc.exceptions.MlServiceUnavailableException;
import com.example.leafdoc.util.ImageFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Component
public class MlServiceClient {
    private final RestClient restClient;

    public MlServiceClient(
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
            throw new MlServiceUnavailableException(
                    "Unable to communicate with ML service",
                    e
            );
        }
    }

    public Map<String, Object> predict(MultipartFile file) {
        //todo: ask tiyasa about the genaral responce pattern from the ml model, types of crop supported.
        //  make a DTO with ML service return data type.
        //  Add a mlService layer above it that stores scans includes user, image path, [maybe response parts], scan date time

        ImageFile image = new ImageFile(file); // auto validates the image, if anything goes south throws tantrum(Exception)

        log.debug(file.getContentType());
        log.debug(file.getName());
        log.debug(file.getOriginalFilename());
        log.debug(String.valueOf(file.getSize()));
        log.debug(file.getClass().toString());

        MultipartBodyBuilder body = new MultipartBodyBuilder();

        assert image.getContentType() != null;
        body.part("file", image.file().getResource())
                .filename(image.getFileName()+"."+image.getExtension())
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
            throw new MlServiceUnavailableException(
                    "Unable to communicate with ML service",
                    e
            );
        }
    }
}
