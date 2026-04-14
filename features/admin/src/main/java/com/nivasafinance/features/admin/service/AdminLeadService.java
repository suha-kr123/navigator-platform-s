package com.nivasafinance.features.admin.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.AdminLeadSearchResponse;
import com.nivasafinance.features.lead.dto.AdminLeadSearchRequest;

import java.util.UUID;

public interface AdminLeadService {
    void deleteLead(UUID leadId);
    void undoDeleteLead(UUID leadId);
    PaginatedResponse<AdminLeadSearchResponse> adminSearchLeads(PaginationRequest paginationRequest, AdminLeadSearchRequest request);
    PaginatedResponse<AdminLeadSearchResponse> getDeletedLeads(PaginationRequest paginationRequest);
}
