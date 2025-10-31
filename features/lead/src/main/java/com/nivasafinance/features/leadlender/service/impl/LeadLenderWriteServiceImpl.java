package com.nivasafinance.features.leadlender.service.impl;

import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.leadlender.dto.ApprovedDetails;
import com.nivasafinance.features.leadlender.dto.CreateLeadLenderRequest;
import com.nivasafinance.features.leadlender.dto.CreateLeadLenderResponse;
import com.nivasafinance.features.leadlender.dto.LoginDetails;
import com.nivasafinance.features.leadlender.dto.RejectLeadLenderRequest;
import com.nivasafinance.features.leadlender.dto.RmDetails;
import com.nivasafinance.features.leadlender.dto.UpdateLeadLenderRequest;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeadLenderWriteServiceImpl implements LeadLenderWriteService {

    private final LeadLenderRepositoryWrapper leadLenderRepositoryWrapper;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final LenderReadService lenderReadService;
    private final LenderOfficeReadService lenderOfficeReadService;

    @Override
    @Transactional
    public CreateLeadLenderResponse createLeadLender(UUID leadIdentifier, CreateLeadLenderRequest request) {
        // Validate that lead exists and get internal ID
        var lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
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
        return new CreateLeadLenderResponse(savedLeadLender.getLenderIdentifier());
    }

    @Override
    @Transactional
    public void updateLeadLender(UUID leadIdentifier, UUID lenderIdentifier, UpdateLeadLenderRequest request) {
        // Validate that lead exists and get internal ID
        var lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
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
            existingEntity.setLenderOfficeKey(request.getLenderOfficeKey());
        }

        if (request.getRmName() != null) {
            RmDetails rmDetails = existingEntity.getRmDetails();
            if (rmDetails == null) {
                rmDetails = new RmDetails();
            }
            rmDetails.setName(request.getRmName());
            existingEntity.setRmDetails(rmDetails);
        }

        if (request.getRmMobileNumber() != null) {
            RmDetails rmDetails = existingEntity.getRmDetails();
            if (rmDetails == null) {
                rmDetails = new RmDetails();
            }
            rmDetails.setMobileNumber(request.getRmMobileNumber());
            existingEntity.setRmDetails(rmDetails);
        }

        // Handle login details fields
        if (request.getLoginId() != null) {
            LoginDetails loginDetails = existingEntity.getLoginDetails();
            if (loginDetails == null) {
                loginDetails = new LoginDetails();
            }
            loginDetails.setLoginId(request.getLoginId());
            existingEntity.setLoginDetails(loginDetails);
        }

        if (request.getLoginDate() != null) {
            LoginDetails loginDetails = existingEntity.getLoginDetails();
            if (loginDetails == null) {
                loginDetails = new LoginDetails();
            }
            loginDetails.setLoginDate(request.getLoginDate());
            existingEntity.setLoginDetails(loginDetails);
        }

        if (request.getLoginFees() != null) {
            LoginDetails loginDetails = existingEntity.getLoginDetails();
            if (loginDetails == null) {
                loginDetails = new LoginDetails();
            }
            loginDetails.setLoginFees(request.getLoginFees());
            existingEntity.setLoginDetails(loginDetails);
        }

        // Handle approved details fields
        if (request.getApprovedAmount() != null) {
            ApprovedDetails approvedDetails = existingEntity.getApprovedDetails();
            if (approvedDetails == null) {
                approvedDetails = new ApprovedDetails();
            }
            approvedDetails.setApprovedAmount(request.getApprovedAmount());
            existingEntity.setApprovedDetails(approvedDetails);
        }

        if (request.getRoi() != null) {
            ApprovedDetails approvedDetails = existingEntity.getApprovedDetails();
            if (approvedDetails == null) {
                approvedDetails = new ApprovedDetails();
            }
            approvedDetails.setRoi(request.getRoi());
            existingEntity.setApprovedDetails(approvedDetails);
        }

        if (request.getTenureValue() != null) {
            ApprovedDetails approvedDetails = existingEntity.getApprovedDetails();
            if (approvedDetails == null) {
                approvedDetails = new ApprovedDetails();
            }
            approvedDetails.setTenureValue(request.getTenureValue());
            existingEntity.setApprovedDetails(approvedDetails);
        }

        if (request.getTenureType() != null) {
            ApprovedDetails approvedDetails = existingEntity.getApprovedDetails();
            if (approvedDetails == null) {
                approvedDetails = new ApprovedDetails();
            }
            approvedDetails.setTenureType(request.getTenureType());
            existingEntity.setApprovedDetails(approvedDetails);
        }

        if (request.getApprovedDate() != null) {
            ApprovedDetails approvedDetails = existingEntity.getApprovedDetails();
            if (approvedDetails == null) {
                approvedDetails = new ApprovedDetails();
            }
            approvedDetails.setApprovedDate(request.getApprovedDate());
            existingEntity.setApprovedDetails(approvedDetails);
        }

        if (request.getProcessingFees() != null) {
            ApprovedDetails approvedDetails = existingEntity.getApprovedDetails();
            if (approvedDetails == null) {
                approvedDetails = new ApprovedDetails();
            }
            approvedDetails.setProcessingFees(request.getProcessingFees());
            existingEntity.setApprovedDetails(approvedDetails);
        }

        if (request.getSanctionExpiry() != null) {
            ApprovedDetails approvedDetails = existingEntity.getApprovedDetails();
            if (approvedDetails == null) {
                approvedDetails = new ApprovedDetails();
            }
            approvedDetails.setSanctionExpiry(request.getSanctionExpiry());
            existingEntity.setApprovedDetails(approvedDetails);
        }

        if (request.getInsuranceFees() != null) {
            ApprovedDetails approvedDetails = existingEntity.getApprovedDetails();
            if (approvedDetails == null) {
                approvedDetails = new ApprovedDetails();
            }
            approvedDetails.setInsuranceFees(request.getInsuranceFees());
            existingEntity.setApprovedDetails(approvedDetails);
        }

        // Handle stage
        if (request.getStage() != null) {
            existingEntity.setStage(request.getStage());
        }

        // Handle remarks - set simple key
        if (request.getRemarks() != null) {
            existingEntity.setRemarks(request.getRemarks());
        }

        leadLenderRepositoryWrapper.saveWithException(existingEntity);
    }

    @Override
    @Transactional
    public void rejectLeadLender(UUID leadIdentifier, UUID lenderIdentifier, RejectLeadLenderRequest request) {
        // Validate that lead exists and get internal ID
        var lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
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
                "Lead lender relationship is already rejected. Current status: " + 
                existingEntity.getStatus()
            );
        }

        // Update the status to REJECTED
        existingEntity.setStatus(LeadLenderStatus.REJECTED);
        
        // Set the reject remark key
        existingEntity.setRemarks(request.getRemarks());
        
        leadLenderRepositoryWrapper.saveWithException(existingEntity);
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

