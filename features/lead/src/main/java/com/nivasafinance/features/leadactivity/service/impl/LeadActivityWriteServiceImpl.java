package com.nivasafinance.features.leadactivity.service.impl;

import com.nivasafinance.features.leadactivity.dto.CreateLeadActivityRequest;
import com.nivasafinance.features.leadactivity.dto.CreateLeadActivityResponse;
import com.nivasafinance.features.leadactivity.entity.LeadActivity;
import com.nivasafinance.features.leadactivity.repository.LeadActivityRepositoryWrapper;
import com.nivasafinance.features.leadactivity.service.LeadActivityWriteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class LeadActivityWriteServiceImpl implements LeadActivityWriteService {
    private final LeadActivityRepositoryWrapper leadActivityRepositoryWrapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CreateLeadActivityResponse createLeadActivity(CreateLeadActivityRequest request) {
        LeadActivity activity = new LeadActivity();
        activity.setIdentifier(UUID.randomUUID());
        activity.setLeadId(request.getLeadId());
        activity.setDescription(request.getDescription());
        activity.setResourceType(request.getResource());
        activity.setResourceAction(request.getAction());
        activity.setMetadata(request.getMetadata());
        activity.setResourceId(request.getResourceId());
        
        // Explicitly set createdBy to override JPA auditing (which would use "system" in async threads)
        if (request.getCreatedBy() != null) {
            activity.setCreatedBy(request.getCreatedBy());
        }
        
        LeadActivity saved = leadActivityRepositoryWrapper.saveWithException(activity);
        return CreateLeadActivityResponse.builder()
                .id(saved.getId())
                .identifier(saved.getIdentifier())
                .build();
    }
}



