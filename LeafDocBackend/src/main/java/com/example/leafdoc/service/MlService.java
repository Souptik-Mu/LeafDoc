package com.example.leafdoc.service;

import com.example.leafdoc.exceptions.MlServiceResponseException;
import com.example.leafdoc.exceptions.MlServiceUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

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

    public String predict(MultipartFile file)
            throws IOException {

        MultipartBodyBuilder body = new MultipartBodyBuilder();

        body.part("file", file.getResource());

        //todo maybe validate file here

        try {
            String response = restClient.post()
                            .uri("/ml/predict")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .body(body.build())
                            .retrieve()
                            .body(String.class);
            return response;

        } catch (ResourceAccessException e) {
            throw new MlServiceUnavailableException();
        }
    }
}
