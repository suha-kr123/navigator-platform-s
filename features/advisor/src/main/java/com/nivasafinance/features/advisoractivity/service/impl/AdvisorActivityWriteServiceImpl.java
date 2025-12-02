package com.nivasafinance.features.advisoractivity.service.impl;

import com.nivasafinance.features.advisoractivity.dto.CreateAdvisorActivityRequest;
import com.nivasafinance.features.advisoractivity.dto.CreateAdvisorActivityResponse;
import com.nivasafinance.features.advisoractivity.entity.AdvisorActivity;
import com.nivasafinance.features.advisoractivity.repository.AdvisorActivityRepositoryWrapper;
import com.nivasafinance.features.advisoractivity.service.AdvisorActivityWriteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AdvisorActivityWriteServiceImpl implements AdvisorActivityWriteService {
    private final AdvisorActivityRepositoryWrapper advisorActivityRepositoryWrapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CreateAdvisorActivityResponse createAdvisorActivity(CreateAdvisorActivityRequest request) {
        AdvisorActivity activity = new AdvisorActivity();
        activity.setIdentifier(UUID.randomUUID());
        activity.setAdvisorId(request.getAdvisorId());
        activity.setDescription(request.getDescription());
        activity.setResourceType(request.getResource());
        activity.setResourceAction(request.getAction());
        activity.setMetadata(request.getMetadata());
        activity.setResourceId(request.getResourceId());
        
        AdvisorActivity saved = advisorActivityRepositoryWrapper.saveWithException(activity);
        return CreateAdvisorActivityResponse.builder()
                .id(saved.getId())
                .identifier(saved.getIdentifier())
                .build();
    }
}

