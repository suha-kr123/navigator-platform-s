package com.nivasafinance.features.advisor.service.crm;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.*;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AdvisorCRMServiceImpl implements AdvisorCRMService {

    private final AdvisorReadService advisorReadService;
    private final AdvisorWriteService advisorWriteService;

    @Override
    public AdvisorTemplateResponse getAdvisorTemplate() {
        return advisorReadService.getAdvisorTemplate();
    }

    @Override
    public PaginatedResponse<AdvisorBasicResponse> getAllAdvisors(PaginationRequest paginationRequest, String name, String mobileNumber) {
        return advisorReadService.getAllAdvisors(paginationRequest, name, mobileNumber);
    }

    @Override
    public UUID createAdvisor(CreateAdvisorRequest request) {
        return advisorWriteService.createAdvisor(request);
    }

    @Override
    public AdvisorResponse getAdvisorByIdentifier(UUID identifier) {
        return advisorReadService.getAdvisorByIdentifier(identifier);
    }

    @Override
    public PaginatedResponse<AdvisorBasicResponse> searchAdvisors(PaginationRequest paginationRequest, AdvisorSearchRequest request) {
        return advisorReadService.searchAdvisors(paginationRequest, request);
    }

    @Override
    public void updateAdvisor(UUID identifier, UpdateAdvisorRequest request) {
        advisorWriteService.updateAdvisor(identifier, request);
    }

    @Override
    public void updateQualificationDetails(UUID identifier, UpdateQualificationDetailsRequest request) {
        advisorWriteService.updateQualificationDetails(identifier, request);
    }

    @Override
    public void updateOccupationDetails(UUID identifier, UpdateOccupationDetailsRequest request) {
        advisorWriteService.updateOccupationDetails(identifier, request);
    }

    @Override
    public void updateSegmentationDetails(UUID identifier, UpdateSegmentationDetailsRequest request) {
        advisorWriteService.updateSegmentationDetails(identifier, request);
    }

    @Override
    public void rejectAdvisor(UUID identifier, RejectAdvisorRequest request) {
        advisorWriteService.rejectAdvisor(identifier, request);
    }

    @Override
    public void dormantAdvisor(UUID identifier, DormantAdvisorRequest request) {
        advisorWriteService.dormantAdvisor(identifier, request);
    }

    @Override
    public void activateAdvisor(UUID identifier) {
        advisorWriteService.activateAdvisor(identifier);
    }

    @Override
    public void outOfGeoAdvisor(UUID identifier, OutOfGeoAdvisorRequest request) {
        advisorWriteService.outOfGeoAdvisor(identifier, request);
    }

    @Override
    public void undoRejectAdvisor(UUID identifier) {
        advisorWriteService.undoRejectAdvisor(identifier);
    }

    @Override
    public void undoDormantAdvisor(UUID identifier) {
        advisorWriteService.undoDormantAdvisor(identifier);
    }

    @Override
    public void undoOutOfGeoAdvisor(UUID identifier) {
        advisorWriteService.undoOutOfGeoAdvisor(identifier);
    }

    @Override
    public PaginatedResponse<AdvisorDashboardResponse> getAdvisorDashboard(
            PaginationRequest paginationRequest,
            AdvisorDashboardFilters filters) {
        return advisorReadService.getAdvisorDashboard(paginationRequest, filters);
    }

    @Override
    public PaginatedResponse<AdvisorBasicResponse> getMyAdvisors(PaginationRequest paginationRequest) {
        return advisorReadService.getMyAdvisors(paginationRequest);
    }

    @Override
    public PaginatedResponse<AdvisorBasicResponse> getAdvisorsByReferralCode(String referralCode, PaginationRequest paginationRequest) {
        return advisorReadService.getAdvisorsByReferralCode(referralCode, paginationRequest);
    }
}
