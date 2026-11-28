package com.example.leafdoc.util;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Getter
public class ImageFile {
    private final MultipartFile file;
    private final String contentType;
    private final String fileName;
    private final String extension;

    public ImageFile(MultipartFile file, ImageValidator validator) {

        ImageValidator.ValidationResult result =
                validator.validate(file);

        this.file = file;
        this.fileName = result.fileName();
        this.extension = result.extension();
        this.contentType = result.contentType();
    }

    public byte[] bytes() throws IOException {
        return file.getBytes();
    }
}
