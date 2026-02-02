package com.nivasafinance.features.bulkoperations.storage;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.bulkoperations.common.exception.BulkOperationStorageException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class StoredFileMultipartFile implements MultipartFile {

    private static final String DEFAULT_CONTENT_TYPE = "text/csv";

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final long size;
    private final byte[] content;

    public StoredFileMultipartFile(String name, String originalFilename, String contentType, long size, byte[] content) {
        this.name = ValidationUtils.requireNonNullOrEmpty(name, BulkOperationStorageException::nameRequired);
        this.originalFilename = originalFilename;
        this.contentType = ValidationUtils.isNullOrEmpty(contentType) ? DEFAULT_CONTENT_TYPE : contentType;
        this.size = size;
        this.content = ValidationUtils.requireNonNull(content, BulkOperationStorageException::contentRequired);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        throw BulkOperationStorageException.transferToNotSupported();
    }
}
