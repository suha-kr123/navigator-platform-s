package com.nivasafinance.features.leadotp.service.impl;

import com.nivasafinance.common.utils.PhoneNumberUtils;
import com.nivasafinance.features.lead.dto.CreateLeadRequest;
import com.nivasafinance.features.lead.dto.CreateLeadResponse;
import com.nivasafinance.features.lead.dto.LeadBasicResponse;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.exception.ActiveLeadAlreadyExistsException;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.leadotp.dto.CreateLeadSendOtpRequest;
import com.nivasafinance.features.leadotp.dto.CreateLeadSendOtpResponse;
import com.nivasafinance.features.leadotp.dto.CreateLeadVerifyOtpRequest;
import com.nivasafinance.features.leadotp.dto.CreateLeadVerifyOtpResponse;
import com.nivasafinance.features.leadotp.dto.LeadContactSendOtpResponse;
import com.nivasafinance.features.leadotp.dto.LeadContactVerifyOtpRequest;
import com.nivasafinance.features.leadotp.dto.LeadContactVerifyOtpResponse;
import com.nivasafinance.features.leadotp.entity.LeadOneTimeToken;
import com.nivasafinance.features.leadotp.exception.LeadOtpExceptionFactory;
import com.nivasafinance.features.leadotp.repository.LeadOneTimeTokenRepositoryWrapper;
import com.nivasafinance.features.leadotp.service.LeadOtpService;
import com.nivasafinance.features.otp.core.dto.OtpSendCommand;
import com.nivasafinance.features.otp.core.dto.OtpSendResult;
import com.nivasafinance.features.otp.core.dto.OtpVerifyCommand;
import com.nivasafinance.features.otp.core.dto.OtpVerifyResult;
import com.nivasafinance.features.otp.core.entity.OneTimeToken;
import com.nivasafinance.features.otp.core.enums.OtpStatus;
import com.nivasafinance.features.otp.core.exception.OtpExceptionFactory;
import com.nivasafinance.features.otp.core.repository.OneTimeTokenRepositoryWrapper;
import com.nivasafinance.features.otp.core.service.OtpCoreService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadOtpServiceImpl implements LeadOtpService {

    private static final String VERIFY_LEAD_FOR_CB_REFERENCE = "VERIFY_LEAD_FOR_CB";
    private static final String CREATE_LEAD_REFERENCE = "CREATE_LEAD";

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final ContactRepositoryWrapper contactRepositoryWrapper;
    private final PersonReadService personReadService;
    private final OtpCoreService otpCoreService;
    private final LeadOneTimeTokenRepositoryWrapper leadOneTimeTokenRepositoryWrapper;
    private final LeadReadService leadReadService;
    private final LeadWriteService leadWriteService;
    private final OneTimeTokenRepositoryWrapper oneTimeTokenRepositoryWrapper;

    @Override
    @Transactional
    public LeadContactSendOtpResponse sendVerifyLeadForCbOtp(UUID leadIdentifier, UUID contactIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Contact contact = resolveContact(lead, contactIdentifier);
        String mobileNumber = resolvePrimaryMobileNumber(contact);

        OtpSendResult result = otpCoreService.sendOtp(OtpSendCommand.builder()
                .reference(VERIFY_LEAD_FOR_CB_REFERENCE)
                .recipient(mobileNumber)
                .build());

        leadOneTimeTokenRepositoryWrapper.invalidateActiveTokens(VERIFY_LEAD_FOR_CB_REFERENCE, lead.getId(), contact.getId());
        leadOneTimeTokenRepositoryWrapper.saveWithException(LeadOneTimeToken.builder()
                .leadId(lead.getId())
                .contactId(contact.getId())
                .ottId(result.getOneTimeTokenId())
                .reference(VERIFY_LEAD_FOR_CB_REFERENCE)
                .status(OtpStatus.SENT)
                .build());

        return LeadContactSendOtpResponse.builder()
                .oneTimeTokenId(result.getOneTimeTokenId())
                .reference(result.getReference())
                .validityInMins(result.getValidityInMins())
                .build();
    }

    @Override
    @Transactional
    public LeadContactVerifyOtpResponse verifyLeadForCbOtp(
            UUID leadIdentifier,
            UUID contactIdentifier,
            LeadContactVerifyOtpRequest request) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Contact contact = resolveContact(lead, contactIdentifier);
        LeadOneTimeToken trackedToken = leadOneTimeTokenRepositoryWrapper.findByLeadContactAndTokenId(
                        VERIFY_LEAD_FOR_CB_REFERENCE,
                        lead.getId(),
                        contact.getId(),
                        request.getOneTimeTokenId())
                .orElseThrow(LeadOtpExceptionFactory::activeTokenNotFound);

        OtpVerifyResult result = otpCoreService.verifyOtp(OtpVerifyCommand.builder()
                .reference(VERIFY_LEAD_FOR_CB_REFERENCE)
                .oneTimeTokenId(request.getOneTimeTokenId())
                .otp(request.getOtp())
                .build());

        leadOneTimeTokenRepositoryWrapper.updateStatus(trackedToken.getId(), OtpStatus.VERIFIED);
        return LeadContactVerifyOtpResponse.builder()
                .verified(result.isVerified())
                .build();
    }

    @Override
    @Transactional
    public CreateLeadSendOtpResponse sendCreateLeadOtp(CreateLeadSendOtpRequest request) {
        String mobileNumber = normalizeCreateLeadMobileNumber(request.getMobileNumber());

        OtpSendResult result = otpCoreService.sendOtp(OtpSendCommand.builder()
                .reference(CREATE_LEAD_REFERENCE)
                .recipient(mobileNumber)
                .build());

        leadOneTimeTokenRepositoryWrapper.saveWithException(LeadOneTimeToken.builder()
                .ottId(result.getOneTimeTokenId())
                .reference(CREATE_LEAD_REFERENCE)
                .status(OtpStatus.SENT)
                .build());

        return CreateLeadSendOtpResponse.builder()
                .oneTimeTokenId(result.getOneTimeTokenId())
                .reference(result.getReference())
                .validityInMins(result.getValidityInMins())
                .build();
    }

    @Override
    @Transactional
    public CreateLeadVerifyOtpResponse verifyCreateLeadOtp(CreateLeadVerifyOtpRequest request) {
        LeadOneTimeToken trackedToken = leadOneTimeTokenRepositoryWrapper.findByReferenceAndTokenId(
                        CREATE_LEAD_REFERENCE,
                        request.getOneTimeTokenId())
                .orElseThrow(LeadOtpExceptionFactory::activeTokenNotFound);
        OneTimeToken token = oneTimeTokenRepositoryWrapper.findById(request.getOneTimeTokenId())
                .orElseThrow(OtpExceptionFactory::tokenNotFound);

        OtpVerifyResult result = otpCoreService.verifyOtp(OtpVerifyCommand.builder()
                .reference(CREATE_LEAD_REFERENCE)
                .oneTimeTokenId(request.getOneTimeTokenId())
                .otp(request.getOtp())
                .build());

        Lead resolvedLead = resolveOrCreateLeadForMobile(token.getRelatesTo(), trackedToken.getLeadId());
        leadOneTimeTokenRepositoryWrapper.updateLeadId(trackedToken.getId(), resolvedLead.getId());
        leadOneTimeTokenRepositoryWrapper.updateStatus(trackedToken.getId(), OtpStatus.VERIFIED);

        return CreateLeadVerifyOtpResponse.builder()
                .verified(result.isVerified())
                .leadIdentifier(resolvedLead.getLeadIdentifier())
                .build();
    }

    private Lead resolveOrCreateLeadForMobile(String mobileNumber, Long existingLeadId) {
        if (existingLeadId != null) {
            return leadRepositoryWrapper.findByIdWithException(existingLeadId);
        }

        Optional<LeadBasicResponse> existingLead = leadReadService.findLeadByPhoneNumber(mobileNumber);
        if (existingLead.isPresent()) {
            return leadRepositoryWrapper.findByIdWithException(existingLead.get().getId());
        }

        try {
            CreateLeadRequest createLeadRequest = new CreateLeadRequest();
            createLeadRequest.setPhoneNumber(new CreateLeadRequest.MobileNumberDetails(mobileNumber, false));
            CreateLeadResponse createdLead = leadWriteService.createLead(createLeadRequest);
            return leadRepositoryWrapper.findByLeadIdentifierWithException(createdLead.getLeadIdentifier());
        } catch (ActiveLeadAlreadyExistsException ex) {
            return leadReadService.findLeadByPhoneNumber(mobileNumber)
                    .map(LeadBasicResponse::getId)
                    .map(leadRepositoryWrapper::findByIdWithException)
                    .orElseThrow(() -> ex);
        }
    }

    private Contact resolveContact(Lead lead, UUID contactIdentifier) {
        if (lead.getContacts() == null || lead.getContacts().isEmpty()) {
            throw LeadOtpExceptionFactory.invalidContactForLead();
        }

        for (Long contactId : lead.getContacts()) {
            Contact contact = contactRepositoryWrapper.findByIdWithException(contactId);
            if (contact.getIdentifier().equals(contactIdentifier)) {
                return contact;
            }
        }

        throw LeadOtpExceptionFactory.invalidContactForLead();
    }

    private String resolvePrimaryMobileNumber(Contact contact) {
        PersonResponse personResponse = personReadService.getPersonById(contact.getPersonId());
        List<MobileNumberDetails> mobileNumbers = personResponse.getMobileNumbers();
        if (mobileNumbers == null || mobileNumbers.isEmpty()) {
            throw LeadOtpExceptionFactory.missingPrimaryMobile();
        }

        return mobileNumbers.stream()
                .filter(mobile -> mobile.getIsPrimary() != null && mobile.getIsPrimary())
                .map(MobileNumberDetails::getNumber)
                .map(PhoneNumberUtils::normalizePhoneNumber)
                .findFirst()
                .orElseThrow(LeadOtpExceptionFactory::missingPrimaryMobile);
    }

    private String normalizeCreateLeadMobileNumber(String mobileNumber) {
        String normalized = PhoneNumberUtils.normalizePhoneNumber(mobileNumber);
        if (normalized == null || normalized.length() != 10) {
            throw LeadOtpExceptionFactory.invalidCreateLeadMobile();
        }
        return normalized;
    }
}
