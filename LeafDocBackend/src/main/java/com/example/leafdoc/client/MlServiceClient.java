package com.example.leafdoc.client;

import com.example.leafdoc.DTO.prediction.MlPredictResponse;
import com.example.leafdoc.exceptions.MlServiceUnavailableException;
import com.example.leafdoc.util.ImageFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.util.Map;
import java.util.Objects;

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

    public MlPredictResponse predict(ImageFile image) {

        MultipartBodyBuilder body = new MultipartBodyBuilder();

        body.part("file", image.file().getResource())
                .filename(image.getFileName()+"."+image.getExtension())
                .contentType(
                        MediaType.parseMediaType(
                                Objects.requireNonNull(
                                        image.getContentType()
                                )
                        )
                );

        try {
            return restClient.post()
                            .uri("/ml/predict")
                            .contentType(MediaType.MULTIPART_FORM_DATA)
                            .body(body.build())
                            .retrieve()
                            .body(MlPredictResponse.class);
        } catch (ResourceAccessException e) {
            throw new MlServiceUnavailableException(
                    "Unable to communicate with ML service",
                    e
            );
        }
    }
}
