package com.nivasafinance.features.lead.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.features.consent.dto.ConsentResponse;
import com.nivasafinance.features.consent.service.ConsentReadService;
import com.nivasafinance.features.creditbureau.dto.*;
import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import com.nivasafinance.features.creditbureau.service.CreditBureauReadService;
import com.nivasafinance.features.lead.dto.EnquiryConsentStatusResponse;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadCreditBureauReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeadCreditBureauReadServiceImpl implements LeadCreditBureauReadService {

    private final CreditBureauReadService creditBureauReadService;
    private final ConsentReadService consentReadService;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final ContactRepositoryWrapper contactRepositoryWrapper;

    @Override
    public List<CustomerEnquiryResponse> getCustomerEnquiryByEnquiryIdentifier(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        validateContactBelongsToLead(lead, contact.getId());

        CreditBureauEnquiry enquiry = creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier);
        if  (CollectionUtils.isEmpty(contact.getCbEnquiryId()) || !contact.getCbEnquiryId().contains(enquiry.getId())) {
            throw new BadRequestException("Enquiry does not belong to the provided contact");
        }
        return creditBureauReadService.getCustomerEnquiryByEnquiryIdentifier(enquiryIdentifier);
    }

    @Override
    public Optional<SummaryResponse> getSummaryResponseByEnquiryIdentifier(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        validateContactBelongsToLead(lead, contact.getId());

        CreditBureauEnquiry enquiry = creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier);
        if  (CollectionUtils.isEmpty(contact.getCbEnquiryId()) || !contact.getCbEnquiryId().contains(enquiry.getId())) {
            throw new BadRequestException("Enquiry does not belong to the provided contact");
        }
        return creditBureauReadService.getSummaryByEnquiryIdentifier(enquiryIdentifier);
    }

    @Override
    public Optional<EnquiryStatusResponse> getEnquiryStatusByEnquiryIdentifier(UUID leadIdentifer, UUID contactIdentifier, UUID enquiryIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifer);
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        validateContactBelongsToLead(lead, contact.getId());

        CreditBureauEnquiry enquiry = creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier);
        if  (CollectionUtils.isEmpty(contact.getCbEnquiryId()) || !contact.getCbEnquiryId().contains(enquiry.getId())) {
            throw new BadRequestException("Enquiry does not belong to the provided contact");
        }

        return creditBureauReadService.getEnquiryStatusByEnquiryIdentifier(enquiryIdentifier);
    }

    @Override
    public Optional<EnquiryConsentStatusResponse> getConsentStatusByEnquiryIdentifier(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        validateContactBelongsToLead(lead, contact.getId());

        CreditBureauEnquiry enquiry = creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier);
        if (CollectionUtils.isEmpty(contact.getCbEnquiryId()) || !contact.getCbEnquiryId().contains(enquiry.getId())) {
            throw new BadRequestException("Enquiry does not belong to the provided contact");
        }

        EnquiryConsentStatusResponse.EnquiryConsentStatusResponseBuilder responseBuilder =
                EnquiryConsentStatusResponse.builder().enquiryIdentifier(enquiryIdentifier);

        if (enquiry.getConsentId() == null) {
            return Optional.of(responseBuilder.build());
        }

        return consentReadService.findById(enquiry.getConsentId())
                .map(consent -> mapToEnquiryConsentStatusResponse(responseBuilder, consent))
                .or(() -> Optional.of(responseBuilder.build()));
    }

    private EnquiryConsentStatusResponse mapToEnquiryConsentStatusResponse(
            EnquiryConsentStatusResponse.EnquiryConsentStatusResponseBuilder builder,
            ConsentResponse consent) {
        return builder
                .consentIdentifier(consent.getIdentifier())
                .consentStatus(consent.getStatus())
                .consentSentTime(consent.getConsentSentTime())
                .consentReceivedTime(consent.getConsentReceivedTime())
                .consentWithdrawalRequestedTime(consent.getConsentWithdrawlRequestedTime())
                .build();
    }

    @Override
    public List<TrendsResponse> getScoreTrendsByEnquiryIdentifier(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        validateContactBelongsToLead(lead, contact.getId());

        CreditBureauEnquiry enquiry = creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier);
        if (CollectionUtils.isEmpty(contact.getCbEnquiryId()) || !contact.getCbEnquiryId().contains(enquiry.getId())) {
            throw new BadRequestException("Enquiry does not belong to the provided contact");
        }
        return creditBureauReadService.getTrendsByEnquiryIdentifier(enquiryIdentifier);
    }

    @Override
    public List<DemographicVariationResponse> getDemographicVariationsByEnquiryIdentifier(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        validateContactBelongsToLead(lead, contact.getId());

        CreditBureauEnquiry enquiry = creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier);
        if (CollectionUtils.isEmpty(contact.getCbEnquiryId()) || !contact.getCbEnquiryId().contains(enquiry.getId())) {
            throw new BadRequestException("Enquiry does not belong to the provided contact");
        }
        return creditBureauReadService.getDemographicVariationsByEnquiryIdentifier(enquiryIdentifier);
    }

    @Override
    public Long getEnquiryIdForCbReportRegenerate(UUID leadIdentifier, UUID contactIdentifier, UUID enquiryIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        validateContactBelongsToLead(lead, contact.getId());

        CreditBureauEnquiry enquiry = creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier);
        if (CollectionUtils.isEmpty(contact.getCbEnquiryId()) || !contact.getCbEnquiryId().contains(enquiry.getId())) {
            throw new BadRequestException("Enquiry does not belong to the provided contact");
        }
        return enquiry.getId();
    }

    private void validateContactBelongsToLead(Lead lead, Long contactId) {
        List<Long> contacts = lead.getContacts();
        if (CollectionUtils.isEmpty(contacts) || !contacts.contains(contactId)) {
            throw new BadRequestException("Contact does not belong to the provided lead identifier");
        }
    }
}
