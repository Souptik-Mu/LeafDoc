package com.example.leafdoc.client;

import com.example.leafdoc.exceptions.ImageStorageException;
import com.example.leafdoc.util.ImageFile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Component
public class ImageStorageClient {

    private final RestClient sbRestClient;
    private final String bucketName;
    private final String publicBaseUrl;

    public ImageStorageClient(
            @Qualifier("supabaseRestClient") RestClient sbRestClient,
            @Value("${supabase.bucket}") String bucketName,
            @Value("${supabase.url}") String supabaseUrl) {
        this.bucketName = bucketName;
        this.sbRestClient = sbRestClient;
        this.publicBaseUrl = supabaseUrl.replaceAll("/+$", "")
                + "/storage/v1/object/public/"
                + bucketName + "/";
    }

    public String upload (ImageFile image, String storePath) {

        String contentType = image.getContentType();
        try{
            sbRestClient.post()
                    .uri("/storage/v1/object/{bucket}/{path}", bucketName, storePath)
                    .contentType(MediaType.parseMediaType(contentType))
                    .header("x-upsert", "false") // do not overwrite
                    .body(image.bytes())
                    .retrieve()
                    .toBodilessEntity();

            return getPublicImageUrl(storePath);

        } catch (RestClientException ex) {
            throw new ImageStorageException(
                    "An unexpected error occurred while uploading the image.",
                    ex
            );
        }
    }

    public String getPublicImageUrl(String path) {
        return publicBaseUrl + UriUtils.encodePath(
                path, StandardCharsets.UTF_8
        );
    }

    public void delete(String path) {
        sbRestClient.delete()
                .uri("/storage/v1/object/{bucket}/{path}",
                        bucketName, path)
                .retrieve()
                .toBodilessEntity();
    }
}
