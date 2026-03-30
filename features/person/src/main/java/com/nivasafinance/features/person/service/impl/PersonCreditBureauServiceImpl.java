package com.nivasafinance.features.person.service.impl;

import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.IdentifierData;
import com.nivasafinance.features.consent.dto.ConsentReceivedRequest;
import com.nivasafinance.features.consent.dto.CreateAndSendConsent;
import com.nivasafinance.features.consent.entity.Consent;
import com.nivasafinance.features.consent.service.ConsentWriteService;
import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryRequest;
import com.nivasafinance.features.creditbureau.dto.CreditBureauEnquiryResponse;
import com.nivasafinance.features.creditbureau.entity.CreditBureauEnquiry;
import com.nivasafinance.features.creditbureau.enums.CreditBureauEnquiryStatus;
import com.nivasafinance.features.creditbureau.exception.CreditBureauExceptionFactory;
import com.nivasafinance.features.creditbureau.service.CreditBureauReadService;
import com.nivasafinance.features.creditbureau.service.CreditBureauWriteService;
import com.nivasafinance.features.person.dto.CreditBureauEnquiryInitiationResult;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.dto.RecordCbConsentResult;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.person.service.PersonCreditBureauService;
import com.nivasafinance.features.person.service.PersonReadService;
import com.nivasafinance.services.creditbureau.dto.CreditBureauPersonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonCreditBureauServiceImpl implements PersonCreditBureauService {

    private final PersonReadService personReadService;
    private final CreditBureauWriteService creditBureauWriteService;
    private final CreditBureauReadService creditBureauReadService;
    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final ConsentWriteService consentWriteService;
    private final MessageSource messageSource;

    @Value("${creditbureau.stale-period-in-days:30}")
    private Integer stalePeriodInDays;
    @Value("${creditbureau.consent-validity-period-in-days:90}")
    private Integer consentValidityPeriodInDays;

    @Override
    @Transactional
    public CreditBureauEnquiryInitiationResult initiateCreditBureauEnquiry(CreditBureauEnquiryRequest request) {
        Long personId = request.getPersonId();
        String entityType = request.getEntityType();
        Long entityId = request.getEntityId();
        log.info("Initiating credit bureau enquiry for personId: {}, entityType: {}", personId, entityType);
        
        PersonResponse personResponse = personReadService.getPersonById(personId);
        
        // STEP 1: Check for valid (non-expired) SUCCESS enquiry
        Optional<Map<String, Object>> validSuccessEnquiryOpt = 
            personRepositoryWrapper.findActiveSuccessCreditBureauEnquiry(personId, stalePeriodInDays);
        
        if (validSuccessEnquiryOpt.isPresent()) {
            Long validEnquiryId = (Long) validSuccessEnquiryOpt.get().get("enquiry_id");
            log.info("Found valid non-expired SUCCESS enquiry ID: {} for personId: {}", validEnquiryId, personId);
            CreditBureauEnquiry enquiry = creditBureauReadService.getCbEnquiryEntityById(validEnquiryId);
            return CreditBureauEnquiryInitiationResult.builder()
                    .response(CreditBureauEnquiryResponse.toCbEnquiryResponse(enquiry))
                    .build();
        }
        
        // STEP 2: Check for in-progress enquiry (INITIATED or PROCESSING)
        Long latestEnquiryId = getLatestEnquiryId(personResponse);
        if (latestEnquiryId != null) {
            Optional<CreditBureauEnquiryResponse> latestEnquiryOpt = creditBureauReadService.getCbEnquiryById(latestEnquiryId);
            if (latestEnquiryOpt.isPresent()) {
                CreditBureauEnquiryStatus status = latestEnquiryOpt.get().getStatus();
                if (status == CreditBureauEnquiryStatus.INITIATED || status == CreditBureauEnquiryStatus.PROCESSING) {
                    log.info("Found in-progress enquiry ID: {} with status: {} for personId: {}, returning existing enquiry",
                            latestEnquiryId, status, personId);
                    return CreditBureauEnquiryInitiationResult.builder()
                            .response(latestEnquiryOpt.get())
                            .build();
                }
            }
        }

        // STEP 3: Check for existing valid consent before creating new enquiry
        Optional<Map<String, Object>> validConsentOpt =
                personRepositoryWrapper.findValidConsentForPerson(personId, consentValidityPeriodInDays);

        UUID consentIdentifier = null;

        if (validConsentOpt.isPresent()) {
            UUID existingConsentIdentifier = (UUID) validConsentOpt.get().get("consent_identifier");
            log.info("Found valid existing consent for personId: {}, will reuse for new enquiry", personId);
            consentIdentifier = existingConsentIdentifier;
        }

        // STEP 4: Create new enquiry (INITIATED)
        log.info("Creating new enquiry for personId: {}", personId);
        CreditBureauEnquiryResponse initiateResponse = creditBureauWriteService.initiateEnquiry(personId);
        Long enquiryId = initiateResponse.getId();
        UUID enquiryIdentifier = initiateResponse.getIdentifier();

        log.info("Enquiry initiated with ID: {}, identifier: {} for personId: {}",
                enquiryId, enquiryIdentifier, personId);

        // Update person's cb_enquiry_id array and latestEnquiryId
        updatePersonCbEnquiryIds(personId, personResponse, enquiryId);

        // STEP 5: Use existing consent or create new one
        if (validConsentOpt.isPresent()) {
            // Link existing consent to new enquiry
            Long existingConsentId = ((Number) validConsentOpt.get().get("consent_id")).longValue();
            creditBureauWriteService.linkConsentToEnquiry(enquiryId, existingConsentId);
            log.info("Linked existing consent ID: {} to new enquiry ID: {}, triggering credit bureau pull", existingConsentId, enquiryId);
            initiateResponse.setConsentIdentifier(consentIdentifier);
            
            // Trigger credit bureau pull since consent is already available
            CreditBureauEnquiry enquiry = creditBureauReadService.getCbEnquiryEntityById(enquiryId);
            CompletableFuture<CreditBureauEnquiryResponse> asyncPullFuture = triggerCreditBureauPull(enquiry, personId, request.getAddressesForCreditBureauPull());
            return CreditBureauEnquiryInitiationResult.builder()
                    .response(initiateResponse)
                    .asyncPullFuture(asyncPullFuture)
                    .build();
        } else {
            // Create new consent and send link
            // Get recipient phone for consent link (first mobile)
            String recipientPhone = null;
            if (personResponse.getMobileNumbers() != null && !personResponse.getMobileNumbers().isEmpty()) {
                recipientPhone = personResponse.getMobileNumbers().stream()
                        .map(com.nivasafinance.features.person.entity.MobileNumberDetails::getNumber)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
            }

            Consent saved = consentWriteService.createAndSendConsent(CreateAndSendConsent.builder()
                    .personId(personId)
                    .type("CB")
                    .enquiryIdentifier(enquiryIdentifier)
                    .leadIdentifier(request.getLeadIdentifier())
                    .contactIdentifier(request.getContactIdentifier())
                    .entityType(entityType)
                    .entityId(entityId)
                    .recipientPhone(recipientPhone)
                    .build());

            // Link consent to enquiry immediately when consent is created/sent
            creditBureauWriteService.linkConsentToEnquiry(enquiryId, saved.getId());
            log.info("Linked new consent ID: {} to enquiry ID: {} immediately after creation", saved.getId(), enquiryId);

            initiateResponse.setConsentIdentifier(saved.getIdentifier());
        }

        return CreditBureauEnquiryInitiationResult.builder()
                .response(initiateResponse)
                .build();
    }

    @Override
    @Transactional
    public CompletableFuture<CreditBureauEnquiryResponse> onConsentGranted(Long consentId, UUID enquiryIdentifier, List<AddressData> addressesForCreditBureauPull) {
        CreditBureauEnquiry enquiry = creditBureauReadService.getCbEnquiryEntityByIdentifier(enquiryIdentifier);

        Long personId = personRepositoryWrapper.findPersonIdByCbEnquiryId(enquiry.getId())
                .orElseThrow(() -> CreditBureauExceptionFactory.enquiryConsentNotLinkable(messageSource));

        if (enquiry.getStatus() != CreditBureauEnquiryStatus.INITIATED) {
            throw CreditBureauExceptionFactory.enquiryConsentNotLinkable(messageSource);
        }

        // If consent is already linked (from immediate linking when created or reuse scenario), skip linking but validate it matches
        if (enquiry.getConsentId() == null) {
            creditBureauWriteService.linkConsentToEnquiry(enquiry.getId(), consentId);
        } else if (!enquiry.getConsentId().equals(consentId)) {
            // Consent mismatch - different consent already linked
            throw CreditBureauExceptionFactory.enquiryConsentNotLinkable(messageSource);
        } else {
            log.info("Consent ID: {} already linked to enquiry ID: {} (linked when created), proceeding with pull",
                    consentId, enquiry.getId());
        }

        // Update person.consent_details: add CB consent (do not remove existing)
        Person person = personRepositoryWrapper.findByIdWithException(personId);
        List<Person.ConsentInfo> list = person.getConsentDetails() != null
                ? new ArrayList<>(person.getConsentDetails()) : new ArrayList<>();
        list.add(new Person.ConsentInfo(consentId, "CB"));
        person.setConsentDetails(list);
        personRepositoryWrapper.saveWithException(person);

        // Build CreditBureauPersonData and trigger async pull
        return triggerCreditBureauPull(enquiry, personId, addressesForCreditBureauPull);
    }

    @Override
    @Transactional
    public RecordCbConsentResult recordCbConsentReceived(Long personId) {
        Consent saved = consentWriteService.createConsentReceived(
                ConsentReceivedRequest.builder()
                        .personId(personId)
                        .type("CB")
                        .build());
        Person person = personRepositoryWrapper.findByIdWithException(personId);
        List<Person.ConsentInfo> list = person.getConsentDetails() != null
                ? new ArrayList<>(person.getConsentDetails()) : new ArrayList<>();
        list.add(new Person.ConsentInfo(saved.getId(), "CB"));
        person.setConsentDetails(list);
        personRepositoryWrapper.saveWithException(person);
        return RecordCbConsentResult.builder()
                .consentIdentifier(saved.getIdentifier())
                .build();
    }

    /**
     * Triggers the credit bureau pull asynchronously for the given enquiry.
     *
     * @param enquiry The credit bureau enquiry
     * @param personId The person ID
     * @param addressesForCreditBureauPull when non-null and non-empty, used as CB addresses only; otherwise person addresses
     */
    private CompletableFuture<CreditBureauEnquiryResponse> triggerCreditBureauPull(
            CreditBureauEnquiry enquiry,
            Long personId,
            List<AddressData> addressesForCreditBureauPull) {
        PersonResponse personResponse = personReadService.getPersonById(personId);
        List<AddressData> addresses = resolveAddressesForCreditBureauPull(personId, addressesForCreditBureauPull);
        List<IdentifierData> identifiers = personReadService.getIdentifiers(personId);
        CreditBureauPersonData personData = CreditBureauPersonData.builder()
                .firstName(personResponse.getFirstName())
                .middleName(personResponse.getMiddleName())
                .lastName(personResponse.getLastName())
                .gender(personResponse.getGender() != null ? personResponse.getGender().name() : null)
                .dateOfBirth(personResponse.getDateOfBirth())
                .mobileNumbers(personResponse.getMobileNumbers() != null
                        ? personResponse.getMobileNumbers().stream()
                        .map(com.nivasafinance.features.person.entity.MobileNumberDetails::getNumber)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
                        : new ArrayList<>())
                .extData(personResponse.getExtData())
                .addresses(addresses)
                .identifiers(identifiers)
                .build();

      return creditBureauWriteService.executeCreditBureauFlowAsync(enquiry, personId, personData)
                .thenApply(flowResponse -> {
                    try {
                        updatePersonAfterEnquiryCompletion(personId, enquiry.getId(), flowResponse.getStatus());
                    } catch (Exception e) {
                        log.error("Failed to update person after enquiry completion for enquiryId: {}", enquiry.getId(), e);
                    }
                    return flowResponse;
                })
                .exceptionally(throwable -> {
                    log.error("Credit bureau flow failed for enquiryId: {}", enquiry.getId(), throwable);
                    return null;
                });
    }

    private List<AddressData> resolveAddressesForCreditBureauPull(
            Long personId,
            List<AddressData> addressesForCreditBureauPull) {
        if (addressesForCreditBureauPull != null && !addressesForCreditBureauPull.isEmpty()) {
            return addressesForCreditBureauPull;
        }
        return personReadService.getAddresses(personId);
    }

    private Long getLatestEnquiryId(PersonResponse personResponse) {
        // First check latestEnquiryId in cbDetails
        if (personResponse.getCbDetails() != null && 
            personResponse.getCbDetails().getLatestEnquiryId() != null) {
            return personResponse.getCbDetails().getLatestEnquiryId();
        }
        
        // Fallback: check cbEnquiryId list and get the latest one
        List<Long> cbEnquiryIds = personResponse.getCbEnquiryId();
        if (cbEnquiryIds != null && !cbEnquiryIds.isEmpty()) {
            return cbEnquiryIds.get(cbEnquiryIds.size() - 1);
        }
        
        return null;
    }
    
    private void updatePersonCbEnquiryIds(Long personId, PersonResponse personResponse, Long enquiryId) {
        Person person = personRepositoryWrapper.findByIdWithException(personId);

        List<Long> personEnquiryIds = person.getCbEnquiryId();
        if (personEnquiryIds == null) {
            personEnquiryIds = new java.util.ArrayList<>();
        }
        if (!personEnquiryIds.contains(enquiryId)) {
            personEnquiryIds.add(enquiryId);
        }
        person.setCbEnquiryId(personEnquiryIds);

        // Update latestEnquiryId in cbDetails
        Person.CreditBureauDetails cbDetails = person.getCbDetails();
        if (cbDetails == null) {
            cbDetails = Person.CreditBureauDetails.builder().build();
        }
        cbDetails.setLatestEnquiryId(enquiryId);
        person.setCbDetails(cbDetails);

        personRepositoryWrapper.saveWithException(person);
    }

    private void updatePersonAfterEnquiryCompletion(Long personId, Long enquiryId, CreditBureauEnquiryStatus status) {
        try {
            Person person = personRepositoryWrapper.findByIdWithException(personId);

            Person.CreditBureauDetails cbDetails = person.getCbDetails();
            if (cbDetails == null) {
            cbDetails = Person.CreditBureauDetails.builder().build();
        }

            if (CreditBureauEnquiryStatus.SUCCESS == status) {
                cbDetails.setLatestSuccessEnquiryId(enquiryId);
            }
            cbDetails.setLatestEnquiryId(enquiryId);
            person.setCbDetails(cbDetails);

            personRepositoryWrapper.saveWithException(person);

            log.info("Updated person credit bureau details for personId: {}, enquiryId: {}, status: {}", 
                personId, enquiryId, status);
        } catch (Exception e) {
            log.error("Failed to update person after enquiry completion for personId: {}, enquiryId: {}", 
                personId, enquiryId, e);
        }
    }
}