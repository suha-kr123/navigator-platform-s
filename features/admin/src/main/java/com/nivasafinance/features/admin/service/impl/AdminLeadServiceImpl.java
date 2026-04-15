package com.nivasafinance.features.admin.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.admin.exception.AdminExceptionFactory;
import com.nivasafinance.features.admin.service.AdminLeadService;
import com.nivasafinance.features.lead.dto.AdminLeadSearchResponse;
import com.nivasafinance.features.lead.dto.AdminLeadSearchRequest;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.common.exception.ResourceNotFoundException;
import com.nivasafinance.features.person.service.PersonReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminLeadServiceImpl implements AdminLeadService {

    private final LeadWriteService leadWriteService;
    private final LeadReadService leadReadService;
    private final PersonReadService personReadService;
    private final MessageSource messageSource;

    @Override
    public void deleteLead(UUID leadId) {
        leadWriteService.deleteLead(leadId);
    }

    @Override
    public void undoDeleteLead(UUID leadId) {
        Long personId = leadReadService.findPrimaryPersonIdForLead(leadId);
        if (personId != null) {
            try {
                personReadService.getPersonById(personId);
            } catch (ResourceNotFoundException e) {
                throw AdminExceptionFactory.cannotRestoreLeadPersonDeleted(messageSource);
            }
        }
        leadWriteService.undoDeleteLead(leadId);
    }

    @Override
    public PaginatedResponse<AdminLeadSearchResponse> adminSearchLeads(PaginationRequest paginationRequest, AdminLeadSearchRequest request) {
        return leadReadService.adminSearchLeads(paginationRequest, request);
    }

    @Override
    public PaginatedResponse<AdminLeadSearchResponse> getDeletedLeads(PaginationRequest paginationRequest) {
        return leadReadService.getDeletedLeads(paginationRequest);
    }
}
