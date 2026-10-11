package com.example.leafdoc.service;

import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class MlService {
    private final RestClient restClient;
    private final String serviceKey;

    public MlService(
            RestClient.Builder builder,
            @Value("${app.ml.service.url}") String mlUrl,
            @Value("${app.ml.service.key}") String serviceKey
    ) {
        this.restClient = builder
                .baseUrl(mlUrl)
                .build();

        this.serviceKey = serviceKey;
    }

    public Map<String, Object> check(){
        try{
            Map<String, Object> response = restClient
                    .get()
                    .uri("/ml")
                    //.header("X-ML-Service-Key", serviceKey)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if(response == null){
                return Map.of(
                        "Status" , "Disconnected",
                            "msg" , "NO response"
                );
            }
            response.put("connected", true);
            response.put("msg" , "Good working");

            return response;
        }
        catch (HttpClientErrorException.Forbidden e){
            return Map.of(
                    "connected", false,
                    "authentication", "Unknown",
                    "msg", "Unable to connect to ML service"
            );
        }
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
