package com.nivasafinance.features.leadlender.service.impl;

import com.nivasafinance.features.lead.repository.LeadRepositoryWrapper;
import com.nivasafinance.features.leadlender.dto.LeadLenderResponse;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
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
    public List<LeadLenderResponse> getLeadLenders(UUID leadIdentifier, List<String> statusList) {
        // Validate that lead exists and get internal ID
        var lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        List<LeadLender> leadLenders = leadLenderRepositoryWrapper.findByLeadId(lead.getId());
        
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
    public LeadLenderResponse getLeadLenderByIdentifier(UUID leadIdentifier, UUID lenderIdentifier) {
        // Validate that lead exists and get internal ID
        var lead = leadRepositoryWrapper.findByLeadIdentifierWithException(leadIdentifier);
        
        LeadLender leadLender = leadLenderRepositoryWrapper.findByLenderIdentifierWithException(lenderIdentifier);
        
        // Validate that the lender belongs to the specified lead
        if (!leadLender.getLeadId().equals(lead.getId())) {
            throw new LeadLenderNotFoundException(
                "Lead lender with identifier " + lenderIdentifier + " does not belong to lead " + leadIdentifier
            );
        }
        
        return createLeadLenderResponse(leadLender);
    }

    private LeadLenderResponse createLeadLenderResponse(LeadLender leadLender) {
        // Get lead to retrieve external UUID identifier
        var lead = leadRepositoryWrapper.findByIdWithException(leadLender.getLeadId());
        
        LenderResponseData lender = lenderReadService.getByKey(leadLender.getLenderKey());

        LenderOfficeReponseData lenderOffice = null;
        if (leadLender.getLenderOfficeKey() != null) {
            lenderOffice = lenderOfficeReadService.getByKey(leadLender.getLenderOfficeKey());
        }

        CodeValueResponse stageResponse = null;
        if (leadLender.getStage() != null) {
            try {
                MasterCodeValue stageValue = masterCodeValueRepositoryWrapper
                    .findByKeyAndCodeKeyWithException(leadLender.getStage(), SystemControlledMasterCodes.LENDER_STAGE_MASTER);
                String value = stageValue.getValue() != null && stageValue.getValue().getDefaultValue() != null
                    ? stageValue.getValue().getDefaultValue()
                    : leadLender.getStage();
                String description = stageValue.getDescription() != null && stageValue.getDescription().getDefaultValue() != null
                    ? stageValue.getDescription().getDefaultValue()
                    : null;
                stageResponse = new CodeValueResponse(stageValue.getId(), leadLender.getStage(), value, description, stageValue.isActive());
            } catch (Exception e) {
                // Fallback with minimal data
                stageResponse = new CodeValueResponse(null, leadLender.getStage(), leadLender.getStage(), null, true);
            }
        }

        CodeValueResponse remarksResponse = null;
        if (leadLender.getRemarks() != null) {
            String remarksKey = leadLender.getRemarks();
            try {
                MasterCodeValue remarksValue = masterCodeValueRepositoryWrapper
                    .findByKeyAndCodeKeyWithException(remarksKey, SystemControlledMasterCodes.LENDER_REJECTION_REASON_MASTER);
                String value = remarksValue.getValue() != null && remarksValue.getValue().getDefaultValue() != null
                    ? remarksValue.getValue().getDefaultValue()
                    : remarksKey;
                String description = remarksValue.getDescription() != null && remarksValue.getDescription().getDefaultValue() != null
                    ? remarksValue.getDescription().getDefaultValue()
                    : null;
                remarksResponse = new CodeValueResponse(remarksValue.getId(), remarksKey, value, description, remarksValue.isActive());
            } catch (Exception e) {
                // Fallback with minimal data
                remarksResponse = new CodeValueResponse(null, remarksKey, remarksKey, null, true);
            }
        }

        return new LeadLenderResponse(
            leadLender.getLenderIdentifier(),
            lead.getLeadIdentifier(), // Use external UUID identifier
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

