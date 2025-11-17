package com.nivasafinance.features.leadlender.service.impl;

import com.nivasafinance.common.context.UserContext;
import com.nivasafinance.features.lead.dto.LeadBasicResponse;
import com.nivasafinance.features.lead.dto.LeadResponse;
import com.nivasafinance.features.lead.entity.Lead;
import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.lead.service.LeadReadService;
import com.nivasafinance.features.lead.service.LeadWriteService;
import com.nivasafinance.features.leadlender.dto.*;
import com.nivasafinance.features.leadlender.entity.LeadLender;
import com.nivasafinance.features.leadlender.enums.LeadLenderStatus;
import com.nivasafinance.features.leadlender.exception.InvalidLeadLenderStatusException;
import com.nivasafinance.features.leadlender.exception.InvalidLenderOfficeException;
import com.nivasafinance.features.leadlender.exception.LeadLenderAlreadyExistsException;
import com.nivasafinance.features.leadlender.repository.LeadLenderRepositoryWrapper;
import com.nivasafinance.features.leadlender.service.LeadLenderWriteService;
import com.nivasafinance.features.lender.lender.service.LenderReadService;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadLenderWriteServiceImpl implements LeadLenderWriteService {

    private final LeadLenderRepositoryWrapper leadLenderRepositoryWrapper;
    private final LeadWriteService leadWriteService;
    private final LeadReadService leadReadService;
    private final LenderReadService lenderReadService;
    private final LenderOfficeReadService lenderOfficeReadService;
    private final CodeMasterService codeMasterService;
    private final CodeValueMasterService codeValueMasterService;

    @Override
    @Transactional
    public CreateLeadLenderResponse createLeadLender(UUID leadIdentifier, CreateLeadLenderRequest request) {
        // Validate that lead exists and get internal ID
        var lead = leadReadService.getLeadBasicByIdentifier(leadIdentifier);

        // First validate that the lender exists
        lenderReadService.getByKey(request.getLenderKey());

        // Check if lead-lender relationship already exists
        try {
            LeadLender existingLeadLender = leadLenderRepositoryWrapper
                    .findByLeadIdAndLenderKeyWithException(lead.getId(), request.getLenderKey());
            if (existingLeadLender.getStatus().isInProgress()) {
                throw new LeadLenderAlreadyExistsException(
                        "Lead lender relationship already exists for lead: " + leadIdentifier +
                        " with lender: " + request.getLenderKey()
                );
            }
        } catch (Exception e) {
            // If not found, continue with creation
        }

        LeadLender entity = new LeadLender();
        entity.setLeadId(lead.getId()); // Use internal Long ID
        entity.setLenderKey(request.getLenderKey());
        entity.setStatus(LeadLenderStatus.SELECTED);
        entity.setLenderIdentifier(UUID.randomUUID());

        LeadLender savedLeadLender = leadLenderRepositoryWrapper.saveWithException(entity);
        leadWriteService.touchLead(leadIdentifier);
        return new CreateLeadLenderResponse(savedLeadLender.getLenderIdentifier());
    }

    @Override
    @Transactional
    public void updateLeadLender(UUID leadIdentifier, UUID lenderIdentifier, UpdateLeadLenderRequest request) {
        // Validate that lead exists and get internal ID
        var lead = leadReadService.getLeadBasicByIdentifier(leadIdentifier);

        LeadLender existingEntity = leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier);

        // Validate that the lender belongs to the specified lead
        if (!existingEntity.getLeadId().equals(lead.getId())) {
            throw new InvalidLeadLenderStatusException(
                    "Lead lender with identifier " + lenderIdentifier + " does not belong to lead " + leadIdentifier
            );
        }

        // Check if already rejected
        if (existingEntity.getStatus() == LeadLenderStatus.REJECTED) {
            throw new InvalidLeadLenderStatusException(
                    "Cannot update rejected lead lender relationship"
            );
        }

        // Validate lender office if provided
        if (request.getLenderOfficeKey() != null) {
            validateOfficeForLender(request.getLenderOfficeKey(), existingEntity);
        }
        existingEntity.setLenderOfficeKey(request.getLenderOfficeKey());
        existingEntity.setRmDetails(request.getRmDetails());
        existingEntity.setLoginDetails(request.getLoginDetails());
        existingEntity.setApprovedDetails(request.getApprovedDetails());
        existingEntity.setStage(request.getStage());

        leadLenderRepositoryWrapper.saveWithException(existingEntity);
        leadWriteService.touchLead(leadIdentifier);
    }

    @Override
    @Transactional
    public void rejectLeadLender(UUID leadIdentifier, UUID lenderIdentifier, RejectLeadLenderRequest request) {
        // Validate that lead exists and get internal ID
        LeadBasicResponse lead = leadReadService.getLeadBasicByIdentifier(leadIdentifier);

        LeadLender existingEntity = leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier);

        if(request.getRejectReason() != null){ // check it belongs to correct master
            codeValueMasterService.getCodeValueByKeyAndCodeKey(request.getRejectReason(), SystemControlledMasterCodes.LENDER_REJECTION_REASON_MASTER);
        }

        // Validate that the lender belongs to the specified lead
        if (!existingEntity.getLeadId().equals(lead.getId())) {
            throw new InvalidLeadLenderStatusException(
                    "Lead lender with identifier " + lenderIdentifier + " does not belong to lead " + leadIdentifier
            );
        }

        // Check if already rejected
        if (existingEntity.getStatus() == LeadLenderStatus.REJECTED) {
            throw new InvalidLeadLenderStatusException(
                    "Lead lender relationship is already rejected. Current status: " +
                    existingEntity.getStatus()
            );
        }

        // Update the status to REJECTED
        existingEntity.setStatus(LeadLenderStatus.REJECTED);

        RejectionDetails rejectionDetails = existingEntity.getRejectionDetails();
        if (rejectionDetails == null) {
            rejectionDetails = new RejectionDetails();
        }
        rejectionDetails.setRejectedBy(UserContext.getUsername());
        rejectionDetails.setRejectionDate(LocalDateTime.now());
        rejectionDetails.setRejectionReason(request.getRejectReason());
        rejectionDetails.setRemarks(request.getRemarks());

        existingEntity.setRejectionDetails(rejectionDetails);

        leadLenderRepositoryWrapper.saveWithException(existingEntity);
        leadWriteService.touchLead(leadIdentifier);
    }

    @Override
    @Transactional
    public void submitLeadLender(UUID leadIdentifier, UUID lenderIdentifier) {
        // Validate that lead exists and get internal ID
        var lead = leadReadService.getLeadBasicByIdentifier(leadIdentifier);

        LeadLender existingEntity = leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier);

        // Validate that the lender belongs to the specified lead
        if (!existingEntity.getLeadId().equals(lead.getId())) {
            throw new InvalidLeadLenderStatusException(
                    "Lead lender with identifier " + lenderIdentifier + " does not belong to lead " + leadIdentifier
            );
        }

        // Check if already submitted
        if (existingEntity.getStatus() == LeadLenderStatus.SUBMITTED) {
            throw new InvalidLeadLenderStatusException(
                    "Lead lender relationship is already submitted. Current status: " +
                    existingEntity.getStatus()
            );
        }

        // Check if already rejected
        if (existingEntity.getStatus() == LeadLenderStatus.REJECTED) {
            throw new InvalidLeadLenderStatusException(
                    "Cannot submit rejected lead lender relationship. Current status: " +
                    existingEntity.getStatus()
            );
        }

        // Update the status to SUBMITTED
        existingEntity.setStatus(LeadLenderStatus.SUBMITTED);

        leadLenderRepositoryWrapper.saveWithException(existingEntity);
        leadWriteService.touchLead(leadIdentifier);
    }

    private void validateOfficeForLender(String officeKey, LeadLender existingEntity) {
        LenderOfficeReponseData lenderOffice = lenderOfficeReadService.getByKey(officeKey);

        // Validate that the lender office belongs to the same lender as the lead-lender relationship
        if (!lenderOffice.getLenderKey().equals(existingEntity.getLenderKey())) {
            throw new InvalidLenderOfficeException(
                    "Invalid lender office key: " + officeKey +
                    " for lender: " + existingEntity.getLenderKey()
            );
        }
    }
}

