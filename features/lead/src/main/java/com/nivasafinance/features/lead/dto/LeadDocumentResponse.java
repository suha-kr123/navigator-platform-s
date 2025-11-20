package com.nivasafinance.features.lead.dto;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeResponse;
import jdk.jfr.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadDocumentResponse {
    private UUID identifier;
    private String name;
    private String type;
    private Long size;
    private List<MasterCodeResponse> categories;
    private List<CodeValueResponse> tags;
    private LocalDateTime createdAt;
    private String createdBy;
}

