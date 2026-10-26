package com.example.leafdoc.client;

import com.example.leafdoc.DTO.ImageUploadResult;
import com.example.leafdoc.util.ImageFile;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@Component
public class ImageStorageClient {

    private final RestClient sbRestClient;
    private final String bucketName;
    private final String supabaseUrl;

    public ImageStorageClient(
            @Qualifier("supabaseRestClient") RestClient sbRestClient,
            @Value("${supabase.bucket}") String bucketName,
            @Value("${supabase.url}") String supabaseUrl) {
        this.bucketName = bucketName;
        this.sbRestClient = sbRestClient;
        this.supabaseUrl = supabaseUrl;
    }

    public ImageUploadResult upload (ImageFile image, String storePath) { // later changing to imageFile

        String contentType = image.getContentType();

        sbRestClient.post()
                .uri("/storage/v1/object/{bucket}/{path}", bucketName, storePath)
                .contentType(MediaType.parseMediaType(contentType))
                .header("x-upsert", "false") // do not overwrite
                .body(image.bytes())
                .retrieve()
                .toBodilessEntity();

        return new ImageUploadResult(
                storePath,
                contentType,
                getPublicImageUrl(storePath)
        );
    }

    public String getPublicImageUrl(String path) {
        String baseUrl = supabaseUrl.replaceAll("/+$", "");

        String encodedPath = UriUtils.encodePath(
                path, StandardCharsets.UTF_8
        );

        return baseUrl
                + "/storage/v1/object/public/"
                + bucketName
                + "/"
                + encodedPath;
    }

    public void delete(String path) {
        sbRestClient.delete()
                .uri("/storage/v1/object/{bucket}/{path}",
                        bucketName, path)
                .retrieve()
                .toBodilessEntity();
    }

}
