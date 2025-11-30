package com.nivasafinance.features.lead.dto;

import jakarta.validation.Valid;
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
public class BulkContactsUpdateRequest {
    
    @Builder.Default
    @Valid
    private List<CreateLeadContactRequest> creates = new ArrayList<>();
    
    @Builder.Default
    @Valid
    private List<ContactUpdateItem> updates = new ArrayList<>();
    
    @Builder.Default
    private List<String> deletes = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactUpdateItem {
        private String contactIdentifier;
        
        @Valid
        private UpdateLeadContactRequest data;
    }
}

