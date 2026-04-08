package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.advisor.dto.AddBankDetailsRequest;
import com.nivasafinance.features.advisor.dto.BankDetails;
import com.nivasafinance.features.advisor.dto.UpdateBankDetailsRequest;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.enums.BankDetailsStatus;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdvisorBankDetailsWriteServiceImplTest {

    private static final String ADVISOR_USERNAME = "adv_user";

    @Mock
    private AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Mock
    private CodeMasterService codeMasterService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private UserReadService userReadService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private AdvisorBankDetailsWriteServiceImpl advisorBankDetailsWriteService;

    private UUID advisorIdentifier;
    private UUID bankIdentifier;
    private Advisor advisor;

    @BeforeEach
    void setUp() {
        advisorIdentifier = UUID.randomUUID();
        bankIdentifier = UUID.randomUUID();
        advisor = new Advisor();
        advisor.setId(1L);
        advisor.setIdentifier(advisorIdentifier);
        advisor.setUsername(ADVISOR_USERNAME);
        advisor.setBankDetails(new ArrayList<>());
    }

    private void stubPersonForEvent() {
        when(userReadService.getPersonForUser(ADVISOR_USERNAME)).thenReturn(
                PersonResponse.builder().mobileNumbers(List.of(new MobileNumberDetails("9999999999", true, false))).build());
    }

    @Test
    void addBankDetails_success_returnsBankIdentifier() {
        AddBankDetailsRequest request = new AddBankDetailsRequest();
        request.setNameAsPerPassbook("Name");
        request.setAccountNo("123");
        request.setIfscCode("IFSC0001");
        request.setIsPrimary(false);
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);
        stubPersonForEvent();

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            UUID result = advisorBankDetailsWriteService.addBankDetails(advisorIdentifier, request);

            assertNotNull(result);
            assertEquals(1, advisor.getBankDetails().size());
            assertEquals(result, advisor.getBankDetails().get(0).getBankIdentifier());
            verify(advisorRepositoryWrapper).saveWithException(advisor);
        }
    }

    @Test
    void addBankDetails_withBankName_validatesAgainstMaster() {
        AddBankDetailsRequest request = new AddBankDetailsRequest();
        request.setBankName("BANK_KEY");
        request.setNameAsPerPassbook("Name");
        request.setAccountNo("123");
        request.setIfscCode("IFSC0001");
        request.setIsPrimary(false);
        CodeValueResponse cv = new CodeValueResponse();
        cv.setKey("BANK_KEY");
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(codeMasterService.getAllCodeValuesByCodeKey(anyString(), eq(true), eq("default"))).thenReturn(List.of(cv));
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);
        stubPersonForEvent();

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            UUID result = advisorBankDetailsWriteService.addBankDetails(advisorIdentifier, request);

            assertNotNull(result);
            verify(codeMasterService).getAllCodeValuesByCodeKey(anyString(), eq(true), eq("default"));
        }
    }

    @Test
    void updateBankDetails_success() {
        BankDetails existing = BankDetails.builder()
                .bankIdentifier(bankIdentifier)
                .isPrimary(false)
                .build();
        advisor.getBankDetails().add(existing);

        UpdateBankDetailsRequest request = new UpdateBankDetailsRequest();
        request.setNameAsPerPassbook("New Name");
        request.setAccountNo("456");
        request.setIfscCode("IFSC0002");
        request.setBankName("BANK");
        CodeValueResponse bankCv = new CodeValueResponse();
        bankCv.setKey("BANK");

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(codeMasterService.getAllCodeValuesByCodeKey(anyString(), eq(true), eq("default"))).thenReturn(List.of(bankCv));
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);
        stubPersonForEvent();

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            advisorBankDetailsWriteService.updateBankDetails(advisorIdentifier, bankIdentifier, request);

            assertEquals("New Name", existing.getNameAsPerPassbook());
            assertEquals("456", existing.getAccountNo());
            verify(advisorRepositoryWrapper).saveWithException(advisor);
        }
    }

    @Test
    void activateBankDetails_success_returnsBankIdentifier() {
        BankDetails existing = BankDetails.builder()
                .bankIdentifier(bankIdentifier)
                .status(BankDetailsStatus.DEACTIVATED)
                .build();
        advisor.getBankDetails().add(existing);

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);
        stubPersonForEvent();

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            UUID result = advisorBankDetailsWriteService.activateBankDetails(advisorIdentifier, bankIdentifier);

            assertEquals(bankIdentifier, result);
            assertEquals(BankDetailsStatus.ACTIVE, existing.getStatus());
        }
    }

    @Test
    void deactivateBankDetails_success_returnsBankIdentifier() {
        BankDetails existing = BankDetails.builder()
                .bankIdentifier(bankIdentifier)
                .status(BankDetailsStatus.ACTIVE)
                .build();
        advisor.getBankDetails().add(existing);

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);
        stubPersonForEvent();

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");

            UUID result = advisorBankDetailsWriteService.deactivateBankDetails(advisorIdentifier, bankIdentifier);

            assertEquals(bankIdentifier, result);
            assertEquals(BankDetailsStatus.DEACTIVATED, existing.getStatus());
        }
    }

    @Test
    void getBankDetail_notFound_throwsException() {
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);

        UpdateBankDetailsRequest request = new UpdateBankDetailsRequest();
        request.setNameAsPerPassbook("N");
        request.setAccountNo("1");
        request.setIfscCode("IFSC");
        request.setBankName("B");

        assertThrows(IllegalStateException.class,
                () -> advisorBankDetailsWriteService.updateBankDetails(advisorIdentifier, bankIdentifier, request));
    }

    @Test
    void addBankDetails_invalidBankName_throwsException() {
        AddBankDetailsRequest request = new AddBankDetailsRequest();
        request.setBankName("INVALID_BANK");
        request.setNameAsPerPassbook("Name");
        request.setAccountNo("123");
        request.setIfscCode("IFSC");
        request.setIsPrimary(false);
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(codeMasterService.getAllCodeValuesByCodeKey(anyString(), eq(true), eq("default"))).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> advisorBankDetailsWriteService.addBankDetails(advisorIdentifier, request));
        verify(advisorRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void addBankDetails_whenBankDetailsNull_initializesList() {
        advisor.setBankDetails(null);
        AddBankDetailsRequest request = new AddBankDetailsRequest();
        request.setNameAsPerPassbook("Name");
        request.setAccountNo("123");
        request.setIfscCode("IFSC0001");
        request.setIsPrimary(false);
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);
        stubPersonForEvent();

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            advisorBankDetailsWriteService.addBankDetails(advisorIdentifier, request);
        }

        assertNotNull(advisor.getBankDetails());
        assertEquals(1, advisor.getBankDetails().size());
    }

    @Test
    void addBankDetails_primaryTrue_demotesExistingPrimary() {
        BankDetails existing = BankDetails.builder()
                .bankIdentifier(UUID.randomUUID())
                .isPrimary(true)
                .nameAsPerPassbook("Old")
                .accountNo("1")
                .ifscCode("IFSC")
                .build();
        advisor.getBankDetails().add(existing);

        AddBankDetailsRequest request = new AddBankDetailsRequest();
        request.setNameAsPerPassbook("Name");
        request.setAccountNo("123");
        request.setIfscCode("IFSC0001");
        request.setIsPrimary(true);
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);
        stubPersonForEvent();

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            advisorBankDetailsWriteService.addBankDetails(advisorIdentifier, request);
        }

        assertFalse(existing.getIsPrimary());
        assertTrue(advisor.getBankDetails().stream().filter(BankDetails::getIsPrimary).count() >= 1);
    }

    @Test
    void updateBankDetails_blankRequiredField_throwsBadRequest() {
        BankDetails existing = BankDetails.builder()
                .bankIdentifier(bankIdentifier)
                .isPrimary(false)
                .build();
        advisor.getBankDetails().add(existing);
        UpdateBankDetailsRequest request = new UpdateBankDetailsRequest();
        request.setNameAsPerPassbook("");
        request.setAccountNo("456");
        request.setIfscCode("IFSC");
        request.setBankName("BANK");
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(messageSource.getMessage(any(), any(), any())).thenReturn("error");

        assertThrows(BadRequestException.class,
                () -> advisorBankDetailsWriteService.updateBankDetails(advisorIdentifier, bankIdentifier, request));
        verify(advisorRepositoryWrapper, never()).saveWithException(any());
    }

    @Test
    void updateBankDetails_setPrimaryTrue_demotesOtherAccounts() {
        UUID otherId = UUID.randomUUID();
        BankDetails other = BankDetails.builder()
                .bankIdentifier(otherId)
                .isPrimary(true)
                .nameAsPerPassbook("O")
                .accountNo("1")
                .ifscCode("I1")
                .bankName("B1")
                .build();
        BankDetails target = BankDetails.builder()
                .bankIdentifier(bankIdentifier)
                .isPrimary(false)
                .nameAsPerPassbook("T")
                .accountNo("2")
                .ifscCode("I2")
                .bankName("B2")
                .build();
        advisor.getBankDetails().add(other);
        advisor.getBankDetails().add(target);

        UpdateBankDetailsRequest request = new UpdateBankDetailsRequest();
        request.setNameAsPerPassbook("New");
        request.setAccountNo("456");
        request.setIfscCode("IFSC0002");
        request.setBankName("BANK");
        request.setIsPrimary(true);
        CodeValueResponse bankCv = new CodeValueResponse();
        bankCv.setKey("BANK");

        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);
        when(codeMasterService.getAllCodeValuesByCodeKey(anyString(), eq(true), eq("default"))).thenReturn(List.of(bankCv));
        when(advisorRepositoryWrapper.saveWithException(any(Advisor.class))).thenReturn(advisor);
        stubPersonForEvent();

        try (MockedStatic<UserContext> userContext = mockStatic(UserContext.class)) {
            userContext.when(UserContext::getUsername).thenReturn("user1");
            advisorBankDetailsWriteService.updateBankDetails(advisorIdentifier, bankIdentifier, request);
        }

        assertFalse(other.getIsPrimary());
        assertTrue(Boolean.TRUE.equals(target.getIsPrimary()));
    }

    @Test
    void activateBankDetails_whenNoBankDetailsList_throws() {
        advisor.setBankDetails(null);
        when(advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier)).thenReturn(advisor);

        assertThrows(IllegalStateException.class,
                () -> advisorBankDetailsWriteService.activateBankDetails(advisorIdentifier, bankIdentifier));
    }
}
