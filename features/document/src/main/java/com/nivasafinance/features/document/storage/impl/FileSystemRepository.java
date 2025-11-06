package com.nivasafinance.features.document.storage.impl;

import com.nivasafinance.features.document.storage.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class FileSystemRepository implements ContentRepository {
    
    private static final int FILE_PROTOCOL_PREFIX_LENGTH = 7;
    
    @Value("${local.storage.base-path:/tmp/documents}")
    private String basePath;
    
    @Override
    public String saveFile(InputStream inputStream, String documentPath) {
        File file = new File(basePath, documentPath);
        file.getParentFile().mkdirs();
        
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            inputStream.transferTo(outputStream);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
        
        return "file://" + basePath + "/" + documentPath;
    }
    
    @Override
    public void deleteFile(String storageKey) {
        String cleanPath = storageKey.startsWith("file://") 
                ? storageKey.substring(FILE_PROTOCOL_PREFIX_LENGTH)
                : storageKey;
        File file = new File(cleanPath);
        if (file.exists()) {
            file.delete();
        }
    }
    
    @Override
    public InputStream fetchFile(String storageKey) {
        String cleanPath = storageKey.startsWith("file://")
                ? storageKey.substring(FILE_PROTOCOL_PREFIX_LENGTH)
                : storageKey;
        File file = new File(cleanPath);
        if (!file.exists()) {
            throw new RuntimeException("File not found: " + cleanPath + " (original path: " + storageKey + ")");
        }
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + cleanPath + " (original path: " + storageKey + ")", e);
        }
    }
    
    @Override
    public String getSignedDownloadUrl(String storageKey, Long expiresIn) {
        return null;
    }
}

