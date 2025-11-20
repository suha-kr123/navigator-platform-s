package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.advisor.dto.*;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonCreateResponse;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.person.service.PersonWriteService;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.repository.SourcingChannelRepositoryWrapper;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelWriteService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class AdvisorWriteServiceImpl implements AdvisorWriteService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final PersonWriteService personWriteService;
    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final SourcingChannelWriteService sourcingChannelWriteService;
    private final SourcingChannelRepositoryWrapper sourcingChannelRepositoryWrapper;
    private final CodeMasterService codeMasterService;

    @Override
    public UUID createAdvisor(CreateAdvisorRequest request) {
        // Create person record
        PersonCreateRequest personRequest = buildPersonCreateRequest(request);
        PersonCreateResponse personResponse = personWriteService.createPerson(personRequest);

        // Create advisor
        Advisor advisor = new Advisor();
        advisor.setIdentifier(UUID.randomUUID());
        advisor.setPersonId(personResponse.getId());
        advisor.setStatus(AdvisorStatus.CREATED);

        Advisor savedAdvisor = advisorRepositoryWrapper.saveWithException(advisor);

        return savedAdvisor.getIdentifier();
    }

    @Override
    public void updateAdvisor(UUID identifier, UpdateAdvisorRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);

        // Update person details
        PersonUpdateRequest personUpdateRequest = buildPersonUpdateRequest(request);
        personWriteService.updatePerson(advisor.getPersonId(), personUpdateRequest);

        advisorRepositoryWrapper.saveWithException(advisor);
    }

    @Override
    public void updateSourcingDetails(UUID identifier, UpdateSourcingDetailsRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);

        // Build marketing details object
        SourcingChannelRequest.MarketingDetails marketingDetails =
                SourcingChannelRequest.MarketingDetails.builder()
                        .sourceId(request.getSourceId())
                        .campaignId(request.getCampaignId())
                        .sourcedBy(request.getSourcedBy())
                        .build();

        // Build sourcing channel request
        SourcingChannelRequest sourcingChannelRequest = new SourcingChannelRequest(
                request.getSourcingChannel(),
                request.getMarketingSource(),
                marketingDetails
        );

        SourcingChannelResponse sourcingChannelResponse;
        Long sourceChannelId = advisor.getSourceChannelId();
        if (advisor.getSourceChannelId() != null) {
            sourcingChannelResponse = sourcingChannelWriteService.update(advisor.getSourceChannelId(), sourcingChannelRequest);
        } else {
            sourcingChannelResponse = sourcingChannelWriteService.create(sourcingChannelRequest);
        }

        if (sourcingChannelResponse != null && sourcingChannelResponse.getId() != null) {
            sourceChannelId = sourcingChannelResponse.getId();
            advisor.setSourceChannelId(sourceChannelId);
        }

        advisorRepositoryWrapper.saveWithException(advisor);
    }

    @Override
    public void updateQualificationDetails(UUID identifier, UpdateQualificationDetailsRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);

        // Validate highestQualification against global QUALIFICATION_MASTER
        if (request.getHighestQualification() != null) {
            List<CodeValueResponse> qualifications =
                    codeMasterService.getAllCodeValuesByCodeKey(SystemControlledMasterCodes.QUALIFICATION_MASTER, true);
            String provided = request.getHighestQualification();
            boolean isValid = qualifications.stream()
                    .anyMatch(cv ->
                            provided.equalsIgnoreCase(cv.getKey()) ||
                            (cv.getValue() != null && provided.equalsIgnoreCase(cv.getValue()))
                    );
            if (!isValid) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid highestQualification. Provide a valid key or value from QUALIFICATION_MASTER.");
            }
        }

        QualificationDetails qualificationDetails = advisor.getQualificationDetails();
        if (qualificationDetails == null) {
            qualificationDetails = new QualificationDetails();
        }

        if (request.getHighestQualification() != null) {
        qualificationDetails.setHighestQualification(request.getHighestQualification());
        }
        advisor.setQualificationDetails(qualificationDetails);

        advisorRepositoryWrapper.saveWithException(advisor);
    }

    @Override
    public void updateOccupationDetails(UUID identifier, UpdateOccupationDetailsRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);

        // Validate occupationType against OCCUPATION_TYPE_MASTER
        if (request.getOccupationType() != null) {
            List<CodeValueResponse> occupationTypes =
                    codeMasterService.getAllCodeValuesByCodeKey(SystemControlledMasterCodes.OCCUPATION_TYPE_MASTER, true);
            String providedType = request.getOccupationType();
            boolean isValidType = occupationTypes.stream()
                    .anyMatch(cv ->
                            providedType.equalsIgnoreCase(cv.getKey()) ||
                            (cv.getValue() != null && providedType.equalsIgnoreCase(cv.getValue()))
                    );
            if (!isValidType) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid occupationType. Provide a valid key or value from OCCUPATION_TYPE_MASTER.");
            }
        }
        // Validate occupation against OCCUPATION_MASTER
        if (request.getOccupation() != null) {
            List<CodeValueResponse> occupations =
                    codeMasterService.getAllCodeValuesByCodeKey(SystemControlledMasterCodes.OCCUPATION_MASTER, true);
            String providedOcc = request.getOccupation();
            boolean isValidOcc = occupations.stream()
                    .anyMatch(cv ->
                            providedOcc.equalsIgnoreCase(cv.getKey()) ||
                            (cv.getValue() != null && providedOcc.equalsIgnoreCase(cv.getValue()))
                    );
            if (!isValidOcc) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid occupation. Provide a valid key or value from OCCUPATION_MASTER.");
            }
        }

        OtherDetails otherDetails = advisor.getOtherDetails();
        if (otherDetails == null) {
            otherDetails = new OtherDetails();
        }

        if (request.getOccupationType() != null) {
        otherDetails.setOccupationType(request.getOccupationType());
        }
        if (request.getOccupation() != null) {
        otherDetails.setOccupation(request.getOccupation());
        }
        advisor.setOtherDetails(otherDetails);

        advisorRepositoryWrapper.saveWithException(advisor);
    }

    @Override
    public UUID rejectAdvisor(UUID identifier, RejectAdvisorRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);
        advisor.setStatus(AdvisorStatus.REJECTED);
        if (request != null && request.getRejected() != null) {
            AdvisorRemarks advisorRemarks = advisor.getRemarks();
            if (advisorRemarks == null) {
                advisorRemarks = new AdvisorRemarks();
            }
            advisorRemarks.setRejected(request.getRejected());
            advisor.setRemarks(advisorRemarks);
        }
        Advisor.RejectionDetails rejectionDetails = advisor.getRejectionDetails();
        if (rejectionDetails == null) {
            rejectionDetails = new Advisor.RejectionDetails();
        }
        rejectionDetails.setRejectionDate(LocalDateTime.now());
        rejectionDetails.setRejectedBy(UserContext.getUsername());
        advisor.setRejectionDetails(rejectionDetails);

        Advisor saved = advisorRepositoryWrapper.saveWithException(advisor);
        return saved.getIdentifier();
    }

    @Override
    public UUID activateAdvisor(UUID identifier) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);
        advisor.setStatus(AdvisorStatus.ACTIVE);
        advisorRepositoryWrapper.saveWithException(advisor);
        return identifier;
    }

    // Convert CreateAdvisorRequest to PersonCreateRequest
    private PersonCreateRequest buildPersonCreateRequest(CreateAdvisorRequest request) {
        PersonCreateRequest personRequest = new PersonCreateRequest();

        // Map mobile number
        personRequest.setMobileNumbers(Collections.singletonList(
                mapCreateMobileNumber(request.getMobileNumberDetails())
        ));

        // Map personal details
        if (request.getPersonalDetails() != null) {
            personRequest.setFirstName(request.getPersonalDetails().getFirstName());
            personRequest.setMiddleName(request.getPersonalDetails().getMiddleName());
            personRequest.setLastName(request.getPersonalDetails().getLastName());
            personRequest.setDateOfBirth(request.getPersonalDetails().getDateOfBirth());
            personRequest.setGender(request.getPersonalDetails().getGender());
        }

        return personRequest;
    }

    private com.nivasafinance.features.person.entity.MobileNumberDetails mapCreateMobileNumber(
            MobileNumberDetails numberDetails) {
        boolean isPrimary = numberDetails.getIsPrimary() == null || Boolean.TRUE.equals(numberDetails.getIsPrimary());
        return new com.nivasafinance.features.person.entity.MobileNumberDetails(
                numberDetails.getMobileNumber(),
                isPrimary,
                numberDetails.getIsWhatsappAvailable()
        );
    }

    private List<com.nivasafinance.features.person.entity.MobileNumberDetails> mapUpdateMobileNumbers(
            List<MobileNumberDetails> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return Collections.emptyList();
        }
        List<com.nivasafinance.features.person.entity.MobileNumberDetails> mapped = new ArrayList<>();
        for (MobileNumberDetails number : numbers) {
            mapped.add(new com.nivasafinance.features.person.entity.MobileNumberDetails(
                    number.getMobileNumber(),
                    number.getIsPrimary(),
                    number.getIsWhatsappAvailable()
            ));
        }
        return mapped;
    }

    // Convert UpdateAdvisorRequest to PersonUpdateRequest
    private PersonUpdateRequest buildPersonUpdateRequest(UpdateAdvisorRequest request) {
        PersonUpdateRequest personRequest = new PersonUpdateRequest();

        // Map mobile numbers
        if (request.getMobileNumberDetails() != null) {
            List<com.nivasafinance.features.person.entity.MobileNumberDetails> mobiles =
                    mapUpdateMobileNumbers(request.getMobileNumberDetails());
            personRequest.setMobileNumbers(mobiles);
        }

        // Map personal details
        if (request.getPersonalDetails() != null) {
            personRequest.setFirstName(request.getPersonalDetails().getFirstName());
            personRequest.setMiddleName(request.getPersonalDetails().getMiddleName());
            personRequest.setLastName(request.getPersonalDetails().getLastName());
            personRequest.setDateOfBirth(request.getPersonalDetails().getDateOfBirth());
            personRequest.setGender(request.getPersonalDetails().getGender());
        }

        return personRequest;
    }

}

