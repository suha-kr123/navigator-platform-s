package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.LeadDocumentResponse;
import com.nivasafinance.features.lead.repository.LeadDocumentRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadDocumentReadService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class LeadDocumentReadServiceImpl implements LeadDocumentReadService {

    private final LeadDocumentRepositoryWrapper leadDocumentRepositoryWrapper;

    @Override
    public PaginatedResponse<LeadDocumentResponse> getAllLeadDocuments(
            UUID leadIdentifier, PaginationRequest paginationRequest) {
        return leadDocumentRepositoryWrapper.findAllDocumentsByLeadIdentifier(
                leadIdentifier, paginationRequest);
    }

    @Override
    public LeadDocumentResponse getLeadDocumentById(UUID leadIdentifier, UUID documentIdentifier) {
        return leadDocumentRepositoryWrapper.findDocumentByLeadIdentifierAndDocumentIdentifier(
                leadIdentifier, documentIdentifier);
    }
}

