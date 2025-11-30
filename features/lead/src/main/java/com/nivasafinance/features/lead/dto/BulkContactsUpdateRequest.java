package com.nivasafinance.features.lead.dto;

import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.common.dto.IdentifierRequest;
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
    
    @Builder.Default
    @Valid
    private List<AddressOperation> addressOperations = new ArrayList<>();
    
    @Builder.Default
    @Valid
    private List<IdentifierOperation> identifierOperations = new ArrayList<>();
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactUpdateItem {
        private String contactIdentifier;
        
        @Valid
        private UpdateLeadContactRequest data;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddressOperation {
        private String contactIdentifier;  // Required
        private String operation;           // 'create', 'update', or 'delete'
        private String addressId;           // Required for 'update' and 'delete'
        
        @Valid
        private AddressRequest data;        // Required for 'create' and 'update'
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IdentifierOperation {
        private String contactIdentifier;  // Required
        private String operation;           // 'create', 'update', or 'delete'
        private String identifierId;        // Required for 'update' and 'delete' (UUID as string)
        
        @Valid
        private IdentifierRequest data;     // Required for 'create' and 'update'
    }
}

