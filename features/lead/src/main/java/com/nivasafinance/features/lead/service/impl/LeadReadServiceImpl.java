package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.analytics.AnalyticsEvent;
import com.nivasafinance.analytics.AnalyticsHelper;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.repository.LeadDashboardWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelReadService;
import com.nivasafinance.features.workflow.service.WorkflowConfigReadService;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class LeadReadServiceImpl implements LeadReadService {

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final AddressDataService addressDataService;
    private final CodeValueMasterService codeValueMasterService;
    private final CodeMasterService codeMasterService;
    private final SourcingChannelReadService sourcingChannelReadService;
    private final LeadDashboardWrapper leadDashboardWrapper;
    private final OfficeReadService officeReadService;
    private final StaffReadService staffReadService;
    private final AnalyticsHelper analyticsHelper;
    private final WorkflowConfigReadService workflowConfigReadService;

    @Override
    @Transactional(readOnly = true)
    public LeadTemplateResponse getLeadTemplate() {
        return LeadTemplateResponse.builder()
                .leadRejectionReasons(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_REJECT_REASON_MASTER, true, "default"))
                .leadWithdrawalReasons(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_WITHDRAWAL_REASON_MASTER, true, "default"))
                .leadOnholdReasons(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_ONHOLD_REASON_MASTER, true, "default"))
                .leadDropoffReasons(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_DROPOFF_REASON_MASTER, true, "default"))
                .occupationProfiles(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_OCCUPATION_PROFILE_MASTER, true, "default"))
                .roofProfiles(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_ROOF_PROFILE_MASTER, true, "default"))
                .ltvOptions(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_LTV_MASTER, true, "default"))
                .foirOptions(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_FOIR_MASTER, true, "default"))
                .monthlyIncomes(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_MONTHLY_INCOME_MASTER, true, "default"))
                .propertyDocumentTypes(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_PROPERTY_DOCUMENT_TYPE_MASTER, true, "default"))
                .locations(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_LOCATION_MASTER, true, "default"))
                .bureauRatings(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_BUREAU_RATING_MASTER, true, "default"))
                .customerProfiles(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_CUSTOMER_PROFILE_MASTER, true, "default"))
                .leadPurposes(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_PURPOSE_MASTER, true, "default"
                ))
                .customerConvinceStatuses(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_CUSTOMER_CONVINCE_STATUS_MASTER, true, "default"))
                .priorities(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_PRIORITY_MASTER, true, "default"))
                .intents(codeMasterService.getAllCodeValuesByCodeKey(
                        SystemControlledMasterCodes.LEAD_INTENT_MASTER, true, "default"))
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
        Lead.PropertyDetails.PropertyMeasurementDetails measurement = propertyDetails.getPropertyMeasurementDetails();

        PropertyDetailsResponse.PropertyMeasurementDetailsData measurementData = null;
        if (measurement != null) {
            measurementData = PropertyDetailsResponse.PropertyMeasurementDetailsData.builder()
                    .buildUpArea(measurement.getBuildUpArea())
                    .siteArea(measurement.getSiteArea())
                    .build();
        }

        DocumentChecklistResponse documentChecklistData = null;
        Lead.DocumentChecklist checklist = propertyDetails.getDocumentChecklist();
        if (checklist != null) {
            documentChecklistData = DocumentChecklistResponse.builder()
                    .ekhataType(checklist.getEkhataType())
                    .ekhataStatus(checklist.getEkhataStatus())
                    .saleDeed(checklist.getSaleDeed())
                    .propertyTax(checklist.getPropertyTax())
                    .statementOfAccounts(checklist.getStatementOfAccounts())
                    .otherDocs(checklist.getOtherDocs())
                    .build();
        }

        CodeValueResponse propertyTypeCodeValue = propertyDetails.getPropertyType() != null && !propertyDetails.getPropertyType().isBlank()
                ? codeValueMasterService.getCodeValueByKeyAndCodeKey(propertyDetails.getPropertyType(),
                        SystemControlledMasterCodes.LEAD_ROOF_PROFILE_MASTER)
                : null;
        CodeValueResponse propertyConstructionStageCodeValue = propertyDetails.getPropertyConstructionStage() != null && !propertyDetails.getPropertyConstructionStage().isBlank()
                ? codeValueMasterService.getCodeValueByKeyAndCodeKey(propertyDetails.getPropertyConstructionStage(),
                        SystemControlledMasterCodes.LEAD_PROPERTY_CONSTRUCTION_STATUS_MASTER)
                : null;

        var address = propertyDetails.getAddress();
        if (address != null) {
            address = addressDataService.enrichAddressWithDisplayNames(address);
        }
        return PropertyDetailsResponse.builder()
                .address(address)
                .geoData(propertyDetails.getGeoData())
                .propertyType(propertyTypeCodeValue)
                .propertyConstructionStage(propertyConstructionStageCodeValue)
                .owner(propertyDetails.getOwner())
                .ownerRelation(propertyDetails.getOwnerRelation())
                .propertyMeasurementDetails(measurementData)
                .documentChecklistResponse(documentChecklistData)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentChecklistResponse getDocumentChecklist(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.OtherDetails otherDetails = lead.getOtherDetails();
        if (otherDetails == null || otherDetails.getPropertyDetails() == null) {
            return DocumentChecklistResponse.builder().build();
        }
        Lead.DocumentChecklist checklist = otherDetails.getPropertyDetails().getDocumentChecklist();
        if (checklist == null) {
            return DocumentChecklistResponse.builder().build();
        }
        return DocumentChecklistResponse.builder()
                .ekhataType(checklist.getEkhataType())
                .ekhataStatus(checklist.getEkhataStatus())
                .saleDeed(checklist.getSaleDeed())
                .propertyTax(checklist.getPropertyTax())
                .statementOfAccounts(checklist.getStatementOfAccounts())
                .otherDocs(checklist.getOtherDocs())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public IncomeObligationDetailsResponse getIncomeObligationDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.IncomeObligationDetails details = lead.getIncomeObligationDetails();

        if (details == null) {
            return IncomeObligationDetailsResponse.builder().build();
        }

        List<IncomeObligationDetailsResponse.IncomeDetailData> incomeDetailsData = null;
        if (details.getIncomeDetails() != null) {
            incomeDetailsData = details.getIncomeDetails().stream()
                    .map(d -> IncomeObligationDetailsResponse.IncomeDetailData.builder()
                            .incomeSource(d.getIncomeSource() != null && !d.getIncomeSource().isBlank()
                                    ? codeValueMasterService.getByKey(d.getIncomeSource())
                                    : null)
                            .amount(d.getAmount())
                            .documentChecklist(mapIncomeDocumentChecklistResponse(d.getDocumentChecklist()))
                            .build())
                    .collect(Collectors.toList());
        }

        IncomeObligationDetailsResponse.ObligationsData obligationsData = null;
        if (details.getObligationDetails() != null) {
            obligationsData = IncomeObligationDetailsResponse.ObligationsData.builder()
                    .existingEmi(details.getObligationDetails().getExistingEmi())
                    .build();
        }

        return IncomeObligationDetailsResponse.builder()
                .incomeDetails(incomeDetailsData)
                .obligations(obligationsData)
                .monthlyFamilyIncome(details.getMonthlyFamilyIncome())
                .build();
    }

    private List<IncomeObligationDetailsResponse.IncomeDocumentChecklistData> mapIncomeDocumentChecklistResponse(
            List<Lead.IncomeDocumentChecklist> checklist) {
        if (checklist == null || checklist.isEmpty()) {
            return null;
        }
        return checklist.stream()
                .map(item -> IncomeObligationDetailsResponse.IncomeDocumentChecklistData.builder()
                        .documentType(item.getDocumentType() != null && !item.getDocumentType().isBlank()
                                ? codeValueMasterService.getByKey(item.getDocumentType())
                                : null)
                        .status(item.getStatus())
                        .build())
                .toList();
    }

    @SuppressWarnings("deprecation")
    @Override
    @Transactional(readOnly = true)
    public SourcingDetailsResponse getSourcingDetails(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);

        SourcingDetailsResponse.SourcingDetailsResponseBuilder builder = SourcingDetailsResponse.builder()
                .sourcingHistory(lead.getSourcingHistory())
                .referredByCode(lead.getReferredByCode())
                .referredByType(lead.getReferredByType());

        // Backward compat: still populate old field if sourcing_channel_id exists
        if (lead.getSourcingChannelId() != null) {
            try {
                builder.sourcingChannelDetails(sourcingChannelReadService.getById(lead.getSourcingChannelId()));
            } catch (Exception e) {
                log.warn("Failed to fetch legacy sourcing channel for lead {}", leadIdentifier, e);
            }
        }

        return builder.build();
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
//    Results are scoped by office hierarchy
    @Override
    public PaginatedResponse<LeadSearchResponse> searchLeads(PaginationRequest paginationRequest, LeadSearchRequest request) {
        return leadRepositoryWrapper.searchLeadsByPhoneNumber(paginationRequest, request);
    }
    /** Does not apply office hierarchy. */
    @Override
    public boolean hasLeadWithMobileNumber(String mobileNumber) {
        return leadRepositoryWrapper.existsLeadWithMobileNumber(mobileNumber);
    }

    @Override
    public Optional<LeadBasicResponse> findLeadByPhoneNumber(String mobileNumber) {
        return leadRepositoryWrapper.findReusableLeadByPhoneNumber(mobileNumber);
    }

    @Override
    public PaginatedResponse<AdminLeadSearchResponse> adminSearchLeads(PaginationRequest paginationRequest, AdminLeadSearchRequest request) {
        return leadRepositoryWrapper.adminSearchLeadsByPhoneNumber(paginationRequest, request);
    }

    @Override
    public PaginatedResponse<AdminLeadSearchResponse> getDeletedLeads(PaginationRequest paginationRequest) {
        return leadRepositoryWrapper.findDeletedLeads(paginationRequest);
    }

    @Override
    public Long findPrimaryPersonIdForLead(UUID leadIdentifier) {
        return leadRepositoryWrapper.findPrimaryPersonIdForLead(leadIdentifier);
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

    @Override
    public CurrentCustomerFormStepResponse getCurrentCustomerFormStep(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.OtherDetails other = lead.getOtherDetails();
        String step = (other != null) ? other.getCurrentCustomerFormStep() : null;
        analyticsHelper.captureLead(new AnalyticsEvent(
                leadIdentifier.toString(),
                "form_step_viewed",
                List.of(
                        new AnalyticsEvent.Param("step_name", step != null ? step : "")
                )));
        return CurrentCustomerFormStepResponse.builder()
                .currentCustomerFormStep(step)
                .build();
    }

    @Override
    public StageIdentifierResponse getStageIdentifier(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Lead.WorkflowDetails workflowDetails = lead.getWorkflowDetails();

        String identifier = null;
        if (workflowDetails != null
                && workflowDetails.getWorkflowConfigKey() != null
                && workflowDetails.getCurrentStageDetails() != null
                && workflowDetails.getCurrentStageDetails().getStageKey() != null) {
            identifier = workflowConfigReadService.getStageIdentifier(
                    workflowDetails.getWorkflowConfigKey(),
                    workflowDetails.getCurrentStageDetails().getStageKey());
        }

        return StageIdentifierResponse.builder()
                .identifier(identifier)
                .build();
    }
}