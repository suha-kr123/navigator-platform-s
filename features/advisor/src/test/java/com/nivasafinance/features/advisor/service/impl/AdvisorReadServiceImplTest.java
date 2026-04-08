package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.advisor.dto.AdvisorBasicResponse;
import com.nivasafinance.features.advisor.dto.AdvisorDashboardFilters;
import com.nivasafinance.features.advisor.dto.AdvisorDashboardResponse;
import com.nivasafinance.features.advisor.dto.AdvisorResponse;
import com.nivasafinance.features.advisor.dto.AdvisorSearchRequest;
import com.nivasafinance.features.advisor.dto.AdvisorTemplateResponse;
import com.nivasafinance.features.advisor.dto.SourcingDetailsResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.exception.AdvisorOperationException;
import com.nivasafinance.features.advisor.repository.AdvisorDashboardWrapper;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.offices.exception.OfficeNotFoundException;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.referral.dto.ReferralCodeRegistryResponse;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.referral.service.ReferralCodeRegistryService;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.repository.SourcingChannelRepositoryWrapper;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.service.StaffReadService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorReadServiceImplTest {

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Mock
    private UserReadService userReadService;

    @Mock
    private SourcingChannelRepositoryWrapper sourcingChannelRepositoryWrapper;

    @Mock
    private ReferralCodeRegistryService referralCodeRegistryService;

    @Mock
    private CodeMasterService codeMasterService;

    @Mock
    private OfficeReadService officeReadService;

    @Mock
    private AdvisorDashboardWrapper advisorDashboardWrapper;

    @Mock
    private MessageSource messageSource;

    @Mock
    private StaffReadService staffReadService;

    @InjectMocks
    private AdvisorReadServiceImpl advisorReadService;

    private static final String ADVISOR_USERNAME = "adv_user";
    private static final String REFERRER_USERNAME = "referrer_user";

    private UUID identifier;
    private Advisor advisor;
    private PersonResponse personResponse;
    private PaginationRequest paginationRequest;

    @BeforeEach
    void setUp() {
        identifier = UUID.randomUUID();
        paginationRequest = new PaginationRequest(0, 20, "createdAt", "DESC");

        advisor = new Advisor();
        advisor.setId(1L);
        advisor.setIdentifier(identifier);
        advisor.setUsername(ADVISOR_USERNAME);
        advisor.setStatus(AdvisorStatus.CREATED);
        advisor.setOfficeKey("HQ");
        advisor.setSourceChannelId(null);
        advisor.setReferralCode(null);

        personResponse = PersonResponse.builder()
                .firstName("John")
                .lastName("Doe")
                .mobileNumbers(null)
                .build();
    }

    @Test
    void getAdvisorByIdentifier_success_returnsMappedResponse() {
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(personResponse);
        when(officeReadService.getOfficeByKey("HQ")).thenReturn(new com.nivasafinance.features.offices.dto.OfficeResponse(1L, "Headquarters", "HQ", "HQ", null, null, true));

        AdvisorResponse result = advisorReadService.getAdvisorByIdentifier(identifier);

        assertNotNull(result);
        assertEquals(identifier, result.getIdentifier());
        assertEquals(AdvisorStatus.CREATED, result.getStatus());
        assertEquals("HQ", result.getOfficeKey());
        assertEquals("Headquarters", result.getOfficeName());
        verify(advisorRepositoryWrapper).findByIdentifierWithException(identifier);
        verify(userReadService).getPersonForUser(ADVISOR_USERNAME);
    }

    @Test
    void getAdvisorByIdentifier_officeKeyNull_setsOfficeNameNull() {
        advisor.setOfficeKey(null);
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(personResponse);

        AdvisorResponse result = advisorReadService.getAdvisorByIdentifier(identifier);

        assertNotNull(result);
        assertNull(result.getOfficeKey());
        assertNull(result.getOfficeName());
        verify(officeReadService, never()).getOfficeByKey(any());
    }

    @Test
    void getAdvisorByIdentifier_officeNotFound_setsOfficeNameNull() {
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(personResponse);
        when(messageSource.getMessage(eq("error.office.key.not.found"), any(Object[].class), any())).thenReturn("Not found");
        doThrow(new OfficeNotFoundException("HQ", messageSource)).when(officeReadService).getOfficeByKey("HQ");

        AdvisorResponse result = advisorReadService.getAdvisorByIdentifier(identifier);

        assertNotNull(result);
        assertNull(result.getOfficeName());
    }

    @Test
    void getSourcingDetails_nullSourceChannelId_returnsEmptyResponse() {
        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);

        SourcingDetailsResponse result = advisorReadService.getSourcingDetails(identifier);

        assertNotNull(result);
        assertNull(result.getSourcingChannelDetails());
        verify(sourcingChannelRepositoryWrapper, never()).findByIdAsResponseWithException(any());
    }

    @Test
    void getSourcingDetails_withSourceChannelId_returnsSanitizedResponse() {
        Long sourceChannelId = 10L;
        advisor.setSourceChannelId(sourceChannelId);
        SourcingChannelResponse channelResponse = new SourcingChannelResponse(1L, UUID.randomUUID(), new CodeValueResponse(), new CodeValueResponse(), null);

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(sourcingChannelRepositoryWrapper.findByIdAsResponseWithException(sourceChannelId)).thenReturn(channelResponse);

        SourcingDetailsResponse result = advisorReadService.getSourcingDetails(identifier);

        assertNotNull(result);
        assertNotNull(result.getSourcingChannelDetails());
        assertNotNull(result.getSourcingChannelDetails());
        verify(sourcingChannelRepositoryWrapper).findByIdAsResponseWithException(sourceChannelId);
    }

    @Test
    void getAdvisorTemplate_success_returnsTemplateWithAllMasters() {
        List<CodeValueResponse> rejectionReasons = List.of(new CodeValueResponse());
        List<CodeValueResponse> dormantReasons = List.of(new CodeValueResponse());
        List<CodeValueResponse> occupationTypes = List.of(new CodeValueResponse());
        List<CodeValueResponse> occupations = List.of(new CodeValueResponse());
        List<CodeValueResponse> qualifications = List.of(new CodeValueResponse());
        List<CodeValueResponse> segmentations = List.of(new CodeValueResponse());

        when(codeMasterService.getAllCodeValuesByCodeKey(anyString(), eq(true), eq("default")))
                .thenReturn(rejectionReasons)
                .thenReturn(dormantReasons)
                .thenReturn(occupationTypes)
                .thenReturn(occupations)
                .thenReturn(qualifications)
                .thenReturn(segmentations);

        AdvisorTemplateResponse result = advisorReadService.getAdvisorTemplate();

        assertNotNull(result);
        assertNotNull(result.getAdvisorRejectionReasons());
        assertNotNull(result.getAdvisorDormantReasons());
        assertNotNull(result.getOccupationTypes());
        assertNotNull(result.getOccupations());
        assertNotNull(result.getQualifications());
        assertNotNull(result.getSegmentations());
        verify(codeMasterService, times(6)).getAllCodeValuesByCodeKey(anyString(), eq(true), eq("default"));
    }

    @Test
    void getAllAdvisors_success_delegatesToWrapper() {
        PaginatedResponse<AdvisorBasicResponse> expected = new PaginatedResponse<>(List.of(), null);
        when(advisorRepositoryWrapper.findAllAdvisors(paginationRequest, "name", "9876543210")).thenReturn(expected);

        PaginatedResponse<AdvisorBasicResponse> result = advisorReadService.getAllAdvisors(paginationRequest, "name", "9876543210");

        assertNotNull(result);
        assertEquals(expected, result);
        verify(advisorRepositoryWrapper).findAllAdvisors(paginationRequest, "name", "9876543210");
    }

    @Test
    void searchAdvisors_success_delegatesToWrapper() {
        AdvisorSearchRequest searchRequest = new AdvisorSearchRequest();
        searchRequest.setMobileNumber("9876543210");
        PaginatedResponse<AdvisorBasicResponse> expected = new PaginatedResponse<>(List.of(), null);
        when(advisorRepositoryWrapper.searchAdvisorsByPhoneNumber(paginationRequest, searchRequest)).thenReturn(expected);

        PaginatedResponse<AdvisorBasicResponse> result = advisorReadService.searchAdvisors(paginationRequest, searchRequest);

        assertNotNull(result);
        verify(advisorRepositoryWrapper).searchAdvisorsByPhoneNumber(paginationRequest, searchRequest);
    }

    @Test
    void getAdvisorDashboard_success_delegatesToWrapper() {
        AdvisorDashboardFilters filters = new AdvisorDashboardFilters();
        PaginatedResponse<AdvisorDashboardResponse> expected = new PaginatedResponse<>(List.of(), null);
        when(advisorDashboardWrapper.findAdvisorDashboard(paginationRequest, filters)).thenReturn(expected);

        PaginatedResponse<AdvisorDashboardResponse> result = advisorReadService.getAdvisorDashboard(paginationRequest, filters);

        assertNotNull(result);
        verify(advisorDashboardWrapper).findAdvisorDashboard(paginationRequest, filters);
    }

    @Test
    void getMyAdvisors_nullUsername_throwsException() {
        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn(null);

            assertThrows(AdvisorOperationException.class,
                    () -> advisorReadService.getMyAdvisors(paginationRequest));

            verify(advisorRepositoryWrapper, never()).findAdvisorsByUsername(any(), any());
        }
    }

    @Test
    void getMyAdvisors_success_delegatesToWrapper() {
        String username = "user1";
        PaginatedResponse<AdvisorBasicResponse> expected = new PaginatedResponse<>(List.of(), null);
        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn(username);
            when(advisorRepositoryWrapper.findAdvisorsByUsername(username, paginationRequest)).thenReturn(expected);

            PaginatedResponse<AdvisorBasicResponse> result = advisorReadService.getMyAdvisors(paginationRequest);

            assertNotNull(result);
            verify(advisorRepositoryWrapper).findAdvisorsByUsername(username, paginationRequest);
        }
    }

    @Test
    void getAdvisorsByReferralCode_success_delegatesToWrapper() {
        String referralCode = "REF001";
        PaginatedResponse<AdvisorBasicResponse> expected = new PaginatedResponse<>(List.of(), null);
        when(advisorRepositoryWrapper.findAdvisorsByReferralCode(referralCode, paginationRequest)).thenReturn(expected);

        PaginatedResponse<AdvisorBasicResponse> result = advisorReadService.getAdvisorsByReferralCode(referralCode, paginationRequest);

        assertNotNull(result);
        verify(advisorRepositoryWrapper).findAdvisorsByReferralCode(referralCode, paginationRequest);
    }

    @Test
    void getAdvisorByIdentifier_withSourceChannelNoReferralCode_setsSourcingChannelOnly() {
        Long sourceChannelId = 10L;
        advisor.setSourceChannelId(sourceChannelId);
        SourcingChannelResponse channelResponse = new SourcingChannelResponse(1L, UUID.randomUUID(), null, null, null);

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(personResponse);
        when(officeReadService.getOfficeByKey("HQ")).thenReturn(new com.nivasafinance.features.offices.dto.OfficeResponse(1L, "HQ", "HQ", "HQ", null, null, true));
        when(sourcingChannelRepositoryWrapper.findByIdAsResponseWithException(sourceChannelId)).thenReturn(channelResponse);

        AdvisorResponse result = advisorReadService.getAdvisorByIdentifier(identifier);

        assertNotNull(result);
        assertNotNull(result.getSourcingChannelDetails());
        verify(referralCodeRegistryService, never()).getReferralCodeByCode(any());
    }

    @Test
    void getAdvisorByIdentifier_withReferralCodeAndRegistryNull_setsReferredByCodeOnly() {
        Long sourceChannelId = 10L;
        advisor.setSourceChannelId(sourceChannelId);
        SourcingChannelResponse channelResponse = new SourcingChannelResponse(1L, UUID.randomUUID(), null, null, null);
        com.nivasafinance.features.sourcechannel.entity.SourcingChannel.MarketingDetails marketingDetails =
                new com.nivasafinance.features.sourcechannel.entity.SourcingChannel.MarketingDetails();
        marketingDetails.setReferredByCode("REFCODE");
        channelResponse.setMarketingDetails(marketingDetails);

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(personResponse);
        when(officeReadService.getOfficeByKey("HQ")).thenReturn(new com.nivasafinance.features.offices.dto.OfficeResponse(1L, "HQ", "HQ", "HQ", null, null, true));
        when(sourcingChannelRepositoryWrapper.findByIdAsResponseWithException(sourceChannelId)).thenReturn(channelResponse);
        when(referralCodeRegistryService.getReferralCodeByCode("REFCODE")).thenReturn(null);

        AdvisorResponse result = advisorReadService.getAdvisorByIdentifier(identifier);

        assertNotNull(result);
        assertEquals("REFCODE", result.getReferredByCode());
        assertNull(result.getReferredByIdentifier());
        verify(referralCodeRegistryService).getReferralCodeByCode("REFCODE");
    }

    @Test
    void getAdvisorByIdentifier_withReferralCodeApplicant_resolvesReferrerFromApplicant() {
        Long sourceChannelId = 10L;
        UUID referrerId = UUID.randomUUID();
        advisor.setSourceChannelId(sourceChannelId);
        SourcingChannelResponse channelResponse = new SourcingChannelResponse(1L, UUID.randomUUID(), null, null, null);
        com.nivasafinance.features.sourcechannel.entity.SourcingChannel.MarketingDetails marketingDetails =
                new com.nivasafinance.features.sourcechannel.entity.SourcingChannel.MarketingDetails();
        marketingDetails.setReferredByCode("REF_APP");
        channelResponse.setMarketingDetails(marketingDetails);
        ReferralCodeRegistryResponse registry = ReferralCodeRegistryResponse.builder()
                .entityType(EntityType.APPLICANT)
                .entityIdentifier(referrerId)
                .build();
        AdvisorRepositoryWrapper.ReferrerDisplayInfo displayInfo =
                new AdvisorRepositoryWrapper.ReferrerDisplayInfo("Applicant Name", "9999999999");

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(personResponse);
        when(officeReadService.getOfficeByKey("HQ")).thenReturn(new com.nivasafinance.features.offices.dto.OfficeResponse(1L, "HQ", "HQ", "HQ", null, null, true));
        when(sourcingChannelRepositoryWrapper.findByIdAsResponseWithException(sourceChannelId)).thenReturn(channelResponse);
        when(referralCodeRegistryService.getReferralCodeByCode("REF_APP")).thenReturn(registry);
        when(advisorRepositoryWrapper.findReferrerDisplayInfo(EntityType.APPLICANT, referrerId))
                .thenReturn(Optional.of(displayInfo));

        AdvisorResponse result = advisorReadService.getAdvisorByIdentifier(identifier);

        assertNotNull(result);
        assertEquals("REF_APP", result.getReferredByCode());
        assertEquals(referrerId, result.getReferredByIdentifier());
        assertEquals(EntityType.APPLICANT, result.getReferredByType());
        assertEquals("Applicant Name", result.getReferredByName());
        assertEquals("9999999999", result.getReferredByNumber());
    }

    @Test
    void getAdvisorByIdentifier_withReferralCodeAdvisor_resolvesReferrerFromAdvisor() {
        Long sourceChannelId = 10L;
        UUID referrerAdvisorId = UUID.randomUUID();
        advisor.setSourceChannelId(sourceChannelId);
        SourcingChannelResponse channelResponse = new SourcingChannelResponse(1L, UUID.randomUUID(), null, null, null);
        com.nivasafinance.features.sourcechannel.entity.SourcingChannel.MarketingDetails marketingDetails =
                new com.nivasafinance.features.sourcechannel.entity.SourcingChannel.MarketingDetails();
        marketingDetails.setReferredByCode("REF_ADV");
        channelResponse.setMarketingDetails(marketingDetails);
        ReferralCodeRegistryResponse registry = ReferralCodeRegistryResponse.builder()
                .entityType(EntityType.ADVISOR)
                .entityIdentifier(referrerAdvisorId)
                .build();
        Advisor referrerAdvisor = new Advisor();
        referrerAdvisor.setUsername(REFERRER_USERNAME);
        MobileNumberDetails primaryMobile =
                new MobileNumberDetails("8888888888", true, false);
        PersonResponse referrerPersonResponse = PersonResponse.builder()
                .displayName("Referrer Advisor")
                .mobileNumbers(List.of(primaryMobile))
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(personResponse);
        when(officeReadService.getOfficeByKey("HQ")).thenReturn(new com.nivasafinance.features.offices.dto.OfficeResponse(1L, "HQ", "HQ", "HQ", null, null, true));
        when(sourcingChannelRepositoryWrapper.findByIdAsResponseWithException(sourceChannelId)).thenReturn(channelResponse);
        when(referralCodeRegistryService.getReferralCodeByCode("REF_ADV")).thenReturn(registry);
        when(advisorRepositoryWrapper.findByIdentifierWithException(referrerAdvisorId)).thenReturn(referrerAdvisor);
        when(userReadService.getPersonForUser(REFERRER_USERNAME)).thenReturn(referrerPersonResponse);

        AdvisorResponse result = advisorReadService.getAdvisorByIdentifier(identifier);

        assertNotNull(result);
        assertEquals("REF_ADV", result.getReferredByCode());
        assertEquals(referrerAdvisorId, result.getReferredByIdentifier());
        assertEquals(EntityType.ADVISOR, result.getReferredByType());
        assertEquals("Referrer Advisor", result.getReferredByName());
        assertEquals("8888888888", result.getReferredByNumber());
    }

    @Test
    void getAdvisorByIdentifier_personWithPrimaryMobile_extractsPrimaryNumber() {
        MobileNumberDetails primary =
                new MobileNumberDetails("9876543210", true, false);
        personResponse = PersonResponse.builder()
                .displayName("John Doe")
                .mobileNumbers(List.of(primary))
                .firstName("John")
                .lastName("Doe")
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(personResponse);
        when(officeReadService.getOfficeByKey("HQ")).thenReturn(new com.nivasafinance.features.offices.dto.OfficeResponse(1L, "HQ", "HQ", "HQ", null, null, true));

        AdvisorResponse result = advisorReadService.getAdvisorByIdentifier(identifier);

        assertNotNull(result);
        assertNotNull(result.getPersonalDetails());
        assertEquals(List.of(primary), result.getPersonalDetails().getMobileNumbers());
    }

    @Test
    void getAdvisorByIdentifier_withReferralCodeAndRegistryWithNullEntityIdentifier_setsCodeOnly() {
        Long sourceChannelId = 10L;
        advisor.setSourceChannelId(sourceChannelId);
        SourcingChannelResponse channelResponse = new SourcingChannelResponse(1L, UUID.randomUUID(), null, null, null);
        com.nivasafinance.features.sourcechannel.entity.SourcingChannel.MarketingDetails marketingDetails =
                new com.nivasafinance.features.sourcechannel.entity.SourcingChannel.MarketingDetails();
        marketingDetails.setReferredByCode("REF_NULL");
        channelResponse.setMarketingDetails(marketingDetails);
        ReferralCodeRegistryResponse registry = ReferralCodeRegistryResponse.builder()
                .entityType(EntityType.STAFF)
                .entityIdentifier(null)
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(personResponse);
        when(officeReadService.getOfficeByKey("HQ")).thenReturn(new com.nivasafinance.features.offices.dto.OfficeResponse(1L, "HQ", "HQ", "HQ", null, null, true));
        when(sourcingChannelRepositoryWrapper.findByIdAsResponseWithException(sourceChannelId)).thenReturn(channelResponse);
        when(referralCodeRegistryService.getReferralCodeByCode("REF_NULL")).thenReturn(registry);

        AdvisorResponse result = advisorReadService.getAdvisorByIdentifier(identifier);

        assertNotNull(result);
        assertEquals("REF_NULL", result.getReferredByCode());
        assertNull(result.getReferredByIdentifier());
        assertNull(result.getReferredByName());
        verify(staffReadService, never()).getStaffByIdentifier(any());
    }

    @Test
    void getAdvisorByIdentifier_withReferralCodeStaff_resolvesReferrerFromStaff() {
        Long sourceChannelId = 10L;
        UUID staffId = UUID.randomUUID();
        advisor.setSourceChannelId(sourceChannelId);
        SourcingChannelResponse channelResponse = new SourcingChannelResponse(1L, UUID.randomUUID(), null, null, null);
        com.nivasafinance.features.sourcechannel.entity.SourcingChannel.MarketingDetails marketingDetails =
                new com.nivasafinance.features.sourcechannel.entity.SourcingChannel.MarketingDetails();
        marketingDetails.setReferredByCode("REF_STAFF");
        channelResponse.setMarketingDetails(marketingDetails);
        ReferralCodeRegistryResponse registry = ReferralCodeRegistryResponse.builder()
                .entityType(EntityType.STAFF)
                .entityIdentifier(staffId)
                .build();
        com.nivasafinance.features.person.entity.MobileNumberDetails staffPrimary =
                new com.nivasafinance.features.person.entity.MobileNumberDetails("7777777777", true, false);
        PersonResponse staffPersonResponse = PersonResponse.builder()
                .displayName("Staff Referrer")
                .mobileNumbers(List.of(staffPrimary))
                .build();
        UserResponse userResponse = UserResponse.builder().personResponse(staffPersonResponse).build();
        StaffResponse staffResponse = StaffResponse.builder().userResponse(userResponse).build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(personResponse);
        when(officeReadService.getOfficeByKey("HQ")).thenReturn(new com.nivasafinance.features.offices.dto.OfficeResponse(1L, "HQ", "HQ", "HQ", null, null, true));
        when(sourcingChannelRepositoryWrapper.findByIdAsResponseWithException(sourceChannelId)).thenReturn(channelResponse);
        when(referralCodeRegistryService.getReferralCodeByCode("REF_STAFF")).thenReturn(registry);
        when(staffReadService.getStaffByIdentifier(staffId)).thenReturn(Optional.of(staffResponse));

        AdvisorResponse result = advisorReadService.getAdvisorByIdentifier(identifier);

        assertNotNull(result);
        assertEquals("REF_STAFF", result.getReferredByCode());
        assertEquals(staffId, result.getReferredByIdentifier());
        assertEquals(EntityType.STAFF, result.getReferredByType());
        assertEquals("Staff Referrer", result.getReferredByName());
        assertEquals("7777777777", result.getReferredByNumber());
    }

    @Test
    void getAdvisorByIdentifier_personWithMobileNumbersNonePrimary_returnsNullForPrimary() {
        MobileNumberDetails secondary =
                new MobileNumberDetails("9876543210", false, false);
        personResponse = PersonResponse.builder()
                .displayName("John Doe")
                .mobileNumbers(List.of(secondary))
                .firstName("John")
                .lastName("Doe")
                .build();

        when(advisorRepositoryWrapper.findByIdentifierWithException(identifier)).thenReturn(advisor);
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(personResponse);
        when(officeReadService.getOfficeByKey("HQ")).thenReturn(new com.nivasafinance.features.offices.dto.OfficeResponse(1L, "HQ", "HQ", "HQ", null, null, true));

        AdvisorResponse result = advisorReadService.getAdvisorByIdentifier(identifier);

        assertNotNull(result);
        assertNotNull(result.getPersonalDetails());
        assertTrue(result.getPersonalDetails().getMobileNumbers() != null && !result.getPersonalDetails().getMobileNumbers().isEmpty());
    }

    @Test
    void findAdvisorByUsername_null_returnsEmptyWithoutCallingWrapper() {
        assertTrue(advisorReadService.findAdvisorByUsername(null).isEmpty());
        verify(advisorRepositoryWrapper, never()).findByUsername(any());
    }

    @Test
    void findAdvisorByUsername_blank_returnsEmptyWithoutCallingWrapper() {
        assertTrue(advisorReadService.findAdvisorByUsername("   ").isEmpty());
        verify(advisorRepositoryWrapper, never()).findByUsername(any());
    }

    @Test
    void findAdvisorByUsername_valid_delegatesToWrapper() {
        when(advisorRepositoryWrapper.findByUsername("u1")).thenReturn(Optional.of(advisor));

        Optional<Advisor> result = advisorReadService.findAdvisorByUsername("u1");

        assertTrue(result.isPresent());
        assertEquals(advisor, result.get());
        verify(advisorRepositoryWrapper).findByUsername("u1");
    }

    @Test
    void findAdvisorByMobileNo_success_delegatesToWrapper() {
        Optional<AdvisorBasicResponse> expected = Optional.of(new AdvisorBasicResponse());
        when(advisorRepositoryWrapper.findAdvisorByMobileNo("9876543210")).thenReturn(expected);

        Optional<AdvisorBasicResponse> result = advisorReadService.findAdvisorByMobileNo("9876543210");

        assertEquals(expected, result);
        verify(advisorRepositoryWrapper).findAdvisorByMobileNo("9876543210");
    }

}
