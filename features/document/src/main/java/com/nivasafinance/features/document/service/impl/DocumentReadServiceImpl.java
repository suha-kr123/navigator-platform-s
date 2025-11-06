package com.nivasafinance.features.document.service.impl;

import com.nivasafinance.features.document.dto.DocumentFileResponse;
import com.nivasafinance.features.document.dto.DocumentResponse;
import com.nivasafinance.features.document.repository.DocumentRepositoryWrapper;
import com.nivasafinance.features.document.service.DocumentReadService;
import com.nivasafinance.features.document.storage.ContentRepositoryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DocumentReadServiceImpl implements DocumentReadService {
    
    private final DocumentRepositoryWrapper documentRepositoryWrapper;
    private final ContentRepositoryFactory contentRepositoryFactory;
    
    @Override
    public DocumentResponse getDocumentById(Long id) {
        return DocumentResponse.from(documentRepositoryWrapper.findByIdWithException(id));
    }
    
    @Override
    public DocumentResponse getDocumentByIdentifier(UUID id) {
        return DocumentResponse.from(documentRepositoryWrapper.findByIdentifierWithException(id));
    }
    
    @Override
    public DocumentFileResponse getDocumentFile(UUID id) {
        com.nivasafinance.features.document.entity.Document document = 
                documentRepositoryWrapper.findByIdentifierWithException(id);
        com.nivasafinance.features.document.storage.ContentRepository contentRepository = 
                contentRepositoryFactory.getRepository(document.getProvider().name());
        java.io.InputStream file = contentRepository.fetchFile(document.getPath());
        return DocumentFileResponse.builder()
                .file(file)
                .data(DocumentResponse.from(document))
                .build();
    }
}

