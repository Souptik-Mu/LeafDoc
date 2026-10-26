package com.example.leafdoc.service;

import com.example.leafdoc.DTO.ImageUploadResult;
import com.example.leafdoc.client.ImageStorageClient;
import com.example.leafdoc.util.ImageFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageStorageClient imageStorageClient;

    public void upload(MultipartFile file)
    {
        ImageFile imageFile = new ImageFile(file);   /// auto validates

        // build an path, add db , after upload & store (user, image path, [contentType],  ...)

        ImageUploadResult res =  imageStorageClient.upload(imageFile, "path/name.jpeg");

    }
}
