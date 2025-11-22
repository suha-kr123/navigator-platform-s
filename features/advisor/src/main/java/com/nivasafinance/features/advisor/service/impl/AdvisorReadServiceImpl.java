package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.advisor.dto.AdvisorTemplateResponse;
import com.nivasafinance.features.advisor.dto.AdvisorResponse;
import com.nivasafinance.features.advisor.dto.AdvisorSearchRequest;
import com.nivasafinance.features.advisor.dto.AdvisorSearchResponse;
import com.nivasafinance.features.advisor.dto.PersonalDetails;
import com.nivasafinance.features.advisor.dto.SourcingDetailsResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorReadService;
import com.nivasafinance.features.advisorlead.repository.AdvisorLeadMappingRepositoryWrapper;
import com.nivasafinance.features.lead.dto.LeadBasicResponse;
import com.nivasafinance.features.person.entity.Person;
import com.nivasafinance.features.person.repository.PersonRepositoryWrapper;
import com.nivasafinance.features.sourcechannel.dto.SourcingChannelResponse;
import com.nivasafinance.features.sourcechannel.repository.SourcingChannelRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class AdvisorReadServiceImpl implements AdvisorReadService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;
    private final PersonRepositoryWrapper personRepositoryWrapper;
    private final SourcingChannelRepositoryWrapper sourcingChannelRepositoryWrapper;
    private final CodeMasterService codeMasterService;
    private final AdvisorLeadMappingRepositoryWrapper advisorLeadMappingRepositoryWrapper;

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
    public PaginatedResponse<AdvisorSearchResponse> searchAdvisors(
            PaginationRequest paginationRequest, AdvisorSearchRequest request) {
        return advisorRepositoryWrapper.searchAdvisorsByPhoneNumber(paginationRequest, request);
    }

    @Override
    public PaginatedResponse<LeadBasicResponse> getLeadsByAdvisorId(UUID advisorId, PaginationRequest paginationRequest) {
        return advisorLeadMappingRepositoryWrapper.findLeadsByAdvisorIdWithException(advisorId, paginationRequest);
    }

    // Map Advisor entity to response DTO
    private AdvisorResponse mapEntityToResponse(Advisor advisor) {
        // Fetch person details
        Person person = personRepositoryWrapper.findByIdWithException(advisor.getPersonId());
        
        AdvisorResponse response = new AdvisorResponse();
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
        return response;
    }
}
