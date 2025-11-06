package com.nivasafinance.features.document.storage;

import com.nivasafinance.features.document.exception.ConfigurationException;
import com.nivasafinance.features.document.storage.impl.FileSystemRepository;
import com.nivasafinance.features.document.storage.impl.S3ContentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContentRepositoryFactory {
    
    private final FileSystemRepository fileSystemRepository;
    private final MessageSource messageSource;
    
    @Autowired(required = false)
    private S3ContentRepository s3ContentRepository;
    
    public ContentRepository getRepository(String providerType) {
        String upperProviderType = providerType.toUpperCase();
        switch (upperProviderType) {
            case "AWS_S3":
                if (s3ContentRepository == null) {
                    throw new ConfigurationException(
                            "AWS_S3 provider is not configured. " +
                            "Please set document.storage.provider=AWS_S3 and configure AWS credentials.",
                            messageSource
                    );
                }
                return s3ContentRepository;
            case "LOCAL":
                return fileSystemRepository;
            default:
                throw new ConfigurationException(
                        "Invalid provider type: " + providerType + ". Valid options: AWS_S3, LOCAL",
                        messageSource
                );
        }
    }
}

