package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.AdvisorUpdateEventPayload;
import com.nivasafinance.features.advisor.dto.AddBankDetailsRequest;
import com.nivasafinance.features.advisor.dto.BankDetails;
import com.nivasafinance.features.advisor.dto.UpdateBankDetailsRequest;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.enums.BankDetailsStatus;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorBankDetailsWriteService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AdvisorBankDetailsWriteServiceImpl implements AdvisorBankDetailsWriteService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final CodeMasterService codeMasterService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PersonRepositoryWrapper personRepositoryWrapper;

    @Override
    public UUID addBankDetails(UUID advisorIdentifier, AddBankDetailsRequest request) {
        Advisor advisor = getAdvisor(advisorIdentifier);

        // Validate bank name against BANK_NAME_MASTER if provided
        if (request.getBankName() != null) {
            validateBankName(request.getBankName());
        }

        BankDetails newBankDetail = BankDetails.builder()
                .bankIdentifier(UUID.randomUUID())
                .isPrimary(request.getIsPrimary())
                .nameAsPerPassbook(request.getNameAsPerPassbook())
                .accountNo(request.getAccountNo())
                .bankName(request.getBankName())
                .ifscCode(request.getIfscCode())
                .upid(request.getUpid())
                .status(BankDetailsStatus.ACTIVE)
                .build();

        if (advisor.getBankDetails() == null) {
            advisor.setBankDetails(new ArrayList<>());
        }

        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            advisor.getBankDetails().forEach(bankDetail -> bankDetail.setIsPrimary(false));
        }

        advisor.getBankDetails().add(newBankDetail);
        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish ADVISOR_UPDATED event
        publishAdvisorUpdatedEvent(advisor);

        return newBankDetail.getBankIdentifier();
    }

    @Override
    public void updateBankDetails(UUID advisorIdentifier, UUID bankIdentifier, UpdateBankDetailsRequest request) {
        Advisor advisor = getAdvisor(advisorIdentifier);
        BankDetails bankDetail = getBankDetail(advisor, bankIdentifier);

        if (request.getBankName() != null) {
            validateBankName(request.getBankName());
        }

        Boolean requestedPrimary = request.getIsPrimary();
        if (Boolean.TRUE.equals(requestedPrimary)) {
            advisor.getBankDetails().forEach(existingDetail -> {
                if (!existingDetail.getBankIdentifier().equals(bankIdentifier)) {
                    existingDetail.setIsPrimary(false);
                }
            });
        }

        bankDetail.setIsPrimary(requestedPrimary);
        bankDetail.setNameAsPerPassbook(request.getNameAsPerPassbook());
        bankDetail.setAccountNo(request.getAccountNo());
        bankDetail.setIfscCode(request.getIfscCode());
        bankDetail.setBankName(request.getBankName());
        bankDetail.setUpid(request.getUpid());

        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish ADVISOR_UPDATED event
        publishAdvisorUpdatedEvent(advisor);
    }

    private void validateBankName(String bankNameKey) {
        List<CodeValueResponse> banks =
                codeMasterService.getAllCodeValuesByCodeKey(SystemControlledMasterCodes.BANK_NAME_MASTER, true);
        boolean isValid = banks.stream().anyMatch(cv -> bankNameKey.equals(cv.getKey()));
        if (!isValid) {
            throw new IllegalArgumentException("Invalid bankName. Provide a valid master key from BANK_NAME_MASTER.");
        }
    }

    @Override
    public UUID activateBankDetails(UUID advisorIdentifier, UUID bankIdentifier) {
        updateStatus(advisorIdentifier, bankIdentifier, BankDetailsStatus.ACTIVE);
        return bankIdentifier;
    }

    @Override
    public UUID deactivateBankDetails(UUID advisorIdentifier, UUID bankIdentifier) {
        updateStatus(advisorIdentifier, bankIdentifier, BankDetailsStatus.DEACTIVATED);
        return bankIdentifier;
    }

    private void updateStatus(UUID advisorIdentifier,
                                             UUID bankIdentifier,
                                             BankDetailsStatus statusToSet) {
        Advisor advisor = getAdvisor(advisorIdentifier);
        BankDetails bankDetail = getBankDetail(advisor, bankIdentifier);

        bankDetail.setStatus(statusToSet);
        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish ADVISOR_UPDATED event
        publishAdvisorUpdatedEvent(advisor);
    }

    private Advisor getAdvisor(UUID advisorIdentifier) {
        return advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);
    }

    private BankDetails getBankDetail(Advisor advisor, UUID bankIdentifier) {
        if (advisor.getBankDetails() == null) {
            throw new IllegalStateException("No bank details found for advisor: " + advisor.getIdentifier());
        }

        return advisor.getBankDetails().stream()
                .filter(bankDetails -> bankDetails.getBankIdentifier().equals(bankIdentifier))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Bank detail not found with identifier: " + bankIdentifier));
    }

    private void publishAdvisorUpdatedEvent(Advisor advisor) {
        // Get primary mobile number from person entity
        com.nivasafinance.features.person.entity.Person person = 
                personRepositoryWrapper.findByIdWithException(advisor.getPersonId());
        String mobileNumber = null;
        if (person.getMobileNumbers() != null && !person.getMobileNumbers().isEmpty()) {
            mobileNumber = person.getMobileNumbers().stream()
                    .filter(m -> m.getIsPrimary() != null && m.getIsPrimary())
                    .map(com.nivasafinance.features.person.entity.MobileNumberDetails::getNumber)
                    .findFirst()
                    .orElse(null);
        }

        AdvisorUpdateEventPayload payload = AdvisorUpdateEventPayload.builder()
                .id(advisor.getId())
                .advisorIdentifier(advisor.getIdentifier())
                .mobileNumber(mobileNumber)
                .build();

        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.ADVISOR_UPDATED.toString(), payload)
        );
    }
}


