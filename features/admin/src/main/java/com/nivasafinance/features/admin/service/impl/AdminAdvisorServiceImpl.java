package com.nivasafinance.features.admin.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.admin.exception.AdminExceptionFactory;
import com.nivasafinance.features.admin.service.AdminAdvisorService;
import com.nivasafinance.features.advisor.dto.AdminAdvisorBasicResponse;
import com.nivasafinance.features.advisor.dto.AdminAdvisorSearchRequest;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminAdvisorServiceImpl implements AdminAdvisorService {

    private final AdvisorWriteService advisorWriteService;
    private final AdvisorReadService advisorReadService;
    private final UserReadService userReadService;
    private final MessageSource messageSource;

    @Override
    public void deleteAdvisor(UUID identifier) {
        advisorWriteService.deleteAdvisor(identifier);
    }

    @Override
    public void undoDeleteAdvisor(UUID identifier) {
        Advisor advisor = advisorReadService.findAdvisorByIdentifierIncludingDeleted(identifier);
        UserResponse user = userReadService.adminGetUserByUsername(advisor.getUsername());
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw AdminExceptionFactory.cannotRestoreAdvisorUserDeleted(messageSource);
        }
        advisorWriteService.undoDeleteAdvisor(identifier);
    }

    @Override
    public PaginatedResponse<AdminAdvisorBasicResponse> adminSearchAdvisors(PaginationRequest paginationRequest, AdminAdvisorSearchRequest request) {
        return advisorReadService.adminSearchAdvisors(paginationRequest, request);
    }

    @Override
    public PaginatedResponse<AdminAdvisorBasicResponse> getDeletedAdvisors(PaginationRequest paginationRequest) {
        return advisorReadService.getDeletedAdvisors(paginationRequest);
    }
}
