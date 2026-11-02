package com.example.leafdoc.config;

import com.example.leafdoc.exceptions.ImageStorageException;
import com.example.leafdoc.exceptions.MlServiceResponseException;
import com.example.leafdoc.exceptions.MlServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableSpringConfigured
public class RestClientConfig {

    @Bean("mlRestClient")
    public RestClient mlRestClient(
            RestClient.Builder builder,
            @Value("${app.ml.service.url}") String mlUrl,
            @Value("${app.ml.service.key}") String serviceKey
    ) {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(60));

        return builder.clone()
                .baseUrl(mlUrl)
                .requestFactory(factory)
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

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        return builder.clone()
                .baseUrl(supabaseUrl)
                .requestFactory(requestFactory)
                .defaultHeader("apikey", secretKey)
                .defaultStatusHandler(
                        HttpStatusCode::is5xxServerError,
                        (request, response) -> {
                            throw new ImageStorageException(
                                    "Supabase Storage is currently unavailable. HTTP "
                                            + response.getStatusCode().value()
                            );
                        }
                )
                .defaultStatusHandler(
                        HttpStatusCode::is4xxClientError,
                        (request, response) -> {
                            throw new ImageStorageException(
                                    "Supabase Storage rejected the request. HTTP "
                                            + response.getStatusCode().value()
                            );
                        }
                )
                .build();
    }
}
