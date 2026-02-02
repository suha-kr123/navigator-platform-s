package com.nivasafinance.features.bulkoperations.service;

import org.springframework.web.multipart.MultipartFile;

import com.nivasafinance.features.bulkoperations.common.entity.BulkOperation;

public interface BulkValidationService {

    void validateAndUpdateOperation(BulkOperation bulkOperation, MultipartFile file);
    
}
