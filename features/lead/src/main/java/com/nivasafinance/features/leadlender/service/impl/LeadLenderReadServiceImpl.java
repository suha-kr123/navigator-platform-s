package com.nivasafinance.features.leadlender.service.impl;

import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.leadlender.dto.LeadLenderResponse;
import com.nivasafinance.features.leadlender.dto.RemarksResponse;
import com.nivasafinance.features.leadlender.dto.StageResponse;
import com.nivasafinance.features.leadlender.entity.LeadLender;
import com.nivasafinance.features.leadlender.enums.LeadLenderStatus;
import com.nivasafinance.features.leadlender.exception.LeadLenderNotFoundException;
import com.nivasafinance.features.leadlender.repository.LeadLenderRepositoryWrapper;
import com.nivasafinance.features.leadlender.service.LeadLenderReadService;
import com.nivasafinance.features.lender.lender.dto.LenderResponseData;
import com.nivasafinance.features.lender.lender.service.LenderReadService;
import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService;
import com.nivasafinance.features.master.codemaster.SystemControlledMasterCodes;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeValueRepositoryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadLenderReadServiceImpl implements LeadLenderReadService {

    private final LeadLenderRepositoryWrapper leadLenderRepositoryWrapper;
    private final LeadRepositoryWrapper leadRepositoryWrapper;
    private final LenderReadService lenderReadService;
    private final LenderOfficeReadService lenderOfficeReadService;
    private final MasterCodeValueRepositoryWrapper masterCodeValueRepositoryWrapper;

    @Override
    public List<LeadLenderResponse> getLeadLenders(UUID leadId, List<String> statusList) {
        // Validate that lead exists first
        leadRepositoryWrapper.findByIdWithException(leadId);
        
        List<LeadLender> leadLenders = leadLenderRepositoryWrapper.findByLeadId(leadId);
        
        // Filter by status if provided
        if (statusList != null && !statusList.isEmpty()) {
            List<LeadLenderStatus> statuses = statusList.stream()
                .map(String::toUpperCase)
                .map(LeadLenderStatus::valueOf)
                .collect(Collectors.toList());
            
            leadLenders = leadLenders.stream()
                .filter(lender -> statuses.contains(lender.getStatus()))
                .collect(Collectors.toList());
        }
        
        return leadLenders.stream()
            .map(this::createLeadLenderResponse)
            .collect(Collectors.toList());
    }

    @Override
    public LeadLenderResponse getLeadLenderByIdentifier(UUID leadId, UUID lenderIdentifier) {
        // Validate that lead exists
        leadRepositoryWrapper.findByIdWithException(leadId);
        
        LeadLender leadLender = leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier);
        
        // Validate that the lender belongs to the specified lead
        if (!leadLender.getLeadId().equals(leadId)) {
            throw new LeadLenderNotFoundException(
                "Lead lender with identifier " + lenderIdentifier + " does not belong to lead " + leadId
            );
        }
        
        return createLeadLenderResponse(leadLender);
    }

    private LeadLenderResponse createLeadLenderResponse(LeadLender leadLender) {
        LenderResponseData lender = lenderReadService.getByKey(leadLender.getLenderKey());

        LenderOfficeReponseData lenderOffice = null;
        if (leadLender.getLenderOfficeKey() != null) {
            lenderOffice = lenderOfficeReadService.getByKey(leadLender.getLenderOfficeKey());
        }

        StageResponse stageResponse = null;
        if (leadLender.getStage() != null) {
            try {
                MasterCodeValue stageValue = masterCodeValueRepositoryWrapper
                    .findByKeyAndCodeKeyWithException(leadLender.getStage(), SystemControlledMasterCodes.STAGE_MASTER);
                String value = stageValue.getValue() != null && stageValue.getValue().getDefault() != null 
                    ? stageValue.getValue().getDefault() 
                    : leadLender.getStage();
                stageResponse = new StageResponse(leadLender.getStage(), value);
            } catch (Exception e) {
                // If stage not found in master, just return the key as value
                stageResponse = new StageResponse(leadLender.getStage(), leadLender.getStage());
            }
        }

        RemarksResponse remarksResponse = null;
        if (leadLender.getRemarks() != null) {
            // Get the latest remark for the current status
            String latestRemarkKey = leadLender.getRemarks().getLatestRemark(leadLender.getStatus().name());
            
            if (latestRemarkKey != null) {
                try {
                    MasterCodeValue remarksValue = masterCodeValueRepositoryWrapper
                        .findByKeyAndCodeKeyWithException(latestRemarkKey, SystemControlledMasterCodes.REMARKS_MASTER);
                    String value = remarksValue.getValue() != null && remarksValue.getValue().getDefault() != null
                        ? remarksValue.getValue().getDefault()
                        : latestRemarkKey;
                    
                    remarksResponse = new RemarksResponse(latestRemarkKey, value);
                } catch (Exception e) {
                    // If remarks not found in master, just return the key as value
                    remarksResponse = new RemarksResponse(latestRemarkKey, latestRemarkKey);
                }
            }
        }

        return new LeadLenderResponse(
            leadLender.getLenderIdentifier(),
            leadLender.getLeadId(),
            leadLender.getStatus(),
            lender,
            lenderOffice,
            leadLender.getLoginDetails(),
            leadLender.getRmDetails(),
            leadLender.getApprovedDetails(),
            stageResponse,
            remarksResponse
        );
    }
}

