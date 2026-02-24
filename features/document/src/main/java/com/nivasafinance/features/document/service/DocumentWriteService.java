package com.nivasafinance.features.document.service;

import com.nivasafinance.features.document.dto.DocumentCreateRequest;
import com.nivasafinance.features.document.dto.DocumentCreateRequestInputStream;
import com.nivasafinance.features.document.dto.DocumentCreateResponse;

import java.util.UUID;

public interface DocumentWriteService {
    
    DocumentCreateResponse createDocument(DocumentCreateRequest createRequest);

    DocumentCreateResponse createDocument(DocumentCreateRequestInputStream createRequest);

    void deleteDocumentById(UUID id);

    void deleteDocumentById(Long id);
}


