package com.nivasafinance.externals.whatsapp.service.impl;

import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppLeadUpdateRequest;
import com.nivasafinance.externals.whatsapp.service.WhatsAppLeadService;
import com.nivasafinance.externals.whatsapp.service.WhatsAppLeadUpdateService;
import com.nivasafinance.features.lead.dto.DropoffLeadRequest;
import com.nivasafinance.features.lead.dto.OnholdLeadRequest;
import com.nivasafinance.features.lead.dto.RejectLeadRequest;
import com.nivasafinance.features.lead.entity.Contact;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.ContactRepositoryWrapper;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.features.person.service.PersonWriteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class WhatsAppLeadUpdateServiceImpl implements WhatsAppLeadUpdateService {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[0-9]{10}$");

    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final ContactRepositoryWrapper contactRepositoryWrapper;
    private final LeadWriteService leadWriteService;
    private final PersonReadService personReadService;
    private final PersonWriteService personWriteService;
    private final WhatsAppLeadService whatsAppLeadService;

    @Override
    @Transactional
    public void updateLead(WhatsAppLeadUpdateRequest request) {
        validateAtLeastOneField(request);

        UUID effectiveLeadIdentifier = resolveEffectiveLeadIdentifier(request);
        leadRepositoryWrapper.findByLeadIdentifierWithException(effectiveLeadIdentifier);

        if (hasAlternativeMobile(request)) {
            appendAlternativeMobile(effectiveLeadIdentifier, request);
        }

        LocalDate resolvedHoldFollowUpDate = resolveHoldFollowUpDate(request);
        if (resolvedHoldFollowUpDate != null) {
            leadWriteService.onholdLead(
                    effectiveLeadIdentifier,
                    OnholdLeadRequest.builder()
                            .reasonCode(blankToNull(request.getOnHoldReasonCode()))
                            .holdFollowUpDate(resolvedHoldFollowUpDate)
                            .build()
            );
        }

        if (request.getStatusAction() != null && !request.getStatusAction().isBlank()) {
            applyStatusAction(effectiveLeadIdentifier, request);
        }
    }

    private UUID resolveEffectiveLeadIdentifier(WhatsAppLeadUpdateRequest request) {
        if (request.getLeadIdentifier() != null) {
            return request.getLeadIdentifier();
        }
        return resolveLeadIdentifierFromMobileNumber(request.getMobileNumber());
    }

    private UUID resolveLeadIdentifierFromMobileNumber(String mobileNumber) {
        if (mobileNumber == null || mobileNumber.isBlank()) {
            throw new BadRequestException("leadIdentifier or mobileNumber is required");
        }
        String normalized = mobileNumber.trim();
        if (!MOBILE_PATTERN.matcher(normalized).matches()) {
            throw new BadRequestException("mobileNumber must be exactly 10 digits");
        }
        return whatsAppLeadService.resolveLeadIdentifierByPrimaryMobileNumber(normalized)
                .orElseThrow(() -> new BadRequestException("No lead found for mobileNumber"));
    }

    private LocalDate resolveHoldFollowUpDate(WhatsAppLeadUpdateRequest request) {
        Integer days = request.getHoldFollowUpInDays();
        if (days != null) {
            if (days <= 0) {
                throw new BadRequestException("holdFollowUpInDays must be a positive number");
            }
            return LocalDate.now().plusDays(days.longValue());
        }
        return request.getHoldFollowUpDate();
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void validateAtLeastOneField(WhatsAppLeadUpdateRequest request) {
        boolean hasAlt = request.getAlternativeMobileNumber() != null
                && !request.getAlternativeMobileNumber().isBlank();
        boolean hasHold = request.getHoldFollowUpDate() != null
                || (request.getHoldFollowUpInDays() != null && request.getHoldFollowUpInDays() > 0);
        boolean hasStatus = request.getStatusAction() != null && !request.getStatusAction().isBlank();
        if (!hasAlt && !hasHold && !hasStatus) {
            throw new BadRequestException(
                    "At least one of statusAction, holdFollowUpDate, holdFollowUpInDays, or alternativeMobileNumber is required"
            );
        }
    }

    private boolean hasAlternativeMobile(WhatsAppLeadUpdateRequest request) {
        return request.getAlternativeMobileNumber() != null
                && !request.getAlternativeMobileNumber().isBlank();
    }

    private void appendAlternativeMobile(UUID leadIdentifier, WhatsAppLeadUpdateRequest request) {
        String normalized = request.getAlternativeMobileNumber().trim();
        if (!MOBILE_PATTERN.matcher(normalized).matches()) {
            throw new BadRequestException("alternativeMobileNumber must be exactly 10 digits");
        }

        Long personId = resolvePrimaryContactPersonId(leadIdentifier);
        PersonResponse person = personReadService.getPersonById(personId);

        List<MobileNumberDetails> merged = new ArrayList<>();
        if (person.getMobileNumbers() != null) {
            merged.addAll(person.getMobileNumbers());
        }

        boolean alreadyPresent = merged.stream()
                .anyMatch(m -> m.getNumber() != null && normalized.equals(m.getNumber().trim()));
        if (alreadyPresent) {
            return;
        }

        boolean whatsappAvailable = request.getAlternativeMobileIsWhatsappAvailable() == null
                ? Boolean.TRUE
                : request.getAlternativeMobileIsWhatsappAvailable();

        merged.add(MobileNumberDetails.builder()
                .number(normalized)
                .isPrimary(false)
                .isWhatsappAvailable(whatsappAvailable)
                .build());

        PersonUpdateRequest updateRequest = PersonUpdateRequest.builder()
                .firstName(person.getFirstName())
                .middleName(person.getMiddleName())
                .lastName(person.getLastName())
                .email(person.getEmail())
                .mobileNumbers(merged)
                .dateOfBirth(person.getDateOfBirth())
                .gender(person.getGender())
                .build();

        personWriteService.updatePerson(personId, updateRequest);
    }

    private Long resolvePrimaryContactPersonId(UUID leadIdentifier) {
        Lead lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        UUID contactIdentifier = getPrimaryContactIdentifier(lead);
        if (contactIdentifier == null) {
            throw new BadRequestException("Lead has no primary contact for alternative mobile update");
        }
        Contact contact = contactRepositoryWrapper.findByIdentifierWithException(contactIdentifier);
        return contact.getPersonId();
    }

    private UUID getPrimaryContactIdentifier(Lead lead) {
        Long primaryContactId = null;
        if (lead.getOtherDetails() != null) {
            primaryContactId = lead.getOtherDetails().getPrimaryContactId();
        }
        if (primaryContactId == null && lead.getContacts() != null && !lead.getContacts().isEmpty()) {
            primaryContactId = lead.getContacts().getFirst();
        }
        if (primaryContactId != null) {
            Contact contact = contactRepositoryWrapper.findByIdWithException(primaryContactId);
            return contact.getIdentifier();
        }
        return null;
    }

    private void applyStatusAction(UUID leadIdentifier, WhatsAppLeadUpdateRequest request) {
        String action = request.getStatusAction().trim().toLowerCase(Locale.ROOT);
        switch (action) {
            case "dropoff" -> leadWriteService.dropoffLead(
                    leadIdentifier,
                    DropoffLeadRequest.builder().reasonCode(request.getReasonCode()).build()
            );
            case "resume" -> leadWriteService.resumeLead(leadIdentifier);
            case "reject" -> leadWriteService.rejectLead(
                    leadIdentifier,
                    RejectLeadRequest.builder().reasonCode(request.getReasonCode()).build()
            );
            default -> throw new BadRequestException("statusAction must be dropoff, resume, or reject");
        }
    }
}
