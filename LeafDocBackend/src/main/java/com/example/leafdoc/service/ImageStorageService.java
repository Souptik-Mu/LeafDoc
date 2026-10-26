package com.example.leafdoc.service;

import com.example.leafdoc.util.ImageFile;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class ImageStorageService {

    private final RestClient sbRestClient;
    private final String bucketName;

    public ImageStorageService(
            @Qualifier("supabaseRestClient") RestClient sbRestClient,
            @Value("${supabase.bucket}") String bucketName) {
        this.bucketName = bucketName;
        this.sbRestClient = sbRestClient;
    }

    public Map<String,String> upload (MultipartFile file) throws IOException { // later changing to imageFile

        ImageFile image = new ImageFile(file); //auto validates image

        String contentType = image.getContentType();

        // Generate a unique name; never trust the client filename.
        String extension = switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png"  -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException(
                    "Unsupported image type"
            );
        };

        String path = UUID.randomUUID() + extension;

        sbRestClient.post()
                .uri("/storage/v1/object/{bucket}/{path}",
                        bucketName, path)
                .contentType(MediaType.parseMediaType(contentType))
                .header("x-upsert", "false")
                .body(image.bytes())
                .retrieve()
                .toBodilessEntity();

        return Map.of(
                "path", path,
                "contentType", contentType
        );
    }

    Map<String, Object> getImage(String path){}

}
