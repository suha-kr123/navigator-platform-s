package com.nivasafinance.features.document.service.impl;

import com.nivasafinance.features.document.config.DocumentStorageProperties;
import com.nivasafinance.features.document.dto.DocumentCreateRequest;
import com.nivasafinance.features.document.dto.DocumentCreateRequestInputStream;
import com.nivasafinance.features.document.dto.DocumentCreateResponse;
import com.nivasafinance.features.document.entity.Document;
import com.nivasafinance.features.document.enums.DocumentStorageProvider;
import com.nivasafinance.features.document.exception.DocumentExceptionFactory;
import com.nivasafinance.features.document.repository.DocumentRepositoryWrapper;
import com.nivasafinance.features.document.service.DocumentWriteService;
import com.nivasafinance.features.document.storage.ContentRepository;
import com.nivasafinance.features.document.storage.ContentRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Service
@Transactional
public class DocumentWriteServiceImpl implements DocumentWriteService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentWriteServiceImpl.class);
    
    private final DocumentRepositoryWrapper documentRepositoryWrapper;
    private final ContentRepositoryFactory contentRepositoryFactory;
    private final DocumentStorageProperties documentStorageProperties;
    private final DocumentExceptionFactory documentExceptionFactory;
    
    public DocumentWriteServiceImpl(
            DocumentRepositoryWrapper documentRepositoryWrapper,
            ContentRepositoryFactory contentRepositoryFactory,
            DocumentStorageProperties documentStorageProperties,
            MessageSource messageSource) {
        this.documentRepositoryWrapper = documentRepositoryWrapper;
        this.contentRepositoryFactory = contentRepositoryFactory;
        this.documentStorageProperties = documentStorageProperties;
        this.documentExceptionFactory = new DocumentExceptionFactory(messageSource);
    }
    
    @Override
    @Transactional
    public DocumentCreateResponse createDocument(DocumentCreateRequest createRequest) {
        documentExceptionFactory.validateDocumentForCreation(createRequest);
        try {
            return createDocumentInternal(
                    createRequest.getName(),
                    createRequest.getFile().getInputStream(),
                    createRequest.getCustomPath(),
                    createRequest.getFile().getContentType(),
                    createRequest.getFile().getSize()
            );
        } catch (IOException e) {
            throw documentExceptionFactory.createOperationException("upload", e);
        }
    }

    @Override
    @Transactional
    public DocumentCreateResponse createDocument(DocumentCreateRequestInputStream createRequest) {
        documentExceptionFactory.validateDocumentForCreation(createRequest);
        return createDocumentInternal(
                createRequest.getName(),
                createRequest.getFile(),
                createRequest.getCustomPath(),
                createRequest.getContentType(),
                createRequest.getSize()
        );
    }

    private DocumentCreateResponse createDocumentInternal(
            String name,
            java.io.InputStream inputStream,
            String customPath,
            String contentType,
            Long size) {
        ContentRepository contentRepository =
                contentRepositoryFactory.getRepository(documentStorageProperties.getProvider());
        String documentPath = customPath != null 
                ? customPath 
                : generateDocumentPath(name);
        
        String storageKey = contentRepository.saveFile(inputStream, documentPath);
        
        Document document = new Document();
        document.setIdentifier(UUID.randomUUID());
        document.setName(name);
        document.setType(contentType);
        document.setSize(size);
        document.setProvider(DocumentStorageProvider.valueOf(documentStorageProperties.getProvider()));
        document.setPath(storageKey);
        
        Document savedDocument = documentRepositoryWrapper.saveWithException(document);
        return DocumentCreateResponse.builder()
                .id(savedDocument.getId())
                .identifier(savedDocument.getIdentifier())
                .build();
    }

    @Override
    @Transactional
    public void deleteDocumentById(UUID id) {
        Document document = documentRepositoryWrapper.findByIdentifierWithException(id);
        ContentRepository contentRepository =
                contentRepositoryFactory.getRepository(document.getProvider().name());
        contentRepository.deleteFile(document.getPath());
        logger.debug("Successfully deleted file from storage: {}", document.getPath());
        documentRepositoryWrapper.deleteByIdWithException(document.getId());
    }
    
    private String generateDocumentPath(String fileName) {
        long timestamp = System.currentTimeMillis();
        return "documents/" + timestamp + "_" + fileName;
    }
}

