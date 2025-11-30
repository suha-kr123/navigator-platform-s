package com.nivasafinance.features.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkContactsUpdateResponse {
    
    private int totalProcessed;
    
    @Builder.Default
    private List<EnrichedLeadContactResponse> createdContacts = new ArrayList<>();
    
    @Builder.Default
    private List<EnrichedLeadContactResponse> updatedContacts = new ArrayList<>();
    
    private int deletedCount;
}

