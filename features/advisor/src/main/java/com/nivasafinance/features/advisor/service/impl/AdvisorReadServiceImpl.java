package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorDashboardFilters;
import com.nivasafinance.features.advisor.dto.AdvisorDashboardResponse;
import com.nivasafinance.features.advisor.dto.AdvisorLeadResponse;
import com.nivasafinance.features.advisor.dto.AdvisorResponse;
import com.nivasafinance.features.advisor.dto.AdvisorSearchRequest;
import com.nivasafinance.features.advisor.dto.AdvisorBasicResponse;
import com.nivasafinance.features.advisor.dto.AdvisorTemplateResponse;
import com.nivasafinance.features.advisor.dto.PersonalDetails;
import com.nivasafinance.features.advisor.dto.SourcingDetailsResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorDashboardWrapper;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisorlead.repository.AdvisorLeadMappingRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.offices.exception.OfficeNotFoundException;
import com.nivasafinance.features.offices.service.OfficeReadService;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.repository.SourcingChannelRepositoryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class AdvisorReadServiceImpl implements AdvisorReadService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final SourcingChannelRepositoryWrapper sourcingChannelRepositoryWrapper;
    private final CodeMasterService codeMasterService;
    private final AdvisorLeadMappingRepositoryWrapper advisorLeadMappingRepositoryWrapper;
    private final OfficeReadService officeReadService;
    private final AdvisorDashboardWrapper advisorDashboardWrapper;

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

        SourcingChannelResponse sourcingChannel = 
                sourcingChannelRepositoryWrapper.findByIdAsResponseWithException(advisor.getSourceChannelId());

        SourcingChannelResponse sanitized = new SourcingChannelResponse(
                null,
                sourcingChannel.getSourcingIdentifier(),
                sourcingChannel.getSourcingChannel(),
                sourcingChannel.getMarketingSource(),
                sourcingChannel.getMarketingDetails()
        );

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
    public PaginatedResponse<AdvisorBasicResponse> getAllAdvisors(PaginationRequest paginationRequest, String name, String mobileNumber) {
        return advisorRepositoryWrapper.findAllAdvisors(paginationRequest, name, mobileNumber);
    }

    @Override
    public PaginatedResponse<AdvisorBasicResponse> searchAdvisors(
            PaginationRequest paginationRequest, AdvisorSearchRequest request) {
        return advisorRepositoryWrapper.searchAdvisorsByPhoneNumber(paginationRequest, request);
    }

    @Override
    public PaginatedResponse<AdvisorLeadResponse> getLeadsByAdvisorId(UUID advisorId, PaginationRequest paginationRequest) {
        return advisorLeadMappingRepositoryWrapper.findLeadsByAdvisorIdWithException(advisorId, paginationRequest);
    }

    @Override
    public PaginatedResponse<AdvisorDashboardResponse> getAdvisorDashboard(
            PaginationRequest paginationRequest,
            AdvisorDashboardFilters filters) {
        return advisorDashboardWrapper.findAdvisorDashboard(paginationRequest, filters);
    }

    // Map Advisor entity to response DTO
    private AdvisorResponse mapEntityToResponse(Advisor advisor) {
        // Fetch person details
        Person person = personRepositoryWrapper.findByIdWithException(advisor.getPersonId());

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
                // If office not found, set officeName to null
                response.setOfficeName(null);
            }
        } else {
            response.setOfficeName(null);
        }

        return response;
    }
}
