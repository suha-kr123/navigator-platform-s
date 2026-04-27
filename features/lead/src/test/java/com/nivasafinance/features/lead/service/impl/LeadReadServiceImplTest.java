package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.analytics.AnalyticsEvent;
import com.nivasafinance.analytics.AnalyticsHelper;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.GeoData;
import com.nivasafinance.common.enums.TenureType;
import com.nivasafinance.features.lead.dto.*;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.enums.LeadSubStatus;
import com.nivasafinance.features.lead.repository.LeadDashboardWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.address.service.AddressDataService;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.offices.dto.OfficeResponse;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelReadService;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.service.StaffReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeadReadServiceImplTest {

    @Mock
    private LeadRepositoryWrapper leadRepositoryWrapper;

    @Mock
    private AddressDataService addressDataService;

    @Mock
    private CodeValueMasterService codeValueMasterService;

    @Mock
    private CodeMasterService codeMasterService;

    @Mock
    private SourcingChannelReadService sourcingChannelReadService;

    @Mock
    private LeadDashboardWrapper leadDashboardWrapper;

    @Mock
    private OfficeReadService officeReadService;

    @Mock
    private StaffReadService staffReadService;

    @Mock
    private AnalyticsHelper analyticsHelper;

    @InjectMocks
    private LeadReadServiceImpl leadReadService;

    private UUID leadIdentifier;
    private Lead lead;

    @BeforeEach
    void setUp() {
        leadIdentifier = UUID.randomUUID();
        lead = new Lead();
        lead.setId(1L);
        lead.setLeadIdentifier(leadIdentifier);
        lead.setStatus(LeadStatus.ACTIVE);
        lead.setProductCode("HL");
    }

    // ==================== getLeadTemplate() Tests ====================

    @Test
    void getLeadTemplate_returnsTemplateWithAllCodeValues() {
        // Arrange
        List<CodeValueResponse> mockValues = List.of(new CodeValueResponse());
        when(codeMasterService.getAllCodeValuesByCodeKey(anyString(), eq(true), eq("default")))
                .thenReturn(mockValues);

        // Act
        LeadTemplateResponse result = leadReadService.getLeadTemplate();

        // Assert
        assertNotNull(result, "Template response should not be null");
        assertEquals(mockValues, result.getLeadRejectionReasons(), "Rejection reasons should be populated");
        assertEquals(mockValues, result.getLeadPurposes(), "Lead purposes should be populated");
    }

    // ==================== getLeadByIdentifier() Tests ====================

    @Test
    void getLeadByIdentifier_withValidIdentifier_returnsLeadResponse() {
        // Arrange
        LeadResponse expected = new LeadResponse();
        when(leadRepositoryWrapper.findLeadResponseByIdentifierWithException(leadIdentifier)).thenReturn(expected);

        // Act
        LeadResponse result = leadReadService.getLeadByIdentifier(leadIdentifier);

        // Assert
        assertNotNull(result, "Lead response should not be null");
        verify(leadRepositoryWrapper).findLeadResponseByIdentifierWithException(leadIdentifier);
    }

    // ==================== getPreliminaryDetails() Tests ====================

    @Test
    void getPreliminaryDetails_withExistingDetails_returnsMappedResponse() {
        // Arrange
        Lead.PreliminaryDetails preliminaryDetails = Lead.PreliminaryDetails.builder()
                .isWhatsAppDIYFormCompleted(true)
                .whatsAppDIYForm(Map.of("name", "John"))
                .monthlyFamilyIncome(BigDecimal.valueOf(50000))
                .build();
        lead.setPreliminaryDetails(preliminaryDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        PreliminaryDetailsResponse result = leadReadService.getPreliminaryDetails(leadIdentifier);

        // Assert
        assertEquals(true, result.getIsWhatsAppDIYFormCompleted(), "WhatsApp form completed flag should be mapped");
        assertEquals(Map.of("name", "John"), result.getWhatsAppFormDetails(), "WhatsApp form details should be mapped");
        assertEquals(BigDecimal.valueOf(50000), result.getMonthlyFamilyIncome(), "Monthly family income should be mapped");
    }

    @Test
    void getPreliminaryDetails_withNullDetails_returnsEmptyResponse() {
        // Arrange
        lead.setPreliminaryDetails(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        PreliminaryDetailsResponse result = leadReadService.getPreliminaryDetails(leadIdentifier);

        // Assert
        assertNotNull(result, "Should return empty response, not null");
        assertNull(result.getIsWhatsAppDIYFormCompleted(), "All fields should be null for empty details");
    }

    // ==================== getCreditDetails() Tests ====================

    @Test
    void getCreditDetails_withExistingDetails_returnsMappedResponse() {
        // Arrange
        Lead.CreditRatingDetails creditDetails = Lead.CreditRatingDetails.builder()
                .underwriter("UW-001")
                .occupationProfile("SALARIED")
                .eligibleLoanAmount(BigDecimal.valueOf(3000000))
                .build();
        lead.setCreditRatingDetails(creditDetails);
        CodeValueResponse codeValue = new CodeValueResponse();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(codeValueMasterService.getByKey("SALARIED")).thenReturn(codeValue);

        // Act
        CreditDetailsResponse result = leadReadService.getCreditDetails(leadIdentifier);

        // Assert
        assertEquals("UW-001", result.getUnderwriter(), "Underwriter should be mapped directly");
        assertEquals(codeValue, result.getOccupationProfile(), "Occupation profile should be resolved via code value service");
        assertEquals(BigDecimal.valueOf(3000000), result.getEligibleLoanAmount(), "Eligible loan amount should be mapped");
    }

    @Test
    void getCreditDetails_withNullDetails_returnsEmptyResponse() {
        // Arrange
        lead.setCreditRatingDetails(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        CreditDetailsResponse result = leadReadService.getCreditDetails(leadIdentifier);

        // Assert
        assertNotNull(result, "Should return empty response, not null");
        assertNull(result.getUnderwriter(), "All fields should be null for empty details");
    }

    // ==================== getProposedDetails() Tests ====================

    @Test
    void getProposedDetails_withExistingDetails_returnsMappedResponse() {
        // Arrange
        Lead.ProposedDetails proposedDetails = Lead.ProposedDetails.builder()
                .proposedLoanAmount(BigDecimal.valueOf(2500000))
                .roi(BigDecimal.valueOf(9.5))
                .tenureValue(240)
                .tenureType(TenureType.MONTH)
                .emi(BigDecimal.valueOf(22000))
                .build();
        lead.setProposedDetails(proposedDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        ProposedDetailsResponse result = leadReadService.getProposedDetails(leadIdentifier);

        // Assert
        assertEquals(BigDecimal.valueOf(2500000), result.getProposedLoanAmount(), "Proposed loan amount should be mapped");
        assertEquals(BigDecimal.valueOf(9.5), result.getRoi(), "ROI should be mapped");
        assertEquals(240, result.getTenureValue(), "Tenure value should be mapped");
        assertEquals(TenureType.MONTH, result.getTenureType(), "Tenure type should be mapped");
        assertEquals(BigDecimal.valueOf(22000), result.getEmi(), "EMI should be mapped");
    }

    @Test
    void getProposedDetails_withNullDetails_returnsEmptyResponse() {
        // Arrange
        lead.setProposedDetails(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        ProposedDetailsResponse result = leadReadService.getProposedDetails(leadIdentifier);

        // Assert
        assertNotNull(result, "Should return empty response, not null");
        assertNull(result.getProposedLoanAmount(), "All fields should be null for empty details");
    }

    // ==================== getPropertyDetails() Tests ====================

    @Test
    void getPropertyDetails_withNullOtherDetails_returnsEmptyResponse() {
        // Arrange
        lead.setOtherDetails(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        PropertyDetailsResponse result = leadReadService.getPropertyDetails(leadIdentifier);

        // Assert
        assertNotNull(result, "Should return empty response, not null");
        assertNull(result.getOwner(), "All fields should be null when other details is null");
    }

    @Test
    void getPropertyDetails_withNullPropertyDetails_returnsEmptyResponse() {
        // Arrange
        Lead.OtherDetails otherDetails = Lead.OtherDetails.builder().propertyDetails(null).build();
        lead.setOtherDetails(otherDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        PropertyDetailsResponse result = leadReadService.getPropertyDetails(leadIdentifier);

        // Assert
        assertNotNull(result, "Should return empty response, not null");
    }

    @Test
    void getPropertyDetails_withFullDetails_returnsMappedResponse() {
        // Arrange
        AddressData address = new AddressData();
        AddressData enrichedAddress = new AddressData();
        GeoData geoData = new GeoData();
        Lead.PropertyDetails.PropertyMeasurementDetails measurement =
                Lead.PropertyDetails.PropertyMeasurementDetails.builder()
                        .buildUpArea("1200").siteArea("1500").build();
        Lead.DocumentChecklist checklist = Lead.DocumentChecklist.builder()
                .ekhataType("E-Khata").saleDeed("Available").build();
        Lead.PropertyDetails propertyDetails = Lead.PropertyDetails.builder()
                .address(address).geoData(geoData)
                .propertyType("APARTMENT").propertyConstructionStage("COMPLETED")
                .owner("John").ownerRelation("Self")
                .propertyMeasurementDetails(measurement)
                .documentChecklist(checklist)
                .build();
        Lead.OtherDetails otherDetails = Lead.OtherDetails.builder().propertyDetails(propertyDetails).build();
        lead.setOtherDetails(otherDetails);

        CodeValueResponse propertyTypeCv = new CodeValueResponse();
        CodeValueResponse constructionStageCv = new CodeValueResponse();

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(addressDataService.enrichAddressWithDisplayNames(address)).thenReturn(enrichedAddress);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(eq("APARTMENT"), anyString())).thenReturn(propertyTypeCv);
        when(codeValueMasterService.getCodeValueByKeyAndCodeKey(eq("COMPLETED"), anyString())).thenReturn(constructionStageCv);

        // Act
        PropertyDetailsResponse result = leadReadService.getPropertyDetails(leadIdentifier);

        // Assert
        assertEquals(enrichedAddress, result.getAddress(), "Address should be enriched via address data service");
        assertEquals(geoData, result.getGeoData(), "Geo data should be mapped");
        assertEquals("John", result.getOwner(), "Owner should be mapped");
        assertEquals("1200", result.getPropertyMeasurementDetails().getBuildUpArea(), "Build up area should be mapped");
        assertEquals("E-Khata", result.getDocumentChecklistResponse().getEkhataType(), "Checklist ekhata type should be mapped");
    }

    // ==================== getDocumentChecklist() Tests ====================

    @Test
    void getDocumentChecklist_withNullOtherDetails_returnsEmptyResponse() {
        // Arrange
        lead.setOtherDetails(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        DocumentChecklistResponse result = leadReadService.getDocumentChecklist(leadIdentifier);

        // Assert
        assertNotNull(result, "Should return empty response, not null");
    }

    @Test
    void getDocumentChecklist_withExistingChecklist_returnsMappedResponse() {
        // Arrange
        Lead.DocumentChecklist checklist = Lead.DocumentChecklist.builder()
                .ekhataType("E-Khata").ekhataStatus("Valid")
                .saleDeed("Available").propertyTax("Paid")
                .statementOfAccounts("Provided").otherDocs("None")
                .build();
        Lead.PropertyDetails propertyDetails = Lead.PropertyDetails.builder().documentChecklist(checklist).build();
        Lead.OtherDetails otherDetails = Lead.OtherDetails.builder().propertyDetails(propertyDetails).build();
        lead.setOtherDetails(otherDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        DocumentChecklistResponse result = leadReadService.getDocumentChecklist(leadIdentifier);

        // Assert
        assertEquals("E-Khata", result.getEkhataType(), "Ekhata type should be mapped");
        assertEquals("Valid", result.getEkhataStatus(), "Ekhata status should be mapped");
        assertEquals("Available", result.getSaleDeed(), "Sale deed should be mapped");
    }

    // ==================== getIncomeObligationDetails() Tests ====================

    @Test
    void getIncomeObligationDetails_withNullDetails_returnsEmptyResponse() {
        // Arrange
        lead.setIncomeObligationDetails(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        IncomeObligationDetailsResponse result = leadReadService.getIncomeObligationDetails(leadIdentifier);

        // Assert
        assertNotNull(result, "Should return empty response, not null");
        assertNull(result.getIncomeDetails(), "Income details should be null");
    }

    @Test
    void getIncomeObligationDetails_withFullDetails_returnsMappedResponse() {
        // Arrange
        Lead.IncomeDocumentChecklist docChecklist = Lead.IncomeDocumentChecklist.builder()
                .documentType("SALARY_SLIP").status("VERIFIED").build();
        Lead.IncomeDetails incomeDetail = Lead.IncomeDetails.builder()
                .incomeSource("SALARY").amount(BigDecimal.valueOf(80000))
                .documentChecklist(List.of(docChecklist)).build();
        Lead.ObligationDetails obligationDetails = Lead.ObligationDetails.builder()
                .existingEmi(BigDecimal.valueOf(15000)).build();
        Lead.IncomeObligationDetails details = Lead.IncomeObligationDetails.builder()
                .incomeDetails(List.of(incomeDetail))
                .obligationDetails(obligationDetails)
                .monthlyFamilyIncome(BigDecimal.valueOf(80000))
                .build();
        lead.setIncomeObligationDetails(details);

        CodeValueResponse incomeSrcCv = new CodeValueResponse();
        CodeValueResponse docTypeCv = new CodeValueResponse();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(codeValueMasterService.getByKey("SALARY")).thenReturn(incomeSrcCv);
        when(codeValueMasterService.getByKey("SALARY_SLIP")).thenReturn(docTypeCv);

        // Act
        IncomeObligationDetailsResponse result = leadReadService.getIncomeObligationDetails(leadIdentifier);

        // Assert
        assertNotNull(result.getIncomeDetails(), "Income details should be populated");
        assertEquals(1, result.getIncomeDetails().size(), "Should have one income detail");
        assertEquals(incomeSrcCv, result.getIncomeDetails().get(0).getIncomeSource(), "Income source should be resolved");
        assertEquals(BigDecimal.valueOf(80000), result.getIncomeDetails().get(0).getAmount(), "Income amount should be mapped");
        assertEquals(BigDecimal.valueOf(15000), result.getObligations().getExistingEmi(), "Existing EMI should be mapped");
        assertEquals(BigDecimal.valueOf(80000), result.getMonthlyFamilyIncome(), "Monthly family income should be mapped");
    }

    // ==================== getSourcingDetails() Tests ====================

    @Test
    void getSourcingDetails_withNullSourcingChannelId_returnsEmptyResponse() {
        // Arrange
        lead.setSourcingChannelId(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        SourcingDetailsResponse result = leadReadService.getSourcingDetails(leadIdentifier);

        // Assert
        assertNotNull(result, "Should return empty response, not null");
        assertNull(result.getSourcingChannelDetails(), "Sourcing channel details should be null");
    }

    @Test
    void getSourcingDetails_withValidSourcingChannelId_returnsDetailsResponse() {
        // Arrange
        lead.setSourcingChannelId(10L);
        SourcingChannelResponse channelResponse = new SourcingChannelResponse();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(sourcingChannelReadService.getById(10L)).thenReturn(channelResponse);

        // Act
        SourcingDetailsResponse result = leadReadService.getSourcingDetails(leadIdentifier);

        // Assert
        assertEquals(channelResponse, result.getSourcingChannelDetails(), "Sourcing channel should be resolved by ID");
    }

    // ==================== getDisbursementDetails() Tests ====================

    @Test
    void getDisbursementDetails_withNullDetails_returnsEmptyResponse() {
        // Arrange
        lead.setDisbursementDetails(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        DisbursementDetailsResponse result = leadReadService.getDisbursementDetails(leadIdentifier);

        // Assert
        assertNotNull(result, "Should return empty response, not null");
        assertNull(result.getDisbursedAmount(), "All fields should be null");
    }

    @Test
    void getDisbursementDetails_withTranches_returnsMappedResponse() {
        // Arrange
        UUID trancheId = UUID.randomUUID();
        Lead.Tranche tranche = Lead.Tranche.builder()
                .identifier(trancheId).amount(BigDecimal.valueOf(500000)).date(LocalDate.of(2025, 6, 1)).build();
        Lead.DisbursementDetails disbursementDetails = Lead.DisbursementDetails.builder()
                .disbursedAmount(BigDecimal.valueOf(2000000))
                .roi(BigDecimal.valueOf(9.0))
                .tenureValue(240).tenureType(TenureType.MONTH)
                .disbursedDate(LocalDate.of(2025, 1, 15))
                .processingFees(BigDecimal.valueOf(10000))
                .tranches(List.of(tranche))
                .build();
        lead.setDisbursementDetails(disbursementDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        DisbursementDetailsResponse result = leadReadService.getDisbursementDetails(leadIdentifier);

        // Assert
        assertEquals(BigDecimal.valueOf(2000000), result.getDisbursedAmount(), "Disbursed amount should be mapped");
        assertEquals(1, result.getTranches().size(), "Should have one tranche");
        assertEquals(trancheId, result.getTranches().get(0).getIdentifier(), "Tranche identifier should be mapped");
        assertEquals(BigDecimal.valueOf(500000), result.getTranches().get(0).getAmount(), "Tranche amount should be mapped");
    }

    // ==================== getTrancheByIdentifier() Tests ====================

    @Test
    void getTrancheByIdentifier_withMatchingTranche_returnsTrancheResponse() {
        // Arrange
        UUID trancheId = UUID.randomUUID();
        Lead.Tranche tranche = Lead.Tranche.builder()
                .identifier(trancheId).amount(BigDecimal.valueOf(300000)).date(LocalDate.of(2025, 3, 1)).build();
        Lead.DisbursementDetails disbursementDetails = Lead.DisbursementDetails.builder()
                .tranches(List.of(tranche)).build();
        lead.setDisbursementDetails(disbursementDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        TrancheResponse result = leadReadService.getTrancheByIdentifier(leadIdentifier, trancheId);

        // Assert
        assertEquals(trancheId, result.getIdentifier(), "Tranche identifier should match");
        assertEquals(BigDecimal.valueOf(300000), result.getAmount(), "Tranche amount should match");
    }

    @Test
    void getTrancheByIdentifier_withNullDisbursementDetails_throwsRuntimeException() {
        // Arrange
        lead.setDisbursementDetails(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        UUID trancheId = UUID.randomUUID();

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> leadReadService.getTrancheByIdentifier(leadIdentifier, trancheId),
                "Should throw when disbursement details is null");
    }

    @Test
    void getTrancheByIdentifier_withNonMatchingTranche_throwsRuntimeException() {
        // Arrange
        Lead.Tranche tranche = Lead.Tranche.builder()
                .identifier(UUID.randomUUID()).amount(BigDecimal.TEN).build();
        Lead.DisbursementDetails disbursementDetails = Lead.DisbursementDetails.builder()
                .tranches(List.of(tranche)).build();
        lead.setDisbursementDetails(disbursementDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        UUID nonExistingId = UUID.randomUUID();

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> leadReadService.getTrancheByIdentifier(leadIdentifier, nonExistingId),
                "Should throw when tranche identifier does not match any tranche");
    }

    // ==================== getLeadBasicByIdentifier() Tests ====================

    @Test
    void getLeadBasicByIdentifier_withValidIdentifier_returnsMappedResponse() {
        // Arrange
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        LeadBasicResponse result = leadReadService.getLeadBasicByIdentifier(leadIdentifier);

        // Assert
        assertEquals(1L, result.getId(), "Lead ID should be mapped");
        assertEquals(leadIdentifier, result.getLeadIdentifier(), "Lead identifier should be mapped");
        assertEquals(LeadStatus.ACTIVE, result.getStatus(), "Lead status should be mapped");
        assertEquals("HL", result.getProductCode(), "Product code should be mapped");
    }

    // ==================== getLeadDashboard() Tests ====================

    @Test
    void getLeadDashboard_delegatesToDashboardWrapper() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        LeadDashboardFilters filters = new LeadDashboardFilters();
        PaginatedResponse<LeadDashboardResponse> expected = PaginatedResponse.<LeadDashboardResponse>builder()
                .content(List.of()).pagination(PaginationInfo.builder().totalElements(0L).build()).build();
        when(leadDashboardWrapper.findLeadDashboard(paginationRequest, filters)).thenReturn(expected);

        // Act
        PaginatedResponse<LeadDashboardResponse> result = leadReadService.getLeadDashboard(paginationRequest, filters);

        // Assert
        assertNotNull(result, "Response should not be null");
        verify(leadDashboardWrapper).findLeadDashboard(paginationRequest, filters);
    }

    // ==================== searchLeads() Tests ====================

    @Test
    void searchLeads_delegatesToRepositoryWrapper() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        LeadSearchRequest request = new LeadSearchRequest();
        PaginatedResponse<LeadSearchResponse> expected = PaginatedResponse.<LeadSearchResponse>builder()
                .content(List.of()).pagination(PaginationInfo.builder().totalElements(0L).build()).build();
        when(leadRepositoryWrapper.searchLeadsByPhoneNumber(paginationRequest, request)).thenReturn(expected);

        // Act
        PaginatedResponse<LeadSearchResponse> result = leadReadService.searchLeads(paginationRequest, request);

        // Assert
        assertNotNull(result, "Response should not be null");
        verify(leadRepositoryWrapper).searchLeadsByPhoneNumber(paginationRequest, request);
    }

    // ==================== hasLeadWithMobileNumber() Tests ====================

    @Test
    void hasLeadWithMobileNumber_whenExists_returnsTrue() {
        // Arrange
        when(leadRepositoryWrapper.existsLeadWithMobileNumber("9876543210")).thenReturn(true);

        // Act
        boolean result = leadReadService.hasLeadWithMobileNumber("9876543210");

        // Assert
        assertTrue(result, "Should return true when lead exists for mobile number");
    }

    @Test
    void hasLeadWithMobileNumber_whenNotExists_returnsFalse() {
        // Arrange
        when(leadRepositoryWrapper.existsLeadWithMobileNumber("0000000000")).thenReturn(false);

        // Act
        boolean result = leadReadService.hasLeadWithMobileNumber("0000000000");

        // Assert
        assertFalse(result, "Should return false when no lead exists for mobile number");
    }

    // ==================== findLeadsByPersonIdsAndStatusesAndSubstatuses() Tests ====================

    @Test
    void findLeadsByPersonIdsAndStatusesAndSubstatuses_delegatesToRepositoryWrapper() {
        // Arrange
        List<Long> personIds = List.of(1L, 2L);
        List<LeadStatus> statuses = List.of(LeadStatus.ACTIVE);
        List<LeadSubStatus> substatuses = List.of(LeadSubStatus.ONHOLD);
        List<LeadWorkflowDetailsDto> expected = List.of(LeadWorkflowDetailsDto.builder().leadId(1L).build());
        when(leadRepositoryWrapper.findLeadsByPersonIdsAndStatusesAndSubstatuses(personIds, statuses, substatuses))
                .thenReturn(expected);

        // Act
        List<LeadWorkflowDetailsDto> result = leadReadService.findLeadsByPersonIdsAndStatusesAndSubstatuses(
                personIds, statuses, substatuses);

        // Assert
        assertEquals(1, result.size(), "Should return results from repository wrapper");
    }

    // ==================== getLeadByReferralTrackingCode() Tests ====================

    @Test
    void getLeadByReferralTrackingCode_delegatesToRepositoryWrapper() {
        // Arrange
        LeadBasicResponse expected = LeadBasicResponse.builder().leadIdentifier(leadIdentifier).build();
        when(leadRepositoryWrapper.findLeadByReferralTrackingCodeWithException("REF-123")).thenReturn(expected);

        // Act
        LeadBasicResponse result = leadReadService.getLeadByReferralTrackingCode("REF-123");

        // Assert
        assertEquals(leadIdentifier, result.getLeadIdentifier(), "Lead identifier should match");
    }

    // ==================== getLeadsByEntity() Tests ====================

    @Test
    void getLeadsByEntity_delegatesToRepositoryWrapper() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        UUID entityId = UUID.randomUUID();
        PaginatedResponse<LeadBasicResponse> expected = PaginatedResponse.<LeadBasicResponse>builder()
                .content(List.of()).pagination(PaginationInfo.builder().totalElements(0L).build()).build();
        when(leadRepositoryWrapper.findLeadsByEntity(EntityType.ADVISOR, entityId, paginationRequest))
                .thenReturn(expected);

        // Act
        PaginatedResponse<LeadBasicResponse> result = leadReadService.getLeadsByEntity(
                EntityType.ADVISOR, entityId, paginationRequest);

        // Assert
        assertNotNull(result, "Response should not be null");
        verify(leadRepositoryWrapper).findLeadsByEntity(EntityType.ADVISOR, entityId, paginationRequest);
    }

    // ==================== getLeadsByReferralCode() Tests ====================

    @Test
    void getLeadsByReferralCode_delegatesToRepositoryWrapper() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        PaginatedResponse<LeadBasicResponse> expected = PaginatedResponse.<LeadBasicResponse>builder()
                .content(List.of()).pagination(PaginationInfo.builder().totalElements(0L).build()).build();
        when(leadRepositoryWrapper.findLeadsByReferralCode("ADV-001", paginationRequest)).thenReturn(expected);

        // Act
        PaginatedResponse<LeadBasicResponse> result = leadReadService.getLeadsByReferralCode("ADV-001", paginationRequest);

        // Assert
        assertNotNull(result, "Response should not be null");
        verify(leadRepositoryWrapper).findLeadsByReferralCode("ADV-001", paginationRequest);
    }

    // ==================== getCurrentCustomerFormStep() Tests ====================

    @Test
    void getCurrentCustomerFormStep_withExistingStep_returnsStep() {
        // Arrange
        Lead.OtherDetails otherDetails = Lead.OtherDetails.builder().currentCustomerFormStep("INCOME_DETAILS").build();
        lead.setOtherDetails(otherDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        CurrentCustomerFormStepResponse result = leadReadService.getCurrentCustomerFormStep(leadIdentifier);

        // Assert
        assertEquals("INCOME_DETAILS", result.getCurrentCustomerFormStep(), "Current step should be mapped from other details");
        verify(analyticsHelper).captureLead(any(AnalyticsEvent.class));
    }

    @Test
    void getCurrentCustomerFormStep_withNullOtherDetails_returnsNullStep() {
        // Arrange
        lead.setOtherDetails(null);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        CurrentCustomerFormStepResponse result = leadReadService.getCurrentCustomerFormStep(leadIdentifier);

        // Assert
        assertNull(result.getCurrentCustomerFormStep(), "Step should be null when other details is null");
        verify(analyticsHelper).captureLead(any(AnalyticsEvent.class));
    }

    // ==================== getLeadDashboardFilters() Tests ====================

    @Test
    void getLeadDashboardFilters_withNoOfficeFilter_returnsAllOfficesAndStaff() {
        // Arrange
        StaffResponse currentStaff = new StaffResponse();
        currentStaff.setOfficeKey("BRANCH-001");
        OfficeResponse currentOffice = new OfficeResponse();
        currentOffice.setCode("BR001");

        OfficeResponse hierarchyOffice = new OfficeResponse();
        hierarchyOffice.setKey("BRANCH-001");
        hierarchyOffice.setName("Branch One");

        StaffResponse staffInBranch = mock(StaffResponse.class, RETURNS_DEEP_STUBS);
        when(staffInBranch.getUserResponse().getPersonResponse().getDisplayName()).thenReturn("Jane");
        when(staffInBranch.getUserResponse().getUsername()).thenReturn("jane");

        when(staffReadService.getCurrentStaff()).thenReturn(currentStaff);
        when(officeReadService.getOfficeByKey("BRANCH-001")).thenReturn(currentOffice);
        when(officeReadService.getOfficesByCodePrefix("BR001")).thenReturn(List.of(hierarchyOffice));
        when(staffReadService.getStaffByOfficeKeys(List.of("BRANCH-001"))).thenReturn(List.of(staffInBranch));

        // Act
        LeadDashboardFiltersResponse result = leadReadService.getLeadDashboardFilters(null);

        // Assert
        assertEquals(1, result.getOffices().size(), "Should return offices in hierarchy");
        assertEquals("BRANCH-001", result.getOffices().get(0).getKey(), "Office key should match");
        assertEquals(1, result.getStaffs().size(), "Should return staff from all hierarchy offices");
        assertEquals("Jane", result.getStaffs().get(0).getDisplayName(), "Staff display name should be mapped");
    }

    // ==================== getCreditDetails() – branch coverage ====================

    @Test
    void getCreditDetails_withAllCodeValueFields_resolvesAllFields() {
        // Arrange
        Lead.CreditRatingDetails creditDetails = Lead.CreditRatingDetails.builder()
                .underwriter("UW-002")
                .occupationProfile("SALARIED")
                .roofProfile("CONCRETE")
                .ltv("HIGH")
                .foir("40_50")
                .monthlyFamilyIncome("ABOVE_1L")
                .propertyDocumentType("SALE_DEED")
                .eligibleLoanAmount(BigDecimal.valueOf(5000000))
                .location("URBAN")
                .bureauRating("A")
                .customerProfiles("PRIME")
                .build();
        lead.setCreditRatingDetails(creditDetails);

        CodeValueResponse cv = new CodeValueResponse();
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(codeValueMasterService.getByKey(anyString())).thenReturn(cv);

        // Act
        CreditDetailsResponse result = leadReadService.getCreditDetails(leadIdentifier);

        // Assert
        assertEquals("UW-002", result.getUnderwriter());
        assertEquals(cv, result.getOccupationProfile());
        assertEquals(cv, result.getRoofProfile());
        assertEquals(cv, result.getLtv());
        assertEquals(cv, result.getFoir());
        assertEquals(cv, result.getMonthlyFamilyIncome());
        assertEquals(cv, result.getPropertyDocumentType());
        assertEquals(cv, result.getLocation());
        assertEquals(cv, result.getBureauRating());
        assertEquals(cv, result.getCustomerProfiles());
    }

    @Test
    void getCreditDetails_withAllNullCodeValueFields_returnsNullCodeValues() {
        // Arrange
        Lead.CreditRatingDetails creditDetails = Lead.CreditRatingDetails.builder()
                .underwriter("UW-003")
                .eligibleLoanAmount(BigDecimal.valueOf(2000000))
                .build();
        lead.setCreditRatingDetails(creditDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        CreditDetailsResponse result = leadReadService.getCreditDetails(leadIdentifier);

        // Assert
        assertEquals("UW-003", result.getUnderwriter());
        assertNull(result.getOccupationProfile(), "Null occupationProfile should not resolve");
        assertNull(result.getRoofProfile(), "Null roofProfile should not resolve");
        assertNull(result.getLtv(), "Null ltv should not resolve");
        assertNull(result.getFoir(), "Null foir should not resolve");
        assertNull(result.getMonthlyFamilyIncome(), "Null monthlyFamilyIncome should not resolve");
        assertNull(result.getPropertyDocumentType(), "Null propertyDocumentType should not resolve");
        assertNull(result.getLocation(), "Null location should not resolve");
        assertNull(result.getBureauRating(), "Null bureauRating should not resolve");
        assertNull(result.getCustomerProfiles(), "Null customerProfiles should not resolve");
    }

    // ==================== getPropertyDetails() – branch coverage ====================

    @Test
    void getPropertyDetails_withNullAddress_returnsNullAddress() {
        // Arrange
        Lead.PropertyDetails propertyDetails = Lead.PropertyDetails.builder()
                .address(null).geoData(null).propertyType(null).propertyConstructionStage(null).build();
        Lead.OtherDetails otherDetails = Lead.OtherDetails.builder().propertyDetails(propertyDetails).build();
        lead.setOtherDetails(otherDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        PropertyDetailsResponse result = leadReadService.getPropertyDetails(leadIdentifier);

        // Assert
        assertNull(result.getAddress(), "Address should be null when not set");
        assertNull(result.getGeoData(), "GeoData should be null when not set");
    }

    @Test
    void getPropertyDetails_withNullMeasurementAndChecklist_returnsNulls() {
        // Arrange
        Lead.PropertyDetails propertyDetails = Lead.PropertyDetails.builder()
                .propertyMeasurementDetails(null).documentChecklist(null)
                .owner("Owner").build();
        Lead.OtherDetails otherDetails = Lead.OtherDetails.builder().propertyDetails(propertyDetails).build();
        lead.setOtherDetails(otherDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        PropertyDetailsResponse result = leadReadService.getPropertyDetails(leadIdentifier);

        // Assert
        assertNull(result.getPropertyMeasurementDetails(), "Measurement should be null when not set");
        assertNull(result.getDocumentChecklistResponse(), "Checklist should be null when not set");
        assertEquals("Owner", result.getOwner());
    }

    @Test
    void getPropertyDetails_withBlankPropertyTypeAndConstructionStage_returnsNullCodeValues() {
        // Arrange
        Lead.PropertyDetails propertyDetails = Lead.PropertyDetails.builder()
                .propertyType("").propertyConstructionStage("  ").build();
        Lead.OtherDetails otherDetails = Lead.OtherDetails.builder().propertyDetails(propertyDetails).build();
        lead.setOtherDetails(otherDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        PropertyDetailsResponse result = leadReadService.getPropertyDetails(leadIdentifier);

        // Assert
        assertNull(result.getPropertyType(), "Blank propertyType should not resolve to code value");
        assertNull(result.getPropertyConstructionStage(), "Blank constructionStage should not resolve to code value");
    }

    // ==================== getDocumentChecklist() – branch coverage ====================

    @Test
    void getDocumentChecklist_withNullPropertyDetails_returnsEmptyResponse() {
        // Arrange
        Lead.OtherDetails otherDetails = Lead.OtherDetails.builder().propertyDetails(null).build();
        lead.setOtherDetails(otherDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        DocumentChecklistResponse result = leadReadService.getDocumentChecklist(leadIdentifier);

        // Assert
        assertNotNull(result, "Should return empty response when propertyDetails is null");
        assertNull(result.getEkhataType());
    }

    @Test
    void getDocumentChecklist_withNullChecklist_returnsEmptyResponse() {
        // Arrange
        Lead.PropertyDetails propertyDetails = Lead.PropertyDetails.builder().documentChecklist(null).build();
        Lead.OtherDetails otherDetails = Lead.OtherDetails.builder().propertyDetails(propertyDetails).build();
        lead.setOtherDetails(otherDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        DocumentChecklistResponse result = leadReadService.getDocumentChecklist(leadIdentifier);

        // Assert
        assertNotNull(result, "Should return empty response when checklist is null");
        assertNull(result.getSaleDeed());
    }

    // ==================== getIncomeObligationDetails() – branch coverage ====================

    @Test
    void getIncomeObligationDetails_withBlankIncomeSource_returnsNullSource() {
        // Arrange
        Lead.IncomeDetails incomeDetail = Lead.IncomeDetails.builder()
                .incomeSource("").amount(BigDecimal.valueOf(50000)).build();
        Lead.IncomeObligationDetails details = Lead.IncomeObligationDetails.builder()
                .incomeDetails(List.of(incomeDetail)).build();
        lead.setIncomeObligationDetails(details);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        IncomeObligationDetailsResponse result = leadReadService.getIncomeObligationDetails(leadIdentifier);

        // Assert
        assertNotNull(result.getIncomeDetails());
        assertNull(result.getIncomeDetails().get(0).getIncomeSource(),
                "Blank income source should not resolve to code value");
        assertEquals(BigDecimal.valueOf(50000), result.getIncomeDetails().get(0).getAmount());
    }

    @Test
    void getIncomeObligationDetails_withNullObligationDetails_returnsNullObligations() {
        // Arrange
        Lead.IncomeObligationDetails details = Lead.IncomeObligationDetails.builder()
                .obligationDetails(null)
                .monthlyFamilyIncome(BigDecimal.valueOf(60000)).build();
        lead.setIncomeObligationDetails(details);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        IncomeObligationDetailsResponse result = leadReadService.getIncomeObligationDetails(leadIdentifier);

        // Assert
        assertNull(result.getObligations(), "Obligations should be null when obligationDetails is null");
        assertEquals(BigDecimal.valueOf(60000), result.getMonthlyFamilyIncome());
    }

    @Test
    void getIncomeObligationDetails_withNullDocumentChecklist_returnsNullChecklist() {
        // Arrange
        Lead.IncomeDetails incomeDetail = Lead.IncomeDetails.builder()
                .incomeSource("BUSINESS").amount(BigDecimal.valueOf(70000))
                .documentChecklist(null).build();
        Lead.IncomeObligationDetails details = Lead.IncomeObligationDetails.builder()
                .incomeDetails(List.of(incomeDetail)).build();
        lead.setIncomeObligationDetails(details);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(codeValueMasterService.getByKey("BUSINESS")).thenReturn(new CodeValueResponse());

        // Act
        IncomeObligationDetailsResponse result = leadReadService.getIncomeObligationDetails(leadIdentifier);

        // Assert
        assertNull(result.getIncomeDetails().get(0).getDocumentChecklist(),
                "Document checklist should be null when not set");
    }

    @Test
    void getIncomeObligationDetails_withEmptyDocumentChecklist_returnsNullChecklist() {
        // Arrange
        Lead.IncomeDetails incomeDetail = Lead.IncomeDetails.builder()
                .incomeSource("BUSINESS").amount(BigDecimal.valueOf(70000))
                .documentChecklist(List.of()).build();
        Lead.IncomeObligationDetails details = Lead.IncomeObligationDetails.builder()
                .incomeDetails(List.of(incomeDetail)).build();
        lead.setIncomeObligationDetails(details);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(codeValueMasterService.getByKey("BUSINESS")).thenReturn(new CodeValueResponse());

        // Act
        IncomeObligationDetailsResponse result = leadReadService.getIncomeObligationDetails(leadIdentifier);

        // Assert
        assertNull(result.getIncomeDetails().get(0).getDocumentChecklist(),
                "Empty document checklist should map to null");
    }

    @Test
    void getIncomeObligationDetails_withBlankDocumentType_returnsNullDocumentType() {
        // Arrange
        Lead.IncomeDocumentChecklist docChecklist = Lead.IncomeDocumentChecklist.builder()
                .documentType("  ").status("VERIFIED").build();
        Lead.IncomeDetails incomeDetail = Lead.IncomeDetails.builder()
                .incomeSource("BUSINESS").amount(BigDecimal.valueOf(70000))
                .documentChecklist(List.of(docChecklist)).build();
        Lead.IncomeObligationDetails details = Lead.IncomeObligationDetails.builder()
                .incomeDetails(List.of(incomeDetail)).build();
        lead.setIncomeObligationDetails(details);

        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        when(codeValueMasterService.getByKey("BUSINESS")).thenReturn(new CodeValueResponse());

        // Act
        IncomeObligationDetailsResponse result = leadReadService.getIncomeObligationDetails(leadIdentifier);

        // Assert
        assertNull(result.getIncomeDetails().get(0).getDocumentChecklist().get(0).getDocumentType(),
                "Blank document type should not resolve to code value");
    }

    // ==================== getDisbursementDetails() – branch coverage ====================

    @Test
    void getDisbursementDetails_withNullTranches_returnsNullTranches() {
        // Arrange
        Lead.DisbursementDetails disbursementDetails = Lead.DisbursementDetails.builder()
                .disbursedAmount(BigDecimal.valueOf(1000000))
                .tranches(null).build();
        lead.setDisbursementDetails(disbursementDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);

        // Act
        DisbursementDetailsResponse result = leadReadService.getDisbursementDetails(leadIdentifier);

        // Assert
        assertEquals(BigDecimal.valueOf(1000000), result.getDisbursedAmount());
        assertNull(result.getTranches(), "Tranches should be null when not set");
    }

    // ==================== getTrancheByIdentifier() – branch coverage ====================

    @Test
    void getTrancheByIdentifier_withNullTranches_throwsRuntimeException() {
        // Arrange
        Lead.DisbursementDetails disbursementDetails = Lead.DisbursementDetails.builder()
                .tranches(null).build();
        lead.setDisbursementDetails(disbursementDetails);
        when(leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier)).thenReturn(lead);
        UUID trancheId = UUID.randomUUID();

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> leadReadService.getTrancheByIdentifier(leadIdentifier, trancheId),
                "Should throw when tranches list is null");
    }

    // ==================== getLeadDashboardFilters() – branch coverage ====================

    @Test
    void getLeadDashboardFilters_withOfficeFilter_filtersStaffBySelectedOffices() {
        // Arrange
        StaffResponse currentStaff = new StaffResponse();
        currentStaff.setOfficeKey("BRANCH-001");
        OfficeResponse currentOffice = new OfficeResponse();
        currentOffice.setCode("BR001");

        OfficeResponse hierarchyOffice1 = new OfficeResponse();
        hierarchyOffice1.setKey("BRANCH-001");
        hierarchyOffice1.setName("Branch One");

        OfficeResponse filteredOffice = new OfficeResponse();
        filteredOffice.setKey("BRANCH-002");
        filteredOffice.setCode("BR001-SUB");

        StaffResponse staffInFiltered = mock(StaffResponse.class, RETURNS_DEEP_STUBS);
        when(staffInFiltered.getUserResponse().getPersonResponse().getDisplayName()).thenReturn("John");
        when(staffInFiltered.getUserResponse().getUsername()).thenReturn("john");

        when(staffReadService.getCurrentStaff()).thenReturn(currentStaff);
        when(officeReadService.getOfficeByKey("BRANCH-001")).thenReturn(currentOffice);
        when(officeReadService.getOfficesByCodePrefix("BR001")).thenReturn(List.of(hierarchyOffice1));
        when(officeReadService.getOfficeByKeys(List.of("BRANCH-002"))).thenReturn(List.of(filteredOffice));
        when(staffReadService.getStaffByOfficeKeys(List.of("BRANCH-002"))).thenReturn(List.of(staffInFiltered));

        LeadDashboardFiltersFilters filters = LeadDashboardFiltersFilters.builder()
                .offices(List.of("BRANCH-002")).build();

        // Act
        LeadDashboardFiltersResponse result = leadReadService.getLeadDashboardFilters(filters);

        // Assert
        assertEquals(1, result.getStaffs().size(), "Should return staff from filtered offices");
        assertEquals("John", result.getStaffs().get(0).getDisplayName());
        verify(officeReadService).getOfficeByKeys(List.of("BRANCH-002"));
    }

    // ==================== findLeadByPhoneNumber() Tests ====================

    @Test
    void findLeadByPhoneNumber_whenFound_returnsOptionalWithResponse() {
        // Arrange
        LeadBasicResponse expected = LeadBasicResponse.builder().leadIdentifier(leadIdentifier).build();
        when(leadRepositoryWrapper.findReusableLeadByPhoneNumber("9876543210")).thenReturn(Optional.of(expected));

        // Act
        Optional<LeadBasicResponse> result = leadReadService.findLeadByPhoneNumber("9876543210");

        // Assert
        assertTrue(result.isPresent(), "Should return non-empty Optional");
        assertSame(expected, result.get(), "Should return the response from repository wrapper");
        verify(leadRepositoryWrapper).findReusableLeadByPhoneNumber("9876543210");
    }

    // ==================== adminSearchLeads() Tests ====================

    @Test
    void adminSearchLeads_delegatesToRepositoryWrapper() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        AdminLeadSearchRequest request = new AdminLeadSearchRequest();
        PaginatedResponse<AdminLeadSearchResponse> expected = PaginatedResponse.<AdminLeadSearchResponse>builder()
                .content(List.of()).pagination(PaginationInfo.builder().totalElements(0L).build()).build();
        when(leadRepositoryWrapper.adminSearchLeadsByPhoneNumber(paginationRequest, request)).thenReturn(expected);

        // Act
        PaginatedResponse<AdminLeadSearchResponse> result = leadReadService.adminSearchLeads(paginationRequest, request);

        // Assert
        assertNotNull(result, "Response should not be null");
        verify(leadRepositoryWrapper).adminSearchLeadsByPhoneNumber(paginationRequest, request);
    }

    // ==================== getDeletedLeads() Tests ====================

    @Test
    void getDeletedLeads_delegatesToRepositoryWrapper() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest();
        PaginatedResponse<AdminLeadSearchResponse> expected = PaginatedResponse.<AdminLeadSearchResponse>builder()
                .content(List.of()).pagination(PaginationInfo.builder().totalElements(0L).build()).build();
        when(leadRepositoryWrapper.findDeletedLeads(paginationRequest)).thenReturn(expected);

        // Act
        PaginatedResponse<AdminLeadSearchResponse> result = leadReadService.getDeletedLeads(paginationRequest);

        // Assert
        assertNotNull(result, "Response should not be null");
        verify(leadRepositoryWrapper).findDeletedLeads(paginationRequest);
    }

    // ==================== findPrimaryPersonIdForLead() Tests ====================

    @Test
    void findPrimaryPersonIdForLead_delegatesToRepositoryWrapper() {
        // Arrange
        when(leadRepositoryWrapper.findPrimaryPersonIdForLead(leadIdentifier)).thenReturn(42L);

        // Act
        Long result = leadReadService.findPrimaryPersonIdForLead(leadIdentifier);

        // Assert
        assertEquals(42L, result, "Should return the primary person id from repository wrapper");
        verify(leadRepositoryWrapper).findPrimaryPersonIdForLead(leadIdentifier);
    }

    @Test
    void getLeadDashboardFilters_withOfficeFilterOutsideHierarchy_excludesNonHierarchyOffices() {
        // Arrange
        StaffResponse currentStaff = new StaffResponse();
        currentStaff.setOfficeKey("BRANCH-001");
        OfficeResponse currentOffice = new OfficeResponse();
        currentOffice.setCode("BR001");

        OfficeResponse hierarchyOffice = new OfficeResponse();
        hierarchyOffice.setKey("BRANCH-001");
        hierarchyOffice.setName("Branch One");

        OfficeResponse outsideOffice = new OfficeResponse();
        outsideOffice.setKey("OTHER-BRANCH");
        outsideOffice.setCode("ZZ999");

        when(staffReadService.getCurrentStaff()).thenReturn(currentStaff);
        when(officeReadService.getOfficeByKey("BRANCH-001")).thenReturn(currentOffice);
        when(officeReadService.getOfficesByCodePrefix("BR001")).thenReturn(List.of(hierarchyOffice));
        when(officeReadService.getOfficeByKeys(List.of("OTHER-BRANCH"))).thenReturn(List.of(outsideOffice));
        when(staffReadService.getStaffByOfficeKeys(List.of())).thenReturn(List.of());

        LeadDashboardFiltersFilters filters = LeadDashboardFiltersFilters.builder()
                .offices(List.of("OTHER-BRANCH")).build();

        // Act
        LeadDashboardFiltersResponse result = leadReadService.getLeadDashboardFilters(filters);

        // Assert
        assertEquals(0, result.getStaffs().size(),
                "Should return no staff when filtered office is outside hierarchy");
    }
}
