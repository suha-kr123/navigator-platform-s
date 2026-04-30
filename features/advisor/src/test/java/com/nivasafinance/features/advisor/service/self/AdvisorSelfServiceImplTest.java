package com.nivasafinance.features.advisor.service.self;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.advisor.dto.AdvisorBasicResponse;
import com.nivasafinance.features.advisor.dto.AdvisorResponse;
import com.nivasafinance.features.advisor.dto.BankDetailsResponse;
import com.nivasafinance.features.advisor.dto.MobileNumberDetails;
import com.nivasafinance.features.advisor.dto.PersonalDetails;
import com.nivasafinance.features.advisor.dto.UpdateAdvisorRequest;
import com.nivasafinance.features.advisor.dto.UpdateQualificationDetailsRequest;
import com.nivasafinance.features.advisor.dto.self.AdvisorSelfLeadCheckRequest;
import com.nivasafinance.features.advisor.dto.self.AdvisorSelfLeadCheckResponse;
import com.nivasafinance.features.advisor.dto.self.AdvisorSelfLeadCreateRequest;
import com.nivasafinance.features.advisor.dto.self.AdvisorSelfLeadCreateResponse;
import com.nivasafinance.features.advisor.dto.self.AdvisorSelfLeadResponse;
import com.nivasafinance.features.advisor.dto.self.AdvisorSelfLeadStageHistoryResponse;
import com.nivasafinance.features.advisor.dto.self.SelfAddBankDetailsRequest;
import com.nivasafinance.features.advisor.dto.self.SelfAddBankDetailsResponse;
import com.nivasafinance.features.advisor.dto.self.SelfAddressRequest;
import com.nivasafinance.features.advisor.dto.self.SelfAdvisorDashboard;
import com.nivasafinance.features.advisor.dto.self.SelfAdvisorDashboardResponse;
import com.nivasafinance.features.advisor.dto.self.SelfAddressResponse;
import com.nivasafinance.features.advisor.dto.self.SelfAdvisorProfileRequest;
import com.nivasafinance.features.advisor.dto.self.SelfAdvisorResponse;
import com.nivasafinance.features.advisor.dto.self.SelfBankDetailsRequest;
import com.nivasafinance.features.advisor.dto.self.SelfOccupationDetailsRequest;
import com.nivasafinance.features.advisor.dto.self.SelfPersonalDetailsRequest;
import com.nivasafinance.features.advisor.dto.self.SelfQualificationDetailsRequest;
import com.nivasafinance.features.advisor.dto.self.SelfSendOtpRequest;
import com.nivasafinance.features.advisor.dto.self.SelfVerifyOtpRequest;
import com.nivasafinance.features.advisor.dto.self.SelfVerifyOtpResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import com.nivasafinance.features.advisor.exception.AdvisorOperationException;
import com.nivasafinance.features.advisor.repository.AdvisorDashboardWrapper;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorAddressReadService;
import com.nivasafinance.features.advisor.service.AdvisorAddressWriteService;
import com.nivasafinance.features.advisor.service.AdvisorBankDetailsReadService;
import com.nivasafinance.features.advisor.service.AdvisorBankDetailsWriteService;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.LeadBasicResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.enums.LeadStatus;
import com.nivasafinance.features.lead.exception.LeadNotFoundException;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryDisplayResponse;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.lead.service.LeadContactWriteService;
import com.nivasafinance.features.leadstages.service.LeadStageHistoryReadService;
import com.nivasafinance.features.master.products.dto.ProductResponse;
import com.nivasafinance.features.master.products.service.ProductReadService;
import com.nivasafinance.integrations.framework.ServiceFactory;
import com.nivasafinance.integrations.framework.config.ThirdPartyServiceList;
import com.nivasafinance.services.authentication.AuthenticationHandler;
import com.nivasafinance.services.authentication.dto.AuthVerifyOtpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorSelfServiceImplTest {

    private static final String SELF_USER = "self_adv_user";

    @Mock
    private AdvisorReadService advisorReadService;
    @Mock
    private AdvisorWriteService advisorWriteService;
    @Mock
    private AdvisorAddressReadService advisorAddressReadService;
    @Mock
    private AdvisorAddressWriteService advisorAddressWriteService;
    @Mock
    private AdvisorBankDetailsReadService advisorBankDetailsReadService;
    @Mock
    private AdvisorBankDetailsWriteService advisorBankDetailsWriteService;
    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;
    @Mock
    private AdvisorDashboardWrapper advisorDashboardWrapper;
    @Mock
    private MessageSource messageSource;
    @Mock
    private ServiceFactory<AuthenticationHandler> authenticationServiceFactory;
    @Mock
    private AuthenticationHandler authenticationHandler;
    @Mock
    private LeadReadService leadReadService;
    @Mock
    private LeadWriteService leadWriteService;
    @Mock
    private LeadContactWriteService leadContactWriteService;
    @Mock
    private LeadStageHistoryReadService leadStageHistoryReadService;
    @Mock
    private ProductReadService productReadService;

    @InjectMocks
    private AdvisorSelfServiceImpl advisorSelfService;

    private UUID meId;
    private Advisor advisorEntity;

    @BeforeEach
    void setUp() {
        meId = UUID.randomUUID();
        advisorEntity = new Advisor();
        advisorEntity.setId(1L);
        advisorEntity.setIdentifier(meId);
        advisorEntity.setUsername(SELF_USER);
        advisorEntity.setReferralCode("REF001");

        lenient().when(messageSource.getMessage(anyString(), any(), any())).thenReturn("err");
    }

    private void stubLoggedInAdvisor() {
        when(advisorRepositoryWrapper.findByUsername(SELF_USER)).thenReturn(Optional.of(advisorEntity));
        lenient().when(advisorRepositoryWrapper.findByIdentifierWithException(meId)).thenReturn(advisorEntity);
    }

    @Test
    void getMyProfile_success_mapsAddressesBanksAndReferredBy() {
        AdvisorResponse full = new AdvisorResponse();
        full.setStatus(AdvisorStatus.ACTIVE);
        full.setPersonalDetails(PersonalDetails.builder().firstName("A").build());
        full.setReferredByName("Referrer");
        full.setReferredByType(com.nivasafinance.features.referral.enums.EntityType.ADVISOR);

        AddressData addr = new AddressData();
        addr.setId("a1");
        BankDetailsResponse bank = BankDetailsResponse.builder()
                .bankIdentifier(UUID.randomUUID())
                .nameAsPerPassbook("N")
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorReadService.getAdvisorByIdentifier(meId)).thenReturn(full);
            when(advisorAddressReadService.getAddresses(meId)).thenReturn(List.of(addr));
            when(advisorBankDetailsReadService.getAllBankDetails(meId)).thenReturn(List.of(bank));

            SelfAdvisorResponse result = advisorSelfService.getMyProfile();

            assertEquals(AdvisorStatus.ACTIVE, result.getStatus());
            assertEquals(1, result.getAddressDetails().size());
            assertEquals(1, result.getBankDetails().size());
            assertNotNull(result.getReferredByDetails());
            assertEquals("Referrer", result.getReferredByDetails().getReferredByName());
        }
    }

    @Test
    void getMyProfile_nullAddressesAndBanks_returnsEmptyLists() {
        AdvisorResponse full = new AdvisorResponse();
        full.setStatus(AdvisorStatus.CREATED);

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorReadService.getAdvisorByIdentifier(meId)).thenReturn(full);
            when(advisorAddressReadService.getAddresses(meId)).thenReturn(null);
            when(advisorBankDetailsReadService.getAllBankDetails(meId)).thenReturn(null);

            SelfAdvisorResponse result = advisorSelfService.getMyProfile();

            assertNotNull(result.getAddressDetails());
            assertTrue(result.getAddressDetails().isEmpty());
            assertNotNull(result.getBankDetails());
            assertTrue(result.getBankDetails().isEmpty());
            assertNull(result.getReferredByDetails());
        }
    }

    @Test
    void updateMyProfile_qualificationOnly_updatesQualificationNotAdvisor() {
        SelfAdvisorProfileRequest request = SelfAdvisorProfileRequest.builder()
                .qualificationDetails(Optional.of(
                        SelfQualificationDetailsRequest.builder()
                                .highestQualification(Optional.of("PG"))
                                .build()))
                .build();

        AdvisorResponse existing = new AdvisorResponse();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorReadService.getAdvisorByIdentifier(meId)).thenReturn(existing);

            advisorSelfService.updateMyProfile(request);

            verify(advisorWriteService, never()).updateAdvisor(eq(meId), any());
            verify(advisorWriteService).updateQualificationDetails(eq(meId), any(UpdateQualificationDetailsRequest.class));
        }
    }

    @Test
    void updateMyProfile_withMobileNumberDetails_callsUpdateAdvisor() {
        SelfAdvisorProfileRequest request = SelfAdvisorProfileRequest.builder()
                .mobileNumberDetails(new MobileNumberDetails("9876543210", true, false))
                .build();
        AdvisorResponse existing = new AdvisorResponse();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorReadService.getAdvisorByIdentifier(meId)).thenReturn(existing);

            advisorSelfService.updateMyProfile(request);

            verify(advisorWriteService).updateAdvisor(eq(meId), any(UpdateAdvisorRequest.class));
        }
    }

    @Test
    void addMyAddress_firstAddress_triggersActivateAdvisor() {
        SelfAddressRequest req = SelfAddressRequest.builder().build();
        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorAddressReadService.getAddresses(meId)).thenReturn(null);
            when(advisorAddressWriteService.addAddress(eq(meId), any())).thenReturn("addr-new");

            advisorSelfService.addMyAddress(req);

            verify(advisorWriteService).activateAdvisor(meId);
        }
    }

    @Test
    void addMyAddress_existingAddresses_skipsActivateAdvisor() {
        SelfAddressRequest req = SelfAddressRequest.builder().build();
        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorAddressReadService.getAddresses(meId)).thenReturn(List.of(new AddressData()));
            when(advisorAddressWriteService.addAddress(eq(meId), any())).thenReturn("addr-2");

            advisorSelfService.addMyAddress(req);

            verify(advisorWriteService, never()).activateAdvisor(any());
        }
    }

    @Test
    void getMyAddresses_null_returnsEmptyList() {
        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorAddressReadService.getAddresses(meId)).thenReturn(null);

            assertTrue(advisorSelfService.getMyAddresses().isEmpty());
        }
    }

    @Test
    void addMyBankDetails_missingRequired_throwsBadRequest() {
        SelfAddBankDetailsRequest request = SelfAddBankDetailsRequest.builder()
                .nameAsPerPassbook(Optional.of(""))
                .accountNo(Optional.of("1"))
                .ifscCode(Optional.of("IFSC"))
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();

            assertThrows(BadRequestException.class, () -> advisorSelfService.addMyBankDetails(request));
        }
        verify(advisorBankDetailsWriteService, never()).addBankDetails(any(), any());
    }

    @Test
    void addMyBankDetails_success() {
        UUID bankId = UUID.randomUUID();
        SelfAddBankDetailsRequest request = SelfAddBankDetailsRequest.builder()
                .nameAsPerPassbook(Optional.of("N"))
                .accountNo(Optional.of("123"))
                .ifscCode(Optional.of("IFSC0001"))
                .isPrimary(Optional.of(false))
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorBankDetailsWriteService.addBankDetails(eq(meId), any())).thenReturn(bankId);

            SelfAddBankDetailsResponse result = advisorSelfService.addMyBankDetails(request);

            assertEquals(bankId, result.getBankDetailsIdentifier());
        }
    }

    @Test
    void getMyBankDetails_unknownBank_throwsAdvisorOperationException() {
        UUID unknown = UUID.randomUUID();
        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorBankDetailsReadService.getAllBankDetails(meId)).thenReturn(Collections.emptyList());

            assertThrows(AdvisorOperationException.class, () -> advisorSelfService.getMyBankDetails(unknown));
        }
    }

    @Test
    void sendOtp_blankMobile_throwsBadRequest() {
        assertThrows(BadRequestException.class, () -> advisorSelfService.sendOtp(new SelfSendOtpRequest("  ")));
        verify(authenticationServiceFactory, never()).getHandler(any());
    }

    @Test
    void sendOtp_success_delegatesToAuthenticationHandler() {
        when(authenticationServiceFactory.getHandler(ThirdPartyServiceList.AUTHENTICATION)).thenReturn(authenticationHandler);

        advisorSelfService.sendOtp(new SelfSendOtpRequest("9876543210"));

        verify(authenticationHandler).sendOtp(any(), any());
    }

    @Test
    void verifyOtp_existingUser_returnsTokensAndExistingUsername() {
        when(authenticationServiceFactory.getHandler(ThirdPartyServiceList.AUTHENTICATION)).thenReturn(authenticationHandler);
        AuthVerifyOtpResponse auth = AuthVerifyOtpResponse.builder()
                .accessToken("a")
                .refreshToken("r")
                .expiresAt(99L)
                .build();
        when(authenticationHandler.verifyOtp(any(), any())).thenReturn(auth);
        AdvisorBasicResponse basic = new AdvisorBasicResponse();
        basic.setUsername("known_user");
        when(advisorRepositoryWrapper.findAdvisorByMobileNo("9876543210")).thenReturn(Optional.of(basic));

        SelfVerifyOtpResponse result = advisorSelfService.verifyOtp(new SelfVerifyOtpRequest("9876543210", "123456"));

        assertFalse(result.isNewUser());
        assertEquals("known_user", result.getUsername());
        assertEquals("a", result.getAccessToken());
    }

    @Test
    void verifyOtp_newUser_createsAdvisor() {
        when(authenticationServiceFactory.getHandler(ThirdPartyServiceList.AUTHENTICATION)).thenReturn(authenticationHandler);
        when(authenticationHandler.verifyOtp(any(), any())).thenReturn(
                AuthVerifyOtpResponse.builder().accessToken("a").refreshToken("r").expiresAt(1L).build());
        when(advisorRepositoryWrapper.findAdvisorByMobileNo("9876543210")).thenReturn(Optional.empty());

        SelfVerifyOtpResponse result = advisorSelfService.verifyOtp(new SelfVerifyOtpRequest("9876543210", "123456"));

        assertTrue(result.isNewUser());
        verify(advisorWriteService).createAdvisor(any());
    }

    @Test
    void verifyOtp_blankOtp_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> advisorSelfService.verifyOtp(new SelfVerifyOtpRequest("9876543210", "")));
    }

    @Test
    void checkAdvisorSelfLead_delegatesToLeadReadService() {
        when(leadReadService.hasLeadWithMobileNumber("9999999999")).thenReturn(true);

        AdvisorSelfLeadCheckResponse result =
                advisorSelfService.checkAdvisorSelfLead(new AdvisorSelfLeadCheckRequest("9999999999"));

        assertTrue(Boolean.TRUE.equals(result.getExists()));
    }

    @Test
    void createAdvisorSelfLead_noReferral_throwsBadRequest() {
        advisorEntity.setReferralCode(null);
        AdvisorSelfLeadCreateRequest request = AdvisorSelfLeadCreateRequest.builder()
                .mobileNumber("9876543210")
                .requestedAmount(BigDecimal.ONE)
                .product("P1")
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();

            assertThrows(BadRequestException.class, () -> advisorSelfService.createAdvisorSelfLead(request));
        }
        verify(leadWriteService, never()).createLead(any());
    }

    @Test
    void createAdvisorSelfLead_success() {
        AdvisorSelfLeadCreateRequest request = AdvisorSelfLeadCreateRequest.builder()
                .mobileNumber("9876543210")
                .requestedAmount(BigDecimal.ONE)
                .product("P1")
                .build();
        UUID leadId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();
        when(leadWriteService.createLead(any())).thenReturn(
                CreateLeadResponse.builder().leadIdentifier(leadId).contactIdentifier(contactId).build());

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();

            AdvisorSelfLeadCreateResponse result = advisorSelfService.createAdvisorSelfLead(request);

            assertEquals(leadId, result.getLeadIdentifier());
            assertEquals(contactId, result.getContactIdentifier());
            verify(leadWriteService).createLead(any());
        }
    }

    @Test
    void getSelfAdvisorLeads_noReferral_throwsBadRequest() {
        advisorEntity.setReferralCode(" ");
        PaginationRequest p = new PaginationRequest(0, 20, "createdAt", "DESC");

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();

            assertThrows(BadRequestException.class, () -> advisorSelfService.getSelfAdvisorLeads(p));
        }
    }

    @Test
    void getSelfAdvisorLeads_success() {
        advisorEntity.setReferralCode("REF001");
        PaginationRequest p = new PaginationRequest(0, 20, "createdAt", "DESC");
        LeadBasicResponse basic = LeadBasicResponse.builder()
                .leadIdentifier(UUID.randomUUID())
                .primaryContactName("Lead")
                .primaryContactPhone("9876500000")
                .requestedAmount(BigDecimal.TEN)
                .status(LeadStatus.ACTIVE)
                .productCode("HL")
                .build();
        PaginatedResponse<LeadBasicResponse> page = new PaginatedResponse<>(List.of(basic), null);

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(leadReadService.getLeadsByReferralCode("REF001", p)).thenReturn(page);
            when(productReadService.getProductByCode("HL")).thenReturn(new ProductResponse(1L, "HL", "Home Loan"));
            when(leadStageHistoryReadService.getStageHistoryWithDisplayLabelsByLeadId(basic.getLeadIdentifier()))
                    .thenReturn(Collections.emptyList());

            PaginatedResponse<AdvisorSelfLeadResponse> result = advisorSelfService.getSelfAdvisorLeads(p);

            assertEquals(1, result.getContent().size());
            assertEquals("Home Loan", result.getContent().get(0).getLoanType());
        }
    }

    @Test
    void getMyDashboard_success() {
        SelfAdvisorDashboard dash = SelfAdvisorDashboard.builder()
                .name("Adv")
                .salesOwner("Owner")
                .salesOwnerMobile("111")
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorDashboardWrapper.getSelfDashboard(meId)).thenReturn(Optional.of(dash));
            when(advisorDashboardWrapper.getLeadCountsByStatusForAdvisor(meId)).thenReturn(Collections.emptyList());
            when(advisorRepositoryWrapper.getTotalPaidByReferralCode("REF001")).thenReturn(BigDecimal.valueOf(5000));

            SelfAdvisorDashboardResponse result = advisorSelfService.getMyDashboard();

            assertEquals("Adv", result.getAdvisorName());
            assertEquals("Owner", result.getSalesOwner());
            assertEquals(BigDecimal.valueOf(5000), result.getTotalPayout());
        }
    }

    @Test
    void getMyDashboard_empty_throwsNotFoundForCurrentUser() {
        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorDashboardWrapper.getSelfDashboard(meId)).thenReturn(Optional.empty());

            assertThrows(AdvisorOperationException.class, () -> advisorSelfService.getMyDashboard());
        }
    }

    @Test
    void resolveMe_blankUsername_throwsNoCurrentUser() {
        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(" ");

            assertThrows(AdvisorOperationException.class, () -> advisorSelfService.getMyAddresses());
        }
    }

    @Test
    void getSelfAdvisorLeadByLeadId_mismatchedReferral_throwsBadRequest() {
        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            UUID leadId = UUID.randomUUID();
            LeadResponse lead = LeadResponse.builder()
                    .leadIdentifier(leadId)
                    .referredByCode("OTHER")
                    .build();
            when(leadReadService.getLeadByIdentifier(leadId)).thenReturn(lead);

            assertThrows(BadRequestException.class, () -> advisorSelfService.getSelfAdvisorLeadByLeadId(leadId));
        }
    }

    @Test
    void updateMyBankDetails_success() {
        UUID bankId = UUID.randomUUID();
        BankDetailsResponse existing = BankDetailsResponse.builder()
                .bankIdentifier(bankId)
                .isPrimary(false)
                .nameAsPerPassbook("Old")
                .accountNo("1")
                .ifscCode("IFSC")
                .bankName("B")
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorBankDetailsReadService.getAllBankDetails(meId)).thenReturn(List.of(existing));

            SelfBankDetailsRequest req = SelfBankDetailsRequest.builder()
                    .nameAsPerPassbook(Optional.of("New"))
                    .build();

            advisorSelfService.updateMyBankDetails(bankId, req);

            verify(advisorBankDetailsWriteService).updateBankDetails(eq(meId), eq(bankId), any());
        }
    }

    @Test
    void updateMyProfile_occupationOnly_callsUpdateOccupation() {
        SelfAdvisorProfileRequest request = SelfAdvisorProfileRequest.builder()
                .occupationDetails(Optional.of(
                        SelfOccupationDetailsRequest.builder()
                                .occupationType(Optional.of("SALARIED"))
                                .occupation(Optional.of("ENGINEER"))
                                .build()))
                .build();
        AdvisorResponse existing = new AdvisorResponse();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorReadService.getAdvisorByIdentifier(meId)).thenReturn(existing);

            advisorSelfService.updateMyProfile(request);

            verify(advisorWriteService, never()).updateAdvisor(eq(meId), any());
            verify(advisorWriteService).updateOccupationDetails(eq(meId), any());
        }
    }

    @Test
    void updateMyProfile_qualificationOptionalEmpty_skipsQualificationUpdate() {
        SelfAdvisorProfileRequest request = SelfAdvisorProfileRequest.builder()
                .qualificationDetails(Optional.empty())
                .build();
        AdvisorResponse existing = new AdvisorResponse();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorReadService.getAdvisorByIdentifier(meId)).thenReturn(existing);

            advisorSelfService.updateMyProfile(request);

            verify(advisorWriteService, never()).updateQualificationDetails(any(), any());
        }
    }

    @Test
    void updateMyProfile_personalDetailsWithMobiles_callsUpdateAdvisor() {
        MobileNumberDetails m = new MobileNumberDetails("9876543210", true, false);
        SelfPersonalDetailsRequest inner = SelfPersonalDetailsRequest.builder()
                .firstName(Optional.of("F"))
                .mobileNumbers(Optional.of(List.of(m)))
                .build();
        SelfAdvisorProfileRequest request = SelfAdvisorProfileRequest.builder()
                .personalDetails(Optional.of(inner))
                .build();
        AdvisorResponse existing = new AdvisorResponse();
        existing.setPersonalDetails(PersonalDetails.builder().firstName("Old").build());

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorReadService.getAdvisorByIdentifier(meId)).thenReturn(existing);

            advisorSelfService.updateMyProfile(request);

            verify(advisorWriteService).updateAdvisor(eq(meId), any(UpdateAdvisorRequest.class));
        }
    }

    @Test
    void updateMyProfile_emptyMobileList_throwsBadRequest() {
        SelfPersonalDetailsRequest inner = SelfPersonalDetailsRequest.builder()
                .mobileNumbers(Optional.of(Collections.emptyList()))
                .build();
        SelfAdvisorProfileRequest request = SelfAdvisorProfileRequest.builder()
                .personalDetails(Optional.of(inner))
                .build();
        AdvisorResponse existing = new AdvisorResponse();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorReadService.getAdvisorByIdentifier(meId)).thenReturn(existing);

            assertThrows(BadRequestException.class, () -> advisorSelfService.updateMyProfile(request));
        }
    }

    @Test
    void getMyAddress_success() {
        AddressData data = new AddressData();
        data.setId("addr-1");
        data.setAddress("Line 1");

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorAddressReadService.getAddress(meId, "addr-1")).thenReturn(data);

            SelfAddressResponse result = advisorSelfService.getMyAddress("addr-1");

            assertEquals("addr-1", result.getId());
            assertEquals("Line 1", result.getAddress());
        }
    }

    @Test
    void updateMyAddress_success() {
        AddressData current = new AddressData();
        current.setId("a1");
        current.setAddress("Old");
        current.setPincode("560001");
        AddressData updated = new AddressData();
        updated.setId("a1");
        updated.setAddress("New");

        SelfAddressRequest req = SelfAddressRequest.builder()
                .address(Optional.of("New"))
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorAddressReadService.getAddress(meId, "a1")).thenReturn(current);
            when(advisorAddressWriteService.updateAddress(eq(meId), eq("a1"), any())).thenReturn(updated);

            SelfAddressResponse result = advisorSelfService.updateMyAddress("a1", req);

            assertEquals("New", result.getAddress());
            verify(advisorAddressWriteService).updateAddress(eq(meId), eq("a1"), any());
        }
    }

    @Test
    void getMyBankDetails_byId_success() {
        UUID bankId = UUID.randomUUID();
        BankDetailsResponse bank = BankDetailsResponse.builder()
                .bankIdentifier(bankId)
                .nameAsPerPassbook("N")
                .accountNo("1")
                .ifscCode("IFSC")
                .bankName("B")
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorBankDetailsReadService.getAllBankDetails(meId)).thenReturn(List.of(bank));

            assertEquals(bankId, advisorSelfService.getMyBankDetails(bankId).getBankIdentifier());
        }
    }

    @Test
    void sendOtp_usesAdvisorUsernameWhenFoundByMobile() {
        when(authenticationServiceFactory.getHandler(ThirdPartyServiceList.AUTHENTICATION)).thenReturn(authenticationHandler);
        AdvisorBasicResponse basic = new AdvisorBasicResponse();
        basic.setUsername("resolved_user");
        when(advisorRepositoryWrapper.findAdvisorByMobileNo("9876543210")).thenReturn(Optional.of(basic));

        advisorSelfService.sendOtp(new SelfSendOtpRequest("9876543210"));

        verify(authenticationHandler).sendOtp(argThat(r -> "resolved_user".equals(r.getUsername())), any());
    }

    @Test
    void resolveMe_usernameUnknown_throwsNotFoundForCurrentUser() {
        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            when(advisorRepositoryWrapper.findByUsername(SELF_USER)).thenReturn(Optional.empty());

            assertThrows(AdvisorOperationException.class, () -> advisorSelfService.getMyAddresses());
        }
    }

    @Test
    void getSelfAdvisorLeadsWithSearch_success_resolvesProductName() {
        advisorEntity.setReferralCode("REF001");
        PaginationRequest p = new PaginationRequest(0, 10, "createdAt", "DESC");
        AdvisorSelfLeadResponse row = AdvisorSelfLeadResponse.builder().loanType("HL").build();
        PaginatedResponse<AdvisorSelfLeadResponse> page = new PaginatedResponse<>(List.of(row), null);

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorRepositoryWrapper.findLeadsByReferralCodeWithSearch(
                    eq("REF001"), eq(p), eq("99"), eq("n"), eq("ACTIVE"), eq("SUB"), isNull(), isNull()))
                    .thenReturn(page);
            when(productReadService.getProductByCode("HL")).thenReturn(new ProductResponse(1L, "HL", "Home Loan"));

            PaginatedResponse<AdvisorSelfLeadResponse> result = advisorSelfService.getSelfAdvisorLeadsWithSearch(
                    p, "99", "n", "ACTIVE", "SUB", null, null);

            assertEquals("Home Loan", result.getContent().get(0).getLoanType());
        }
    }

    @Test
    void getSelfAdvisorLeadsWithSearch_noReferral_throws() {
        advisorEntity.setReferralCode(null);
        PaginationRequest p = new PaginationRequest(0, 10, "createdAt", "DESC");

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();

            assertThrows(BadRequestException.class,
                    () -> advisorSelfService.getSelfAdvisorLeadsWithSearch(p, null, null, null, null, null, null));
        }
    }

    @Test
    void getSelfAdvisorLeadByLeadId_success() {
        UUID leadId = UUID.randomUUID();
        LeadResponse lead = LeadResponse.builder()
                .leadIdentifier(leadId)
                .referredByCode("REF001")
                .primaryPersonName("P")
                .primaryPersonNumber("9876543210")
                .productCode("HL")
                .status(LeadStatus.ACTIVE)
                .requestedAmount(BigDecimal.ONE)
                .leadCreatedAt(LocalDateTime.now())
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(leadReadService.getLeadByIdentifier(leadId)).thenReturn(lead);
            when(productReadService.getProductByCode("HL")).thenReturn(new ProductResponse(1L, "HL", "HL Name"));
            when(leadStageHistoryReadService.getStageHistoryWithDisplayLabelsByLeadId(leadId))
                    .thenReturn(List.of(LeadStageHistoryDisplayResponse.builder()
                            .displayLabel("Stage")
                            .exitedAt(null)
                            .build()));

            AdvisorSelfLeadResponse result = advisorSelfService.getSelfAdvisorLeadByLeadId(leadId);

            assertEquals(leadId, result.getLeadIdentifier());
            assertEquals("P", result.getLeadName());
            assertEquals("HL Name", result.getLoanType());
            assertEquals("Stage", result.getLeadStageDisplayName());
        }
    }

    @Test
    void getSelfAdvisorLeadByLeadId_leadNotFound_throwsBadRequest() {
        UUID leadId = UUID.randomUUID();
        LeadNotFoundException notFound = new LeadNotFoundException(leadId, messageSource);

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(leadReadService.getLeadByIdentifier(leadId)).thenThrow(notFound);

            assertThrows(BadRequestException.class, () -> advisorSelfService.getSelfAdvisorLeadByLeadId(leadId));
        }
    }

    @Test
    void getSelfAdvisorLeadByLeadId_nullReferredBy_throwsBadRequest() {
        UUID leadId = UUID.randomUUID();
        LeadResponse lead = LeadResponse.builder()
                .leadIdentifier(leadId)
                .referredByCode(null)
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(leadReadService.getLeadByIdentifier(leadId)).thenReturn(lead);

            assertThrows(BadRequestException.class, () -> advisorSelfService.getSelfAdvisorLeadByLeadId(leadId));
        }
    }

    @Test
    void getSelfAdvisorLeads_productLookupThrows_usesProductCode() {
        advisorEntity.setReferralCode("REF001");
        PaginationRequest p = new PaginationRequest(0, 20, "createdAt", "DESC");
        LeadBasicResponse basic = LeadBasicResponse.builder()
                .leadIdentifier(UUID.randomUUID())
                .status(LeadStatus.ACTIVE)
                .productCode("BAD")
                .build();
        PaginatedResponse<LeadBasicResponse> page = new PaginatedResponse<>(List.of(basic), null);

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(leadReadService.getLeadsByReferralCode("REF001", p)).thenReturn(page);
            when(productReadService.getProductByCode("BAD")).thenThrow(new RuntimeException("missing"));
            when(leadStageHistoryReadService.getStageHistoryWithDisplayLabelsByLeadId(basic.getLeadIdentifier()))
                    .thenReturn(Collections.emptyList());

            PaginatedResponse<AdvisorSelfLeadResponse> result = advisorSelfService.getSelfAdvisorLeads(p);

            assertEquals("BAD", result.getContent().get(0).getLoanType());
        }
    }

    @Test
    void getSelfAdvisorLeads_nullProductCode_passthrough() {
        advisorEntity.setReferralCode("REF001");
        PaginationRequest p = new PaginationRequest(0, 20, "createdAt", "DESC");
        LeadBasicResponse basic = LeadBasicResponse.builder()
                .leadIdentifier(UUID.randomUUID())
                .status(LeadStatus.ACTIVE)
                .productCode(null)
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(leadReadService.getLeadsByReferralCode("REF001", p)).thenReturn(
                    new PaginatedResponse<>(List.of(basic), null));
            when(leadStageHistoryReadService.getStageHistoryWithDisplayLabelsByLeadId(basic.getLeadIdentifier()))
                    .thenReturn(Collections.emptyList());

            PaginatedResponse<AdvisorSelfLeadResponse> result = advisorSelfService.getSelfAdvisorLeads(p);

            assertNull(result.getContent().get(0).getLoanType());
        }
    }

    @Test
    void getSelfAdvisorLeadStageHistory_success_mergesSameLabel() {
        UUID leadId = UUID.randomUUID();
        LocalDateTime t1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2024, 1, 1, 11, 0);
        LocalDateTime t3 = LocalDateTime.of(2024, 1, 1, 12, 0);

        LeadResponse lead = LeadResponse.builder()
                .leadIdentifier(leadId)
                .referredByCode("REF001")
                .build();

        List<LeadStageHistoryDisplayResponse> rows = List.of(
                LeadStageHistoryDisplayResponse.builder().displayLabel("S1").enteredAt(t1).exitedAt(t2).build(),
                LeadStageHistoryDisplayResponse.builder().displayLabel("S1").enteredAt(t2).exitedAt(t3).build(),
                LeadStageHistoryDisplayResponse.builder().displayLabel("S2").enteredAt(t3).exitedAt(null).build());

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(leadReadService.getLeadByIdentifier(leadId)).thenReturn(lead);
            when(leadStageHistoryReadService.getStageHistoryWithDisplayLabelsByLeadId(leadId)).thenReturn(rows);

            List<AdvisorSelfLeadStageHistoryResponse> result = advisorSelfService.getSelfAdvisorLeadStageHistory(leadId);

            assertEquals(2, result.size());
            assertEquals("S1", result.get(0).getExternalStage());
            assertEquals(t3, result.get(0).getExitedAt());
            assertEquals("S2", result.get(1).getExternalStage());
        }
    }

    @Test
    void getSelfAdvisorLeadStageHistory_emptyRows_returnsEmpty() {
        UUID leadId = UUID.randomUUID();
        LeadResponse lead = LeadResponse.builder()
                .leadIdentifier(leadId)
                .referredByCode("REF001")
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(leadReadService.getLeadByIdentifier(leadId)).thenReturn(lead);
            when(leadStageHistoryReadService.getStageHistoryWithDisplayLabelsByLeadId(leadId))
                    .thenReturn(Collections.emptyList());

            assertTrue(advisorSelfService.getSelfAdvisorLeadStageHistory(leadId).isEmpty());
        }
    }

    @Test
    void getSelfAdvisorLeadStageHistory_leadNotFound_throws() {
        UUID leadId = UUID.randomUUID();
        LeadNotFoundException notFound = new LeadNotFoundException(leadId, messageSource);

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(leadReadService.getLeadByIdentifier(leadId)).thenThrow(notFound);

            assertThrows(BadRequestException.class, () -> advisorSelfService.getSelfAdvisorLeadStageHistory(leadId));
        }
    }

    @Test
    void updateAdvisorSelfLead_withName_updatesContact() {
        UUID leadId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();
        AdvisorSelfLeadCreateRequest request = AdvisorSelfLeadCreateRequest.builder()
                .mobileNumber("9876543210")
                .requestedAmount(BigDecimal.ONE)
                .product("P")
                .firstName("John")
                .build();

        advisorSelfService.updateAdvisorSelfLead(leadId, contactId, request);

        verify(leadContactWriteService).updateContactName(eq(leadId), eq(contactId), any());
        verify(leadWriteService, never()).updatePropertyDetails(any(), any());
    }

    @Test
    void updateAdvisorSelfLead_withLocation_updatesProperty() {
        UUID leadId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();
        AdvisorSelfLeadCreateRequest request = AdvisorSelfLeadCreateRequest.builder()
                .mobileNumber("9876543210")
                .requestedAmount(BigDecimal.ONE)
                .product("P")
                .stateCode("KA")
                .build();

        advisorSelfService.updateAdvisorSelfLead(leadId, contactId, request);

        verify(leadWriteService).updatePropertyDetails(eq(leadId), any());
        verify(leadContactWriteService, never()).updateContactName(any(), any(), any());
    }

    @Test
    void createAdvisorSelfLead_withFirstName_invokesContactNameUpdate() {
        AdvisorSelfLeadCreateRequest request = AdvisorSelfLeadCreateRequest.builder()
                .mobileNumber("9876543210")
                .requestedAmount(BigDecimal.ONE)
                .product("P1")
                .firstName("Jane")
                .build();
        UUID leadId = UUID.randomUUID();
        UUID contactId = UUID.randomUUID();
        when(leadWriteService.createLead(any())).thenReturn(
                CreateLeadResponse.builder().leadIdentifier(leadId).contactIdentifier(contactId).build());

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();

            advisorSelfService.createAdvisorSelfLead(request);

            verify(leadContactWriteService).updateContactName(eq(leadId), eq(contactId), any());
        }
    }

    @Test
    void addMyAddress_withPincode_passesToWriteService() {
        SelfAddressRequest.SelfPincodeRequest pin =
                SelfAddressRequest.SelfPincodeRequest.builder()
                        .pincode(Optional.of("560001"))
                        .build();
        SelfAddressRequest req = SelfAddressRequest.builder()
                .address(Optional.of("Street 1"))
                .pincode(Optional.of(pin))
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorAddressReadService.getAddresses(meId)).thenReturn(List.of(new AddressData()));
            when(advisorAddressWriteService.addAddress(eq(meId), argThat(ar ->
                    "560001".equals(ar.getPincode() != null ? ar.getPincode().getPincode() : null)
                            && "Street 1".equals(ar.getAddress())))).thenReturn("id");

            advisorSelfService.addMyAddress(req);
        }
    }

    @Test
    void updateMyAddress_optionalEmptyPincode_clearsPincodeOnMergedRequest() {
        AddressData current = new AddressData();
        current.setId("a1");
        current.setAddress("Same");
        current.setPincode("560001");
        AddressData updated = new AddressData();
        updated.setId("a1");
        SelfAddressRequest req = SelfAddressRequest.builder()
                .pincode(Optional.empty())
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorAddressReadService.getAddress(meId, "a1")).thenReturn(current);
            when(advisorAddressWriteService.updateAddress(eq(meId), eq("a1"), argThat(ar -> ar.getPincode() == null)))
                    .thenReturn(updated);

            advisorSelfService.updateMyAddress("a1", req);

            verify(advisorAddressWriteService).updateAddress(eq(meId), eq("a1"), any());
        }
    }

    @Test
    void updateMyAddress_omittedPincode_keepsCurrentPincode() {
        AddressData current = new AddressData();
        current.setId("a1");
        current.setAddress("A");
        current.setPincode("560001");
        AddressData updated = new AddressData();
        updated.setId("a1");
        SelfAddressRequest req = SelfAddressRequest.builder().build();
        req.setPincode(null);

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(advisorAddressReadService.getAddress(meId, "a1")).thenReturn(current);
            when(advisorAddressWriteService.updateAddress(eq(meId), eq("a1"), argThat(ar ->
                    ar.getPincode() != null && "560001".equals(ar.getPincode().getPincode()))))
                    .thenReturn(updated);

            advisorSelfService.updateMyAddress("a1", req);

            verify(advisorAddressWriteService).updateAddress(eq(meId), eq("a1"), any());
        }
    }

    @Test
    void getSelfAdvisorLeadStageHistory_wrongReferral_throws() {
        UUID leadId = UUID.randomUUID();
        LeadResponse lead = LeadResponse.builder()
                .leadIdentifier(leadId)
                .referredByCode("WRONG")
                .build();

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(leadReadService.getLeadByIdentifier(leadId)).thenReturn(lead);

            assertThrows(BadRequestException.class, () -> advisorSelfService.getSelfAdvisorLeadStageHistory(leadId));
        }
    }

    @Test
    void getSelfAdvisorLeads_stageHistoryUsesLastRowWhenAllExited() {
        advisorEntity.setReferralCode("REF001");
        PaginationRequest p = new PaginationRequest(0, 20, "createdAt", "DESC");
        UUID leadUuid = UUID.randomUUID();
        LeadBasicResponse basic = LeadBasicResponse.builder()
                .leadIdentifier(leadUuid)
                .status(LeadStatus.ACTIVE)
                .productCode(null)
                .build();

        List<LeadStageHistoryDisplayResponse> hist = List.of(
                LeadStageHistoryDisplayResponse.builder().displayLabel(" ").enteredAt(LocalDateTime.now()).exitedAt(LocalDateTime.now()).build(),
                LeadStageHistoryDisplayResponse.builder().displayLabel("Last").enteredAt(LocalDateTime.now()).exitedAt(LocalDateTime.now()).build());

        try (MockedStatic<UserContext> uc = mockStatic(UserContext.class)) {
            uc.when(UserContext::getUsername).thenReturn(SELF_USER);
            stubLoggedInAdvisor();
            when(leadReadService.getLeadsByReferralCode("REF001", p)).thenReturn(
                    new PaginatedResponse<>(List.of(basic), null));
            when(leadStageHistoryReadService.getStageHistoryWithDisplayLabelsByLeadId(leadUuid)).thenReturn(hist);

            PaginatedResponse<AdvisorSelfLeadResponse> result = advisorSelfService.getSelfAdvisorLeads(p);

            assertEquals("Last", result.getContent().get(0).getLeadStageDisplayName());
        }
    }
}
