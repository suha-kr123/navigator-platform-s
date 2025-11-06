package com.nivasafinance.features.document.repository;

import com.nivasafinance.features.document.entity.Document;
import com.nivasafinance.features.document.exception.DocumentExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DocumentRepositoryWrapper {
    
    private final DocumentRepository documentRepository;
    private final MessageSource messageSource;
    private final DocumentExceptionFactory documentExceptionFactory;
    
    public DocumentRepositoryWrapper(DocumentRepository documentRepository, MessageSource messageSource) {
        this.documentRepository = documentRepository;
        this.messageSource = messageSource;
        this.documentExceptionFactory = new DocumentExceptionFactory(messageSource);
    }
    
    public Document saveWithException(Document document) {
        try {
            return documentRepository.save(document);
        } catch (DataAccessException e) {
            throw documentExceptionFactory.createOperationException("create", e);
        }
    }
    
    public Document findByIdWithException(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> documentExceptionFactory.createNotFoundException(UUID.randomUUID()));
    }
    
    public Page<Document> findAllWithException(Pageable pageable) {
        try {
            return documentRepository.findAll(pageable);
        } catch (DataAccessException e) {
            throw documentExceptionFactory.createOperationException("retrieve", e);
        }
    }
    
    public List<Document> findAllWithException() {
        try {
            return documentRepository.findAll();
        } catch (DataAccessException e) {
            throw documentExceptionFactory.createOperationException("retrieve", e);
        }
    }
    
    public void deleteByIdWithException(Long id) {
        if (!documentRepository.existsById(id)) {
            throw documentExceptionFactory.createNotFoundException(UUID.randomUUID());
        }
        
        try {
            documentRepository.deleteById(id);
        } catch (DataAccessException e) {
            throw documentExceptionFactory.createOperationException("delete", e);
        }
    }
    
    public Document findByIdentifierWithException(UUID identifier) {
        return documentRepository.findByIdentifier(identifier)
                .orElseThrow(() -> documentExceptionFactory.createNotFoundException(identifier));
    }
}

