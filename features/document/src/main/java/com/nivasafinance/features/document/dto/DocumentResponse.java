package com.nivasafinance.features.document.dto;

import com.nivasafinance.features.document.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentResponse {
    private Long id;
    private String name;
    private String type;
    private Long size;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    public static DocumentResponse from(Document document) {
        if (document == null || document.getId() == null) {
            throw new IllegalArgumentException("Document ID cannot be null");
        }
        if (document.getCreatedAt() == null) {
            throw new IllegalArgumentException("Document createdAt cannot be null");
        }
        if (document.getUpdatedAt() == null) {
            throw new IllegalArgumentException("Document updatedAt cannot be null");
        }
        return DocumentResponse.builder()
                .id(document.getId())
                .name(document.getName())
                .type(document.getType())
                .size(document.getSize())
                .createdAt(document.getCreatedAt())
                .createdBy(document.getCreatedBy())
                .updatedAt(document.getUpdatedAt())
                .updatedBy(document.getUpdatedBy())
                .build();
    }
}

