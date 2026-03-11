package com.nivasafinance.features.document.storage.impl;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.nivasafinance.features.document.exception.S3DeleteException;
import com.nivasafinance.features.document.exception.S3DownloadException;
import com.nivasafinance.features.document.exception.S3ExpirationException;
import com.nivasafinance.features.document.exception.S3UploadException;
import com.nivasafinance.features.document.exception.S3UrlGenerationException;
import com.nivasafinance.features.document.storage.ContentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Date;

@Component
@ConditionalOnProperty(
        name = "document.storage.provider",
        havingValue = "AWS_S3",
        matchIfMissing = false
)
@RequiredArgsConstructor
public class S3ContentRepository implements ContentRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(S3ContentRepository.class);
    private static final long MILLISECONDS_PER_SECOND = 1000L;
    private static final long MIN_EXPIRATION_SECONDS = 1L;
    private static final long MAX_EXPIRATION_SECONDS = 604800L;
    private static final long DEFAULT_EXPIRATION_SECONDS = 300L;
    
    private final AmazonS3 amazonS3;
    private final MessageSource messageSource;
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    @Value("${document.storage.signed-url.expiration-seconds:300}")
    private Long defaultExpirationSeconds = DEFAULT_EXPIRATION_SECONDS;
    
    @Override
    public String saveFile(InputStream inputStream, String documentPath) {
        try {
            logger.debug("Uploading file to S3: {}", documentPath);
            
            ObjectMetadata metadata = new ObjectMetadata();
            try {
                metadata.setContentLength(inputStream.available());
            } catch (java.io.IOException e) {
                logger.warn("Could not determine content length", e);
            }
            metadata.setContentType(getContentType(documentPath));
            
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, documentPath, inputStream, metadata);
            amazonS3.putObject(putObjectRequest);
            
            logger.debug("Successfully uploaded file to S3: {}", documentPath);
            return documentPath;
        } catch (AmazonServiceException e) {
            logger.error("Failed to upload file to S3: {}", documentPath, e);
            S3UploadException exception = new S3UploadException(
                    e.getMessage() != null ? e.getMessage() : "Unknown error",
                    messageSource
            );
            exception.initCause(e);
            throw exception;
        } catch (AmazonClientException e) {
            logger.error("Client error uploading file to S3: {}", documentPath, e);
            S3UploadException exception = new S3UploadException(
                    e.getMessage() != null ? e.getMessage() : "Unknown error",
                    messageSource
            );
            exception.initCause(e);
            throw exception;
        }
    }
    
    @Override
    public void deleteFile(String storageKey) {
        try {
            logger.debug("Deleting file from S3: {}", storageKey);
            amazonS3.deleteObject(bucketName, storageKey);
            logger.debug("Successfully deleted file from S3: {}", storageKey);
        } catch (AmazonServiceException e) {
            logger.error("Failed to delete file from S3: {}", storageKey, e);
            S3DeleteException exception = new S3DeleteException(
                    e.getMessage() != null ? e.getMessage() : "Unknown error",
                    messageSource
            );
            exception.initCause(e);
            throw exception;
        } catch (AmazonClientException e) {
            logger.error("Client error deleting file from S3: {}", storageKey, e);
            S3DeleteException exception = new S3DeleteException(
                    e.getMessage() != null ? e.getMessage() : "Unknown error",
                    messageSource
            );
            exception.initCause(e);
            throw exception;
        }
    }
    
    @Override
    public InputStream fetchFile(String storageKey) {
        try {
            logger.debug("Fetching file from S3: {}", storageKey);
            S3Object s3Object = amazonS3.getObject(bucketName, storageKey);
            logger.debug("Successfully fetched file from S3: {}", storageKey);
            return s3Object.getObjectContent();
        } catch (AmazonServiceException e) {
            logger.error("Failed to fetch file from S3: {}", storageKey, e);
            S3DownloadException exception = new S3DownloadException(
                    e.getMessage() != null ? e.getMessage() : "Unknown error",
                    messageSource
            );
            exception.initCause(e);
            throw exception;
        } catch (AmazonClientException e) {
            logger.error("Client error fetching file from S3: {}", storageKey, e);
            S3DownloadException exception = new S3DownloadException(
                    e.getMessage() != null ? e.getMessage() : "Unknown error",
                    messageSource
            );
            exception.initCause(e);
            throw exception;
        }
    }
    
    @Override
    public String getSignedDownloadUrl(String storageKey, Long expiresIn) {
        try {
            long actualExpiration = (expiresIn != null && expiresIn > 0) ? expiresIn : defaultExpirationSeconds;
            
            if (actualExpiration < MIN_EXPIRATION_SECONDS || actualExpiration > MAX_EXPIRATION_SECONDS) {
                throw new S3ExpirationException(messageSource);
            }
            
            logger.debug("Generating signed URL for S3 object: {}, expires in: {}s", storageKey, actualExpiration);
            
            Date expiration = new Date(System.currentTimeMillis() + actualExpiration * MILLISECONDS_PER_SECOND);
            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, storageKey)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
            
            String signedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
            logger.debug("Successfully generated signed URL for S3 object: {}", storageKey);
            return signedUrl;
        } catch (S3ExpirationException e) {
            throw e;
        } catch (AmazonServiceException e) {
            logger.error("Failed to generate signed URL for S3 object: {}", storageKey, e);
            S3UrlGenerationException exception = new S3UrlGenerationException(
                    e.getMessage() != null ? e.getMessage() : "Unknown error",
                    messageSource
            );
            exception.initCause(e);
            throw exception;
        } catch (AmazonClientException e) {
            logger.error("Client error generating signed URL for S3 object: {}", storageKey, e);
            S3UrlGenerationException exception = new S3UrlGenerationException(
                    e.getMessage() != null ? e.getMessage() : "Unknown error",
                    messageSource
            );
            exception.initCause(e);
            throw exception;
        }
    }
    
    private String getContentType(String filePath) {
        String extension = filePath.contains(".") 
                ? filePath.substring(filePath.lastIndexOf('.') + 1).toLowerCase()
                : "";
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "txt":
                return "text/plain";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "svg":
                return "image/svg+xml";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "xls":
                return "application/vnd.ms-excel";
            default:
                return "application/octet-stream";
        }
    }
}


