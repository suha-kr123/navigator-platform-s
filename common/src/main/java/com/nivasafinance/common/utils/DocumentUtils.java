package com.nivasafinance.common.utils;

import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public class DocumentUtils {

    public static Optional<String> getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename != null && originalFilename.contains(".")) {
           return Optional.of(originalFilename.substring(originalFilename.lastIndexOf(".")));
        }

        // Fallback to content type
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.equals("image/jpeg") || contentType.equals("image/jpg")) {
                return Optional.of(".jpg");
            } else if (contentType.equals("image/png")) {
                return Optional.of(".png");
            }
        }

        return Optional.empty();
    }
}
