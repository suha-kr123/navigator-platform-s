package com.nivasafinance.features.consent.service.impl;

import com.nivasafinance.common.context.RequestContext;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.ConsentReceivedEventPayload;
import com.nivasafinance.common.events.payload.ConsentSentEventPayload;
import com.nivasafinance.features.consent.dto.AcceptConsentRequest;
import com.nivasafinance.features.consent.dto.ConsentReceivedRequest;
import com.nivasafinance.features.consent.dto.CreateAndSendConsent;
import com.nivasafinance.features.consent.dto.ResendConsentRequest;
import com.nivasafinance.features.consent.dto.WithdrawConsentRequest;
import com.nivasafinance.features.consent.entity.Consent;
import com.nivasafinance.features.consent.enums.ConsentStatus;
import com.nivasafinance.features.consent.exception.ConsentExceptionFactory;
import com.nivasafinance.features.consent.repository.ConsentRepositoryWrapper;
import com.nivasafinance.features.consent.service.ConsentWriteService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ConsentWriteServiceImpl implements ConsentWriteService {

    private final ConsentRepositoryWrapper consentRepositoryWrapper;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageSource messageSource;

    @Value("${consent.url-base:}")
    private String consentUrlBase;

    public ConsentWriteServiceImpl(ConsentRepositoryWrapper consentRepositoryWrapper,
                                   ApplicationEventPublisher eventPublisher,
                                   MessageSource messageSource) {
        this.consentRepositoryWrapper = consentRepositoryWrapper;
        this.eventPublisher = eventPublisher;
        this.messageSource = messageSource;
    }

    @Override
    public Consent createAndSendConsent(CreateAndSendConsent command) {
        Consent consent = new Consent();
        consent.setStatus(ConsentStatus.SENT);
        consent.setConsentSentDetails(new Consent.ConsentSentDetails(LocalDateTime.now()));
        consent.setConsentReceivedDetails(null);
        consent.setConsentWithdrawnDetails(null);

        Consent saved = consentRepositoryWrapper.saveWithException(consent);

        String consentLink = buildConsentLink(consentUrlBase, saved.getIdentifier().toString(),
                command.getEnquiryIdentifier().toString(),
                command.getLeadIdentifier() != null ? command.getLeadIdentifier().toString() : null,
                command.getContactIdentifier() != null ? command.getContactIdentifier().toString() : null);

        ConsentSentEventPayload payload = ConsentSentEventPayload.builder()
                .personId(command.getPersonId())
                .consentId(saved.getId())
                .consentIdentifier(saved.getIdentifier())
                .enquiryIdentifier(command.getEnquiryIdentifier())
                .consentLink(consentLink)
                .recipientContact(command.getRecipientPhone())
                .build();

        eventPublisher.publishEvent(new SystemEvent<>(
                BusinessEvent.CB_CONSENT_SENT.toString(),
                payload,
                UserContext.getUsername()));

        return saved;
    }

    @Override
    public Consent createConsentReceived(ConsentReceivedRequest request) {
        Consent consent = new Consent();
        consent.setStatus(ConsentStatus.RECEIVED);
        consent.setConsentSentDetails(null);
        String auditId = RequestContext.getRequestMetadata() != null ? RequestContext.getRequestMetadata().getAuditId() : null;
        consent.setConsentReceivedDetails(new Consent.ConsentReceivedDetails(LocalDateTime.now(), auditId));
        consent.setConsentWithdrawnDetails(null);
        return consentRepositoryWrapper.saveWithException(consent);
    }

    @Override
    public void acceptConsent(AcceptConsentRequest command) {
        Consent consent = consentRepositoryWrapper.findByIdentifierWithException(command.getConsentIdentifier());
        if (consent.getStatus() != ConsentStatus.SENT) {
            throw ConsentExceptionFactory.invalidStatusForAccept(consent.getStatus(), messageSource);
        }
        String auditId = RequestContext.getRequestMetadata() != null ? RequestContext.getRequestMetadata().getAuditId() : null;
        consent.setConsentReceivedDetails(new Consent.ConsentReceivedDetails(LocalDateTime.now(), auditId));
        consent.setStatus(ConsentStatus.RECEIVED);
        consentRepositoryWrapper.saveWithException(consent);

        // Build and send withdrawal link if recipient info is provided
        if (command.getRecipientPhone() != null && 
            command.getLeadIdentifier() != null && 
            command.getContactIdentifier() != null &&
            command.getPersonId() != null) {
            
            String withdrawalLink = buildWithdrawalLink(consentUrlBase,
                    command.getConsentIdentifier().toString(),
                    command.getEnquiryIdentifier().toString(),
                    command.getLeadIdentifier().toString(),
                    command.getContactIdentifier().toString());

            ConsentReceivedEventPayload payload = ConsentReceivedEventPayload.builder()
                    .personId(command.getPersonId())
                    .consentId(consent.getId())
                    .consentIdentifier(consent.getIdentifier())
                    .enquiryIdentifier(command.getEnquiryIdentifier())
                    .withdrawalLink(withdrawalLink)
                    .recipientContact(command.getRecipientPhone())
                    .build();

            eventPublisher.publishEvent(new SystemEvent<>(
                    BusinessEvent.CB_CONSENT_RECEIVED.toString(),
                    payload,
                    UserContext.getUsername()));
        }
    }

    @Override
    public void resendConsent(ResendConsentRequest request) {
        Consent consent = consentRepositoryWrapper.findByIdentifierWithException(request.getConsentIdentifier());
        if (consent.getStatus() != ConsentStatus.SENT) {
            throw ConsentExceptionFactory.invalidStatusForResend(consent.getStatus(), messageSource);
        }
        consent.setConsentSentDetails(new Consent.ConsentSentDetails(LocalDateTime.now()));
        consentRepositoryWrapper.saveWithException(consent);

        String consentLink = buildConsentLink(consentUrlBase, consent.getIdentifier().toString(),
                request.getEnquiryIdentifier().toString(),
                request.getLeadIdentifier() != null ? request.getLeadIdentifier().toString() : null,
                request.getContactIdentifier() != null ? request.getContactIdentifier().toString() : null);

        ConsentSentEventPayload payload = ConsentSentEventPayload.builder()
                .personId(request.getPersonId())
                .consentId(consent.getId())
                .consentIdentifier(consent.getIdentifier())
                .enquiryIdentifier(request.getEnquiryIdentifier())
                .consentLink(consentLink)
                .recipientContact(request.getRecipientPhone())
                .build();

        eventPublisher.publishEvent(new SystemEvent<>(
                BusinessEvent.CB_CONSENT_SENT.toString(),
                payload,
                UserContext.getUsername()));
    }

    @Override
    public void withdrawConsent(WithdrawConsentRequest command) {
        Consent consent = consentRepositoryWrapper.findByIdentifierWithException(command.getConsentIdentifier());
        if (consent.getStatus() != ConsentStatus.RECEIVED) {
            throw ConsentExceptionFactory.invalidStatusForWithdrawn(consent.getStatus(), messageSource);
        }
        String auditId = RequestContext.getRequestMetadata() != null ? RequestContext.getRequestMetadata().getAuditId() : null;
        consent.setConsentWithdrawnDetails(new Consent.ConsentWithdrawnDetails(LocalDateTime.now(), auditId));
        consent.setStatus(ConsentStatus.REQUEST_FOR_WITHDRAWAL);
        consentRepositoryWrapper.saveWithException(consent);
    }

    private static String buildConsentLink(String base, String consentIdentifier, String enquiryIdentifier,
                                           String leadIdentifier, String contactIdentifier) {
        String params = "?c=" + consentIdentifier + "&r=" + enquiryIdentifier + "&lead=" + leadIdentifier + "&contact=" + contactIdentifier;
        return (base != null ? base : "") + params;
    }

    private static String buildWithdrawalLink(String base, String consentIdentifier, String enquiryIdentifier,
                                             String leadIdentifier, String contactIdentifier) {
        String params = "?c=" + consentIdentifier + "&r=" + enquiryIdentifier + "&lead=" + leadIdentifier + "&contact=" + contactIdentifier;
        return (base != null ? base : "") + params;
    }
}
