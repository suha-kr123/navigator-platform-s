package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.events.BusinessEvent;
import com.nivasafinance.common.events.SystemEvent;
import com.nivasafinance.common.events.payload.AdvisorCreationEventPayload;
import com.nivasafinance.common.events.payload.AdvisorStatusChangeEventPayload;
import com.nivasafinance.common.events.payload.AdvisorUpdateEventPayload;
import com.nivasafinance.features.advisor.dto.*;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.enums.AdvisorStatus;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorWriteService;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.usermanagement.service.UserWriteService;
import com.nivasafinance.features.referral.dto.ReferralCodeRegistryResponse;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.referral.service.ReferralCodeRegistryService;
import com.nivasafinance.features.rolemanagement.admin.service.AdminUserRoleService;
import com.nivasafinance.features.rolemanagement.role.dto.AddUserRolesRequest;
import com.nivasafinance.features.person.dto.PersonCreateRequest;
import com.nivasafinance.features.person.dto.PersonUpdateRequest;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelRequest;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.service.SourcingChannelWriteService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.advisor.exception.AdvisorExceptionFactory;
import com.nivasafinance.features.usermanagement.dto.UserResponse;
import com.nivasafinance.features.usermanagement.exception.UserAlreadyExistsException;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class AdvisorWriteServiceImpl implements AdvisorWriteService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final SourcingChannelWriteService sourcingChannelWriteService;
    private final CodeMasterService codeMasterService;
    private final OfficeReadService officeReadService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessageSource messageSource;
    private final ReferralCodeRegistryService referralCodeRegistryService;
    private final UserReadService userReadService;
    private final UserWriteService userWriteService;
    private final AdminUserRoleService adminUserRoleService;

    private static final String ROLE_ADVISOR_SELF = "ADVISOR_SELF";

    @Override
    public UUID createAdvisor(CreateAdvisorRequest request) {
        String mobile = getPrimaryMobile(request);
        if (mobile == null || mobile.isBlank()) {
            throw new BadRequestException("Primary mobile number is required");
        }

        String advisorUsername;
        try {
            advisorUsername = userReadService.findUserByPersonMobile(mobile)
                    .map(UserResponse::getUsername)
                    .orElseGet(() -> userWriteService.createUserForMobile(mobile, buildPersonCreateRequest(request)).getUsername());
        } catch (UserAlreadyExistsException e) {
            throw AdvisorExceptionFactory.advisorAlreadyExistsForMobileNumber(mobile, messageSource);
        }
        if (advisorRepositoryWrapper.findByUsername(advisorUsername).isPresent()) {
            throw AdvisorExceptionFactory.advisorAlreadyExistsForMobileNumber(mobile, messageSource);
        }

        Advisor advisor = new Advisor();
        advisor.setIdentifier(UUID.randomUUID());
        advisor.setUsername(advisorUsername);
        advisor.setStatus(AdvisorStatus.CREATED);
        if (request.getOfficeKey() != null) {
            officeReadService.getOfficeByKey(request.getOfficeKey()); //validate
            advisor.setOfficeKey(request.getOfficeKey());
        }
        else
            advisor.setOfficeKey("HQ");

        // Handle preferred call times
        if (request.getPreferredCallStartTime() != null || request.getPreferredCallEndTime() != null) {
            OtherDetails otherDetails = new OtherDetails();
            if (request.getPreferredCallStartTime() != null && request.getPreferredCallEndTime() != null) {
                if (request.getPreferredCallStartTime().isAfter(request.getPreferredCallEndTime())) {
                    throw new BadRequestException("Preferred call start time cannot be after preferred call end time");
                }
                otherDetails.setPreferredCallStartTime(request.getPreferredCallStartTime());
                otherDetails.setPreferredCallEndTime(request.getPreferredCallEndTime());
            } else {
                throw new BadRequestException("Both preferred call start time and end time must be provided together");
            }
            advisor.setOtherDetails(otherDetails);
        }

        //set sales owner as the current user
        advisor.setOwner(UserContext.getUsername());

        generateReferralCode(advisor);
        Advisor savedAdvisor = advisorRepositoryWrapper.saveWithException(advisor);

        handleSourcingChannel(savedAdvisor, request.getSourcingChannelRequest());

        // addUserRoles with primaryRole invokes setPrimaryRole internally, which sets any existing primary to isPrimary=false before setting the new one.
        AddUserRolesRequest roleRequest = new AddUserRolesRequest();
        roleRequest.setRoles(Collections.singletonList(ROLE_ADVISOR_SELF));
        roleRequest.setPrimaryRole(ROLE_ADVISOR_SELF);
        adminUserRoleService.addUserRoles(savedAdvisor.getUsername(), roleRequest);

        // Publish ADVISOR_CREATED event
        AdvisorCreationEventPayload payload = AdvisorCreationEventPayload.builder()
                .id(savedAdvisor.getId())
                .advisorIdentifier(savedAdvisor.getIdentifier())
                .mobileNumber(mobile)
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.ADVISOR_CREATED.toString(), payload, username)
        );

        return savedAdvisor.getIdentifier();
    }

    @Override
    public void updateAdvisor(UUID identifier, UpdateAdvisorRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);

        userWriteService.updatePersonForUser(advisor.getUsername(), buildPersonUpdateRequest(request));

        // Update office key if provided
        if (request.getOfficeKey() != null) {
            advisor.setOfficeKey(request.getOfficeKey());
        }

        // Update owner if provided
        if (request.getOwner() != null) {
            advisor.setOwner(request.getOwner());
        }

        // Handle preferred call times
        OtherDetails otherDetails = advisor.getOtherDetails();
        if (otherDetails == null) {
            otherDetails = new OtherDetails();
        }
        if (request.getPreferredCallStartTime() != null && request.getPreferredCallEndTime() != null) {
            if (request.getPreferredCallStartTime().isAfter(request.getPreferredCallEndTime())) {
                throw new BadRequestException("Preferred call start time cannot be after preferred call end time");
            }
            otherDetails.setPreferredCallStartTime(request.getPreferredCallStartTime());
            otherDetails.setPreferredCallEndTime(request.getPreferredCallEndTime());
        } else if (request.getPreferredCallStartTime() != null || request.getPreferredCallEndTime() != null) {
            // If only one is provided, clear both
            otherDetails.setPreferredCallStartTime(null);
            otherDetails.setPreferredCallEndTime(null);
        }
        advisor.setOtherDetails(otherDetails);

        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish ADVISOR_UPDATED event
        publishAdvisorUpdatedEvent(advisor);
    }

    private void publishAdvisorUpdatedEvent(Advisor advisor) {
        com.nivasafinance.features.person.dto.PersonResponse person = userReadService.getPersonForUser(advisor.getUsername());
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

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(BusinessEvent.ADVISOR_UPDATED.toString(), payload, username)
        );
    }

    @Override
    public void updateSourcingDetails(UUID identifier, UpdateSourcingDetailsRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);

        // Build marketing details object
        SourcingChannelRequest.MarketingDetails marketingDetails =
                SourcingChannelRequest.MarketingDetails.builder()
                        .sourceId(request.getSourceId())
                        .sourceUrl(request.getSourceUrl())
                        .campaignId(request.getCampaignId())
                        .referredByCode(request.getReferredByCode())
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

        // Publish ADVISOR_UPDATED event
        publishAdvisorUpdatedEvent(advisor);
    }

    @Override
    public void updateQualificationDetails(UUID identifier, UpdateQualificationDetailsRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);

        // Validate highestQualification against global QUALIFICATION_MASTER
        if (request.getHighestQualification() != null) {
            List<CodeValueResponse> qualifications =
                    codeMasterService.getAllCodeValuesByCodeKey(SystemControlledMasterCodes.QUALIFICATION_MASTER, true, "default");
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

        qualificationDetails.setHighestQualification(request.getHighestQualification());
        advisor.setQualificationDetails(qualificationDetails);

        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish ADVISOR_UPDATED event
        publishAdvisorUpdatedEvent(advisor);
    }

    @Override
    public void updateOccupationDetails(UUID identifier, UpdateOccupationDetailsRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);

        // Validate occupationType against OCCUPATION_TYPE_MASTER
        if (request.getOccupationType() != null) {
            List<CodeValueResponse> occupationTypes =
                    codeMasterService.getAllCodeValuesByCodeKey(SystemControlledMasterCodes.OCCUPATION_TYPE_MASTER, true, "default");
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
                    codeMasterService.getAllCodeValuesByCodeKey(SystemControlledMasterCodes.OCCUPATION_MASTER, true, "default");
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

        otherDetails.setOccupationType(request.getOccupationType());
        otherDetails.setOccupation(request.getOccupation());
        advisor.setOtherDetails(otherDetails);

        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish ADVISOR_UPDATED event
        publishAdvisorUpdatedEvent(advisor);
    }

    @Override
    public void updateSegmentationDetails(UUID identifier, UpdateSegmentationDetailsRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);

        // Validate segmentation against SEGMENTATION_MASTER
        if (request.getSegmentation() != null) {
            List<CodeValueResponse> segmentations =
                    codeMasterService.getAllCodeValuesByCodeKey(SystemControlledMasterCodes.SEGMENTATION_MASTER, true, "default");
            String provided = request.getSegmentation();
            boolean isValid = segmentations.stream()
                    .anyMatch(cv ->
                            provided.equalsIgnoreCase(cv.getKey()) ||
                            (cv.getValue() != null && provided.equalsIgnoreCase(cv.getValue()))
                    );
            if (!isValid) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid segmentation. Provide a valid key or value from SEGMENTATION_MASTER.");
            }
        }

        SegmentationDetails segmentationDetails = advisor.getSegmentationDetails();
        if (segmentationDetails == null) {
            segmentationDetails = new SegmentationDetails();
        }

        if (request.getSegmentation() != null) {
            segmentationDetails.setSegmentation(request.getSegmentation());
        }
        advisor.setSegmentationDetails(segmentationDetails);

        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish ADVISOR_UPDATED event
        publishAdvisorUpdatedEvent(advisor);
    }

    @Override
    public void rejectAdvisor(UUID identifier, RejectAdvisorRequest request) {
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

        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish ADVISOR_REJECTED event
        String reason = advisor.getRemarks() != null ? advisor.getRemarks().getRejected() : null;
        publishAdvisorStatusChangeEvent(advisor, BusinessEvent.ADVISOR_REJECTED, reason);
    }

    @Override
    public void dormantAdvisor(UUID identifier, DormantAdvisorRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);
        advisor.setStatus(AdvisorStatus.DORMANT);
        if (request != null && request.getDormant() != null) {
            AdvisorRemarks advisorRemarks = advisor.getRemarks();
            if (advisorRemarks == null) {
                advisorRemarks = new AdvisorRemarks();
            }
            advisorRemarks.setDormant(request.getDormant());
            advisor.setRemarks(advisorRemarks);
        }

        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish ADVISOR_DORMANT event
        String reason = advisor.getRemarks() != null ? advisor.getRemarks().getDormant() : null;
        publishAdvisorStatusChangeEvent(advisor, BusinessEvent.ADVISOR_DORMANT, reason);
    }

    @Override
    public void activateAdvisor(UUID identifier) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);
        advisor.setStatus(AdvisorStatus.ACTIVE);
        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish ADVISOR_ACTIVE event
        publishAdvisorStatusChangeEvent(advisor, BusinessEvent.ADVISOR_ACTIVE, null);
    }

    @Override
    public void outOfGeoAdvisor(UUID identifier, OutOfGeoAdvisorRequest request) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);
        advisor.setStatus(AdvisorStatus.OUT_OF_GEO);
        if (request != null && request.getOutOfGeo() != null) {
            AdvisorRemarks advisorRemarks = advisor.getRemarks();
            if (advisorRemarks == null) {
                advisorRemarks = new AdvisorRemarks();
            }
            advisorRemarks.setOutOfGeo(request.getOutOfGeo());
            advisor.setRemarks(advisorRemarks);
        }

        advisorRepositoryWrapper.saveWithException(advisor);

        // Publish ADVISOR_OUT_OF_GEO event
        String reason = advisor.getRemarks() != null ? advisor.getRemarks().getOutOfGeo() : null;
        publishAdvisorStatusChangeEvent(advisor, BusinessEvent.ADVISOR_OUT_OF_GEO, reason);
    }

    private void publishAdvisorStatusChangeEvent(Advisor advisor, BusinessEvent event, String reason) {
        AdvisorStatusChangeEventPayload payload = AdvisorStatusChangeEventPayload.builder()
                .id(advisor.getId())
                .advisorIdentifier(advisor.getIdentifier())
                .reason(reason)
                .build();

        String username = UserContext.getUsername();
        applicationEventPublisher.publishEvent(
                new SystemEvent<>(event.toString(), payload, username)
        );
    }

    private String getPrimaryMobile(CreateAdvisorRequest request) {
        return request.getMobileNumberDetails() != null
                ? request.getMobileNumberDetails().getMobileNumber()
                : null;
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

        // Map mobile numbers (from mobileNumberDetails or from personalDetails.mobileNumbers e.g. Self API)
        if (request.getMobileNumberDetails() != null && !request.getMobileNumberDetails().isEmpty()) {
            List<com.nivasafinance.features.person.entity.MobileNumberDetails> mobiles =
                    mapUpdateMobileNumbers(request.getMobileNumberDetails());
            personRequest.setMobileNumbers(mobiles);
        } else if (request.getPersonalDetails() != null && request.getPersonalDetails().getMobileNumbers() != null) {
            personRequest.setMobileNumbers(request.getPersonalDetails().getMobileNumbers());
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
    

    private void handleSourcingChannel(Advisor advisor, SourcingChannelRequest sourcingChannelRequest) {
        if (ValidationUtils.isNull(sourcingChannelRequest)) {
            return;
        }
        if (advisor.getSourceChannelId() != null) {
            sourcingChannelWriteService.update(advisor.getSourceChannelId(), sourcingChannelRequest);
        } else {
            SourcingChannelResponse response = sourcingChannelWriteService.create(sourcingChannelRequest);
            if (response != null && response.getId() != null) {
                advisor.setSourceChannelId(response.getId());
                advisorRepositoryWrapper.saveWithException(advisor);
            }
        }
    }

    private void generateReferralCode(Advisor advisor) {
        ReferralCodeRegistryResponse response = referralCodeRegistryService.generateReferralCode(EntityType.ADVISOR, advisor.getIdentifier());
        if (response == null) {
            throw AdvisorExceptionFactory.createFailed(messageSource);
        }
        advisor.setReferralCode(response.getReferralCode());
    }

}

