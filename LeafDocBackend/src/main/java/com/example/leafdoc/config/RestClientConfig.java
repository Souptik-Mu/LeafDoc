package com.example.leafdoc.config;

import com.example.leafdoc.exceptions.MlServiceResponseException;
import com.example.leafdoc.exceptions.MlServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean("mlRestClient")
    public RestClient mlRestClient(
            RestClient.Builder builder,
            @Value("${app.ml.service.url}") String mlUrl,
            @Value("${app.ml.service.key}") String serviceKey
    ) {

        return builder.clone()
                .baseUrl(mlUrl)
                .defaultHeader("x-ml-service-key", serviceKey)
                .defaultStatusHandler(
                        HttpStatusCode::is5xxServerError,
                        (request, response) -> {
                            throw new MlServiceUnavailableException();
                        }
                )
                .defaultStatusHandler(
                        HttpStatusCode::is4xxClientError,
                        (request, response) -> {
                            throw new MlServiceResponseException(
                                    response.getStatusCode().value() + " " + response.getStatusText()
                            );
                        }
                )
                .build();
    }

    @Bean("supabaseRestClient")
    public RestClient supabaseRestClient(
            RestClient.Builder builder,
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.secret-key}") String secretKey) {

        return builder.clone()
                .baseUrl(supabaseUrl)
                .defaultHeader("apikey", secretKey)
                .build();
    }
}
