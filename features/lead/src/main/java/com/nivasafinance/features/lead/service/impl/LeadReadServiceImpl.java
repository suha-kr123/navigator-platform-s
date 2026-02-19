package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.repository.LeadDashboardWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelReadService;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.service.StaffReadService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class LeadReadServiceImpl implements LeadReadService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final CodeValueMasterService codeValueMasterService;
    private final CodeMasterService codeMasterService;
    private final SourcingChannelReadService sourcingChannelReadService;
    private final LeadDashboardWrapper leadDashboardWrapper;
    private final OfficeReadService officeReadService;
    private final StaffReadService staffReadService;

    @Override
    @Transactional(readOnly = true)
    public LeadTemplateResponse getLeadTemplate() {
        return LeadTemplateResponse.builder()
                .leadRejectionReasons(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_REJECT_REASON_MASTER, true))
                .leadWithdrawalReasons(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_WITHDRAWAL_REASON_MASTER, true))
                .leadOnholdReasons(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_ONHOLD_REASON_MASTER, true))
                .leadDropoffReasons(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_DROPOFF_REASON_MASTER, true))
                .occupationProfiles(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_OCCUPATION_PROFILE_MASTER, true))
                .roofProfiles(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_ROOF_PROFILE_MASTER, true))
                .ltvOptions(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_LTV_MASTER, true))
                .foirOptions(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_FOIR_MASTER, true))
                .monthlyIncomes(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_MONTHLY_INCOME_MASTER, true))
                .propertyDocumentTypes(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_PROPERTY_DOCUMENT_TYPE_MASTER, true))
                .locations(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_LOCATION_MASTER, true))
                .bureauRatings(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_BUREAU_RATING_MASTER, true))
                .customerProfiles(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_CUSTOMER_PROFILE_MASTER, true))
                .leadPurposes(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_PURPOSE_MASTER, true
                ))
                .priorities(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_PRIORITY_MASTER, true))
                .build();
    }

    @Override
    public LeadResponse getLeadByIdentifier(UUID leadIdentifier) {
        return leadRepositoryWrapper.findLeadResponseByIdentifierWithException(leadIdentifier);
    }

    @Override
    public PreliminaryDetailsResponse getPreliminaryDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.PreliminaryDetails preliminaryDetails = lead.getPreliminaryDetails();
        if (preliminaryDetails == null) {
            return PreliminaryDetailsResponse.builder().build();
        }
        return PreliminaryDetailsResponse.builder()
                .whatsAppFormDetails(preliminaryDetails.getWhatsAppDIYForm())
                .isWhatsAppDIYFormCompleted(preliminaryDetails.getIsWhatsAppDIYFormCompleted())
                .monthlyFamilyIncome(preliminaryDetails.getMonthlyFamilyIncome())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CreditDetailsResponse getCreditDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.CreditRatingDetails creditDetails = lead.getCreditRatingDetails();

        if (creditDetails == null) {
            return CreditDetailsResponse.builder().build();
        }

        return CreditDetailsResponse.builder()
                .underwriter(creditDetails.getUnderwriter())
                .occupationProfile(creditDetails.getOccupationProfile() != null
                    ? codeValueMasterService.getByKey(creditDetails.getOccupationProfile()) : null)
                .roofProfile(creditDetails.getRoofProfile() != null
                    ? codeValueMasterService.getByKey(creditDetails.getRoofProfile()) : null)
                .ltv(creditDetails.getLtv() != null
                    ? codeValueMasterService.getByKey(creditDetails.getLtv()) : null)
                .foir(creditDetails.getFoir() != null
                    ? codeValueMasterService.getByKey(creditDetails.getFoir()) : null)
                .monthlyFamilyIncome(creditDetails.getMonthlyFamilyIncome() != null
                    ? codeValueMasterService.getByKey(creditDetails.getMonthlyFamilyIncome()) : null)
                .propertyDocumentType(creditDetails.getPropertyDocumentType() != null
                    ? codeValueMasterService.getByKey(creditDetails.getPropertyDocumentType()) : null)
                .eligibleLoanAmount(creditDetails.getEligibleLoanAmount())
                .location(creditDetails.getLocation() != null
                    ? codeValueMasterService.getByKey(creditDetails.getLocation()) : null)
                .bureauRating(creditDetails.getBureauRating() != null
                    ? codeValueMasterService.getByKey(creditDetails.getBureauRating()) : null)
                .customerProfiles(creditDetails.getCustomerProfiles() != null
                    ? codeValueMasterService.getByKey(creditDetails.getCustomerProfiles()) : null)
                .build();
    }

    @Override
    public ProposedDetailsResponse getProposedDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.ProposedDetails proposedDetails = lead.getProposedDetails();

        if (proposedDetails == null) {
            return ProposedDetailsResponse.builder().build();
        }

        return ProposedDetailsResponse.builder()
                .proposedLoanAmount(proposedDetails.getProposedLoanAmount())
                .roi(proposedDetails.getRoi())
                .tenureValue(proposedDetails.getTenureValue())
                .tenureType(proposedDetails.getTenureType())
                .emi(proposedDetails.getEmi())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyDetailsResponse getPropertyDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.OtherDetails otherDetails = lead.getOtherDetails();

        if (otherDetails == null || otherDetails.getPropertyDetails() == null) {
            return PropertyDetailsResponse.builder().build();
        }

        Lead.PropertyDetails propertyDetails = otherDetails.getPropertyDetails();

        return PropertyDetailsResponse.builder()
                .address(propertyDetails.getAddress())
                .geoData(propertyDetails.getGeoData())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SourcingDetailsResponse getSourcingDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        if (lead.getSourcingChannelId() == null) {
            return SourcingDetailsResponse.builder().build();
        }

        SourcingChannelResponse sourcingChannelResponse =
            sourcingChannelReadService.getById(lead.getSourcingChannelId());

        return SourcingDetailsResponse.builder()
                .sourcingChannelDetails(sourcingChannelResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DisbursementDetailsResponse getDisbursementDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.DisbursementDetails disbursementDetails = lead.getDisbursementDetails();

        if (disbursementDetails == null) {
            return DisbursementDetailsResponse.builder().build();
        }

        // Map tranches to response
        List<TrancheResponse> trancheResponses = null;
        if (disbursementDetails.getTranches() != null) {
            trancheResponses = disbursementDetails.getTranches().stream()
                    .map(tranche -> TrancheResponse.builder()
                            .identifier(tranche.getIdentifier())
                            .amount(tranche.getAmount())
                            .date(tranche.getDate())
                            .build())
                    .collect(Collectors.toList());
        }

        return DisbursementDetailsResponse.builder()
                .disbursedAmount(disbursementDetails.getDisbursedAmount())
                .roi(disbursementDetails.getRoi())
                .tenureValue(disbursementDetails.getTenureValue())
                .tenureType(disbursementDetails.getTenureType())
                .disbursedDate(disbursementDetails.getDisbursedDate())
                .processingFees(disbursementDetails.getProcessingFees())
                .tranches(trancheResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TrancheResponse getTrancheByIdentifier(UUID leadIdentifier, UUID trancheIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.DisbursementDetails disbursementDetails = lead.getDisbursementDetails();

        if (disbursementDetails == null || disbursementDetails.getTranches() == null) {
            throw new RuntimeException("Tranche not found with identifier: " + trancheIdentifier);
        }

        // Find tranche by identifier
        Lead.Tranche tranche = disbursementDetails.getTranches().stream()
                .filter(t -> trancheIdentifier.equals(t.getIdentifier()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Tranche not found with identifier: " + trancheIdentifier));

        return TrancheResponse.builder()
                .identifier(tranche.getIdentifier())
                .amount(tranche.getAmount())
                .date(tranche.getDate())
                .build();
    }

    @Override
    public LeadBasicResponse getLeadBasicByIdentifier(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        return LeadBasicResponse.builder()
                .id(lead.getId())
                .leadIdentifier(leadIdentifier)
                .status(lead.getStatus())
                .substatus(lead.getSubstatus())
                .productCode(lead.getProductCode())
                .build();
    }

    @Override
    public PaginatedResponse<LeadDashboardResponse> getLeadDashboard(
            PaginationRequest paginationRequest,
            LeadDashboardFilters filters) {
        return leadDashboardWrapper.findLeadDashboard(paginationRequest, filters);
    }

    @Override
    public PaginatedResponse<LeadSearchResponse> searchLeads(PaginationRequest paginationRequest, LeadSearchRequest request) {
        return leadRepositoryWrapper.searchLeadsByPhoneNumber(paginationRequest, request);
    }

    @Override
    public LeadDashboardFiltersResponse getLeadDashboardFilters(LeadDashboardFiltersFilters filters) {
        // Get current user's staff and office
        StaffResponse currentStaff = staffReadService.getCurrentStaff();
        String currentUserOfficeKey = currentStaff.getOfficeKey();
        OfficeResponse currentUserOffice = officeReadService.getOfficeByKey(currentUserOfficeKey);
        String currentUserOfficeCode = currentUserOffice.getCode();

        // Get all offices in hierarchy
        List<LeadDashboardFiltersResponse.OfficeResponse> allOffices = officeReadService.getOfficesByCodePrefix(currentUserOfficeCode)
                .stream().map(officeResponse ->  LeadDashboardFiltersResponse
                        .OfficeResponse.builder()
                        .key(officeResponse.getKey())
                        .name(officeResponse.getName())
                        .build()).toList();

        // Determine which offices to use for staff filtering
        List<String> officeKeysForStaff;
        if (filters != null && !CollectionUtils.isEmpty(filters.getOffices())) {
            // Validate branch offices are within hierarchy
            List<OfficeResponse> filteredOffices = officeReadService.getOfficeByKeys(filters.getOffices());
            officeKeysForStaff = filteredOffices.stream()
                    .filter(office -> office.getCode().startsWith(currentUserOfficeCode))
                    .map(OfficeResponse::getKey)
                    .collect(Collectors.toList());
        } else {
            // Get staff from all offices in hierarchy
            officeKeysForStaff = allOffices.stream()
                    .map(LeadDashboardFiltersResponse.OfficeResponse::getKey)
                    .collect(Collectors.toList());
        }

        // Get staff for the determined offices
        List<LeadDashboardFiltersResponse.StaffResponse> staffList = staffReadService
                .getStaffByOfficeKeys(officeKeysForStaff)
                .stream()
                .map(staffResponse -> LeadDashboardFiltersResponse.StaffResponse
                        .builder()
                        .displayName(staffResponse.getUserResponse().getPersonResponse().getDisplayName())
                        .username(staffResponse.getUserResponse().getUsername()).build())
                .toList();

        return LeadDashboardFiltersResponse.builder()
                .offices(allOffices)
                .staffs(staffList)
                .build();
    }

    @Override
    public List<LeadWorkflowDetailsDto> findLeadsByPersonIdsAndStatusesAndSubstatuses(
            List<Long> personIds, List<LeadStatus> statuses, List<LeadSubStatus> substatuses) {
                return leadRepositoryWrapper.findLeadsByPersonIdsAndStatusesAndSubstatuses(personIds, statuses, substatuses);
    }

    @Override
    public LeadBasicResponse getLeadByReferralTrackingCode(String referralTrackingCode) {
        return leadRepositoryWrapper.findLeadByReferralTrackingCodeWithException(referralTrackingCode);
    }

    @Override
    public PaginatedResponse<LeadBasicResponse> getLeadsByEntity(EntityType entityType, UUID entityIdentifier,
            PaginationRequest paginationRequest) {
        return leadRepositoryWrapper.findLeadsByEntity(entityType, entityIdentifier, paginationRequest);
    }

    @Override
    public PaginatedResponse<LeadBasicResponse> getLeadsByReferralCode(String referralCode,
            PaginationRequest paginationRequest) {
        return leadRepositoryWrapper.findLeadsByReferralCode(referralCode, paginationRequest);
    }
}