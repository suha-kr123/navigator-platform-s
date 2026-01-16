package com.nivasafinance.features.document.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.features.document.service.DocumentReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/documents")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentReadService documentReadService;
    
    @GetMapping("/{documentId}")
    @RequirePermission(permissionName = "READ_LEAD_DOCUMENTS")
    public ResponseEntity<InputStreamResource> getDocument(@PathVariable UUID documentId) {
        com.nivasafinance.features.document.dto.DocumentFileResponse documentFileResponse = 
                documentReadService.getDocumentFile(documentId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, documentFileResponse.getData().getType());
        headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                "inline; filename=\"" + documentFileResponse.getData().getName() + "\"");
        
        InputStreamResource resource = new InputStreamResource(documentFileResponse.getFile());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        documentFileResponse.getData().getType() != null 
                                ? documentFileResponse.getData().getType() 
                                : MediaType.APPLICATION_OCTET_STREAM_VALUE
                ))
                .body(resource);
    }
}


