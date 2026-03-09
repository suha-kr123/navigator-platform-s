package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.advisor.dto.AdvisorDashboardFilters;
import com.nivasafinance.features.advisor.dto.AdvisorDashboardResponse;
import com.nivasafinance.features.advisor.dto.AdvisorResponse;
import com.nivasafinance.features.advisor.dto.AdvisorSearchRequest;
import com.nivasafinance.features.advisor.dto.AdvisorBasicResponse;
import com.nivasafinance.features.advisor.dto.AdvisorTemplateResponse;
import com.nivasafinance.features.advisor.dto.PersonalDetails;
import com.nivasafinance.features.advisor.dto.SourcingDetailsResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.exception.AdvisorExceptionFactory;
import com.nivasafinance.features.advisor.repository.AdvisorDashboardWrapper;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.usermanagement.service.UserReadService;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.offices.exception.OfficeNotFoundException;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.person.dto.PersonResponse;
import com.nivasafinance.features.person.entity.MobileNumberDetails;
import com.nivasafinance.features.referral.enums.EntityType;
import com.nivasafinance.features.referral.service.ReferralCodeRegistryService;
import com.nivasafinance.features.staff.dto.StaffResponse;
import com.nivasafinance.features.staff.service.StaffReadService;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.repository.SourcingChannelRepositoryWrapper;
import lombok.AllArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class AdvisorReadServiceImpl implements AdvisorReadService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final SourcingChannelRepositoryWrapper sourcingChannelRepositoryWrapper;
    private final ReferralCodeRegistryService referralCodeRegistryService;
    private final CodeMasterService codeMasterService;
    private final OfficeReadService officeReadService;
    private final AdvisorDashboardWrapper advisorDashboardWrapper;
    private final MessageSource messageSource;
    private final StaffReadService staffReadService;
    private final UserReadService userReadService;

    @Override
    public Optional<Advisor> findAdvisorByUsername(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        return advisorRepositoryWrapper.findByUsername(username);
    }

    @Override
    public AdvisorResponse getAdvisorByIdentifier(UUID identifier) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);
        return mapEntityToResponse(advisor);
    }

    @Override
    public SourcingDetailsResponse getSourcingDetails(UUID identifier) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(identifier);

        if (advisor.getSourceChannelId() == null) {
            return SourcingDetailsResponse.builder().build();
        }

        SourcingChannelResponse sourcingChannel = sourcingChannelRepositoryWrapper
                .findByIdAsResponseWithException(advisor.getSourceChannelId());

        SourcingChannelResponse sanitized = new SourcingChannelResponse(
                null,
                sourcingChannel.getSourcingIdentifier(),
                sourcingChannel.getSourcingChannel(),
                sourcingChannel.getMarketingSource(),
                sourcingChannel.getMarketingDetails());

        return SourcingDetailsResponse.builder()
                .sourcingChannelDetails(sanitized)
                .build();
    }

    @Override
    public AdvisorTemplateResponse getAdvisorTemplate() {
        List<CodeValueResponse> rejectionReasons = codeMasterService.getAllCodeValuesByCodeKey(
                SystemControlledMasterCodes.ADVISOR_REJECTION_REASON_MASTER, true);
        List<CodeValueResponse> dormantReasons = codeMasterService.getAllCodeValuesByCodeKey(
                SystemControlledMasterCodes.ADVISOR_DORMANT_REASON_MASTER, true);
        List<CodeValueResponse> occupationTypes = codeMasterService.getAllCodeValuesByCodeKey(
                SystemControlledMasterCodes.OCCUPATION_TYPE_MASTER, true);
        List<CodeValueResponse> occupations = codeMasterService.getAllCodeValuesByCodeKey(
                SystemControlledMasterCodes.OCCUPATION_MASTER, true);
        List<CodeValueResponse> qualifications = codeMasterService.getAllCodeValuesByCodeKey(
                SystemControlledMasterCodes.QUALIFICATION_MASTER, true);
        List<CodeValueResponse> segmentations = codeMasterService.getAllCodeValuesByCodeKey(
                SystemControlledMasterCodes.SEGMENTATION_MASTER, true);
        return AdvisorTemplateResponse.builder()
                .advisorRejectionReasons(rejectionReasons)
                .advisorDormantReasons(dormantReasons)
                .occupationTypes(occupationTypes)
                .occupations(occupations)
                .qualifications(qualifications)
                .segmentations(segmentations)
                .build();
    }

    @Override
    public PaginatedResponse<AdvisorBasicResponse> getAllAdvisors(PaginationRequest paginationRequest, String name,
                                                                  String mobileNumber) {
        return advisorRepositoryWrapper.findAllAdvisors(paginationRequest, name, mobileNumber);
    }

    @Override
    public PaginatedResponse<AdvisorBasicResponse> searchAdvisors(
            PaginationRequest paginationRequest, AdvisorSearchRequest request) {
        return advisorRepositoryWrapper.searchAdvisorsByPhoneNumber(paginationRequest, request);
    }

    @Override
    public Optional<AdvisorBasicResponse> findAdvisorByMobileNo(String mobileNumber) {
        return advisorRepositoryWrapper.findAdvisorByMobileNo(mobileNumber);
    }

    @Override
    public PaginatedResponse<AdvisorDashboardResponse> getAdvisorDashboard(
            PaginationRequest paginationRequest,
            AdvisorDashboardFilters filters) {
        return advisorDashboardWrapper.findAdvisorDashboard(paginationRequest, filters);
    }

    // Map Advisor entity to response DTO
    private AdvisorResponse mapEntityToResponse(Advisor advisor) {
        PersonResponse person = userReadService.getPersonForUser(advisor.getUsername());

        AdvisorResponse response = new AdvisorResponse();
        response.setId(advisor.getId());
        response.setIdentifier(advisor.getIdentifier());

        // Map personal details from Person entity
        PersonalDetails personalDetails = PersonalDetails.builder()
                .firstName(person.getFirstName())
                .middleName(person.getMiddleName())
                .lastName(person.getLastName())
                .mobileNumbers(person.getMobileNumbers())
                .dateOfBirth(person.getDateOfBirth())
                .gender(person.getGender())
                .build();
        response.setPersonalDetails(personalDetails);

        // Map sourcing details if exists
        response.setStatus(advisor.getStatus());
        response.setRemarks(advisor.getRemarks());
        response.setQualificationDetails(advisor.getQualificationDetails());
        response.setOtherDetails(advisor.getOtherDetails());
        response.setSegmentationDetails(advisor.getSegmentationDetails());
        response.setOwnerUsername(advisor.getOwner());

        // Map office details
        String officeKey = advisor.getOfficeKey();
        response.setOfficeKey(officeKey);
        if (officeKey != null) {
            try {
                response.setOfficeName(officeReadService.getOfficeByKey(officeKey).getName());
            } catch (OfficeNotFoundException e) {
                response.setOfficeName(null);
            }
        } else {
            response.setOfficeName(null);
        }

        response.setReferralCode(advisor.getReferralCode());

        if (advisor.getSourceChannelId() != null) {
            SourcingChannelResponse sourcingChannel = sourcingChannelRepositoryWrapper
                    .findByIdAsResponseWithException(advisor.getSourceChannelId());
            response.setSourcingChannelDetails(sourcingChannel);
            String referredByCode = sourcingChannel.getMarketingDetails() != null
                    ? sourcingChannel.getMarketingDetails().getReferredByCode() : null;
            if (referredByCode != null && !referredByCode.isBlank()) {
                response.setReferredByCode(referredByCode);
                var registry = referralCodeRegistryService.getReferralCodeByCode(referredByCode);
                if (registry != null) {
                    response.setReferredByIdentifier(registry.getEntityIdentifier());
                    response.setReferredByType(registry.getEntityType());
                    resolveReferrerNameAndPhone(response, registry.getEntityType(), registry.getEntityIdentifier());
                }
            }
        } else {
            response.setSourcingChannelDetails(null);
        }

        return response;
    }

    private void resolveReferrerNameAndPhone(AdvisorResponse response, EntityType entityType, UUID entityIdentifier) {
        if (entityIdentifier == null) {
            return;
        }
        try {
            switch (entityType) {
                case STAFF -> staffReadService.getStaffByIdentifier(entityIdentifier)
                        .map(StaffResponse::getUserResponse)
                        .filter(ur -> ur != null)
                        .map(ur -> ur.getPersonResponse())
                        .filter(pr -> pr != null)
                        .ifPresent(person -> {
                            response.setReferredByName(person.getDisplayName());
                            response.setReferredByNumber(extractPrimaryMobile(person.getMobileNumbers()));
                        });
                case APPLICANT -> advisorRepositoryWrapper.findReferrerDisplayInfo(entityType, entityIdentifier)
                        .ifPresent(info -> {
                            response.setReferredByName(info.name());
                            response.setReferredByNumber(info.phone());
                        });
                case ADVISOR -> {
                    Advisor referrerAdvisor = advisorRepositoryWrapper.findByIdentifierWithException(entityIdentifier);
                    PersonResponse referrerPerson = userReadService.getPersonForUser(referrerAdvisor.getUsername());
                    response.setReferredByName(referrerPerson.getDisplayName());
                    response.setReferredByNumber(extractPrimaryMobile(referrerPerson.getMobileNumbers()));
                }
                default -> { }
            }
        } catch (Exception ignored) {
        }
    }

    private static String extractPrimaryMobile(java.util.List<MobileNumberDetails> mobileNumbers) {
        if (mobileNumbers == null || mobileNumbers.isEmpty()) {
            return null;
        }
        return mobileNumbers.stream()
                .filter(m -> Boolean.TRUE.equals(m.getIsPrimary()))
                .findFirst()
                .map(MobileNumberDetails::getNumber)
                .orElse(null);
    }

    @Override
    public PaginatedResponse<AdvisorBasicResponse> getMyAdvisors(PaginationRequest paginationRequest) {
        String username = UserContext.getUsername();
        if (!ValidationUtils.isNonNull(username)) {
            throw AdvisorExceptionFactory.noCurrentUser(messageSource);
        }
        return advisorRepositoryWrapper.findAdvisorsByUsername(username, paginationRequest);
    }

    @Override
    public PaginatedResponse<AdvisorBasicResponse> getAdvisorsByReferralCode(String referralCode, PaginationRequest paginationRequest) {
        return advisorRepositoryWrapper.findAdvisorsByReferralCode(referralCode, paginationRequest);
    }

}
