package com.example.leafdoc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class MlService {
    private final RestClient restClient;
    private final String serviceKey;

    public MlService(
            RestClient.Builder builder,
            @Value("${ml.service.url}") String mlUrl,
            @Value("${ml.service.key}") String serviceKey
    ) {
        this.restClient = builder
                .baseUrl("http://localhost:8000")
                .build();

        this.serviceKey = serviceKey;
    }

    public String predict(MultipartFile file)
            throws IOException {

        MultipartBodyBuilder body = new MultipartBodyBuilder();

        body.part("file", file.getResource());
        // maybe validate file here

        return restClient
                .post()
                .uri("/ml/predict")
                .header("X-ML-Service-Key", serviceKey)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body.build())
                .retrieve()
                .body(String.class);
    }
}
