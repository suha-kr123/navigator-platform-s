package com.nivasafinance.features.document.service;

import com.nivasafinance.features.document.dto.DocumentFileResponse;
import com.nivasafinance.features.document.dto.DocumentResponse;

import java.util.UUID;

public interface DocumentReadService {
    
    DocumentResponse getDocumentById(Long id);
    
    DocumentResponse getDocumentByIdentifier(UUID id);
    
    DocumentFileResponse getDocumentFile(Long id);
    
    DocumentFileResponse getDocumentFile(UUID id);
}


