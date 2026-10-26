package com.example.leafdoc.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Configurable
public class ImageFile {
    /** some does do */

    @Value("${app.image.max-size:2MB}")
    private DataSize maxFileSize;

    @Value("${app.image.allowed-types}")
    private Set<String> allowedFileTypes;

    private final Map<String, Set<String>> MIME_EXTENSIONS = Map.of(
            "image/jpeg", Set.of("jpg", "jpeg"),
            "image/png", Set.of("png"),
            "image/webp", Set.of("webp")
    );

    private final MultipartFile file;
    @Getter private final String contentType;
    @Getter private String fileName;
    @Getter private String extension;

    /**
     * Validates an uploaded image's size, extension, and actual file format.
     * Auto validates the image on loading, anything goes south throws tantrum.
     *
     * @param multipartFile the uploaded multipart file
     * @throws ResponseStatusException if the file fails validation
     */
    public ImageFile(MultipartFile multipartFile) throws ResponseStatusException {

        if ( multipartFile == null || multipartFile.isEmpty() ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please upload an image"
            );
        }

        this.file = multipartFile;
        this.fileName =  file.getOriginalFilename();
        contentType = detectContentType();

        if (file.getSize() > maxFileSize.toBytes()){
            throw new ResponseStatusException(
                    HttpStatus.CONTENT_TOO_LARGE,
                    "Image exceeds maximum size of " + maxFileSize
            );
        }

        if (!isValidName()){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid image filename"
            );
        }

        if( contentType == null
                || !contentType.equals(file.getContentType())
                || !allowedFileTypes.contains(contentType)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please upload an correct image"
            );
        }
    }

    public byte[] bytes() throws ResponseStatusException {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to read uploaded image",
                    e
            );
        }
    }

    public MultipartFile file() {
        return file;
    }

    private boolean isValidName(){
        if (fileName == null || fileName.lastIndexOf('.') <= 0) {
            return true;
        }
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        extension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);

        boolean isValidExtension = allowedFileTypes.stream()
                .map(String::toLowerCase)
                .filter(MIME_EXTENSIONS::containsKey)
                .anyMatch(type -> MIME_EXTENSIONS.get(type).contains(extension));

        return fileName.isBlank() || fileName.lastIndexOf('.') <= 0 || !isValidExtension;
    }

    private String detectContentType() {
        try (InputStream in = file.getInputStream()) {
            byte[] header = in.readNBytes(12);

            if (startsWith(header, new byte[]{
                    (byte) 0xFF, (byte) 0xD8, (byte) 0xFF
            })) {
                return "image/jpeg";
            }

            if (startsWith(header, new byte[]{
                    (byte) 0x89, 'P', 'N', 'G',
                    13, 10, 26, 10
            })) {
                return "image/png";
            }

            if (header.length >= 12
                    && startsWith(header, "RIFF".getBytes())
                    && matchesAt(header, 8, "WEBP".getBytes())) {
                return "image/webp";
            }

            return null;
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to inspect uploaded image",
                    e
            );
        }
    }

    private static boolean startsWith(byte[] data, byte[] signature) {
        return matchesAt(data, 0, signature);
    }

    private static boolean matchesAt(
            byte[] data, int offset, byte[] signature) {

        if (data.length < offset + signature.length) {
            return false;
        }

        for (int i = 0; i < signature.length; i++) {
            if (data[offset + i] != signature[i]) {
                return false;
            }
        }

        return true;
    }
}
//supabaseRestClient.post()
//        .uri("/object/{bucket}/{key}", bucket, image.filename())
//        .contentType(MediaType.parseMediaType(
//        image.detectedContentType()
//        ))
//                .body(image.bytes())
//        .retrieve()
//        .toBodilessEntity();