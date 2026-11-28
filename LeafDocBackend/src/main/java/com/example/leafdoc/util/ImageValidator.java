package com.example.leafdoc.util;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class ImageValidator {

    @Value("${app.image.max-size:2MB}")
    private DataSize maxFileSize;

    @Value("${app.image.allowed-types}")
    private Set<String> allowedFileTypes;

    private final Map<String, Set<String>> MIME_EXTENSIONS = Map.of(
            "image/jpeg", Set.of("jpg", "jpeg"),
            "image/png", Set.of("png"),
            "image/webp", Set.of("webp")
    );

    public record ValidationResult(
            String fileName,
            String extension,
            String contentType
    ) {}

    /**
     * Validates an uploaded image's size, extension, and actual file format.
     * Auto validates the image on loading, anything goes south throws tantrum.
     *
     * @param file the uploaded multipart file
     * @throws ResponseStatusException if the file fails validation
     */
    public ValidationResult validate(MultipartFile file) throws ResponseStatusException {

        if ( file == null || file.isEmpty() ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please upload an image"
            );
        }
        if (file.getSize() > maxFileSize.toBytes()){
            throw new ResponseStatusException(
                    HttpStatus.CONTENT_TOO_LARGE,
                    "Image exceeds maximum size of " + maxFileSize
            );
        }

        String fileName = sanitizeFilename(file.getOriginalFilename());
        var extension = getExtension(fileName);
        String detectedType = detectContentType(file);

        if (detectedType == null
                || !detectedType.equals(file.getContentType())
                || !allowedFileTypes.contains(detectedType)
                || !MIME_EXTENSIONS.getOrDefault(detectedType, Set.of())
                .contains(extension)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please upload a correct image"
            );
        }

        return new ValidationResult(
                fileName,
                extension,
                detectedType
        );
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid image filename"
            );
        }

        String fileName = originalFilename.replace('\\', '/');
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

        if (fileName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid image filename"
            );
        }
        return fileName;
    }

    private @NonNull String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0 || dot == fileName.length() - 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid image filename"
            );
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String detectContentType(MultipartFile file) {
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