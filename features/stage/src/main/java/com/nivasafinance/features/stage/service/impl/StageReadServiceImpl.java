package com.nivasafinance.features.stage.service.impl;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.stage.dto.StageConfigResponse;
import com.nivasafinance.features.stage.dto.StageFilterResponse;
import com.nivasafinance.features.stage.dto.StageTemplateResponse;
import com.nivasafinance.features.stage.entity.StageConfig;
import com.nivasafinance.features.stage.repository.StageConfigRepositoryWrapper;
import com.nivasafinance.features.stage.service.StageReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StageReadServiceImpl implements StageReadService {

    private final StageConfigRepositoryWrapper stageConfigRepositoryWrapper;
    private final CodeMasterService codeMasterService;

    @Override
    public StageConfigResponse getStageByKey(String key) {
        StageConfig stageConfig = stageConfigRepositoryWrapper.findByKeyWithException(key);
        
        StageConfig.StageConfigDetails stageConfigDetails = stageConfig.getStageConfig();
        List<String> possibleNextStages = ValidationUtils.isNonNull(stageConfigDetails) 
                && ValidationUtils.isNonNull(stageConfigDetails.getPossibleNextStages())
                ? stageConfigDetails.getPossibleNextStages() 
                : Collections.emptyList();
        
        StageConfig.AssigneeRoles assigneeRolesEntity = stageConfig.getAssigneeRoles();
        List<String> assigneeRoles = ValidationUtils.isNonNull(assigneeRolesEntity) 
                && ValidationUtils.isNonNull(assigneeRolesEntity.getRoles())
                ? assigneeRolesEntity.getRoles() 
                : Collections.emptyList();
        
        List<CodeValueResponse> subStages = fetchSubStages(stageConfig.getSubStagesCode());
        
        return StageConfigResponse.from(stageConfig, possibleNextStages, assigneeRoles, subStages);
    }

    private List<CodeValueResponse> fetchSubStages(String subStagesCode) {
        if (!ValidationUtils.isNonNullOrEmpty(subStagesCode)) {
            return Collections.emptyList();
        }
        
        List<CodeValueResponse> codeValues = codeMasterService.getAllCodeValuesByCodeKey(subStagesCode, true);
        
        return codeValues;
    }

    @Override
    public StageTemplateResponse getStageTemplate(String stageKey) {
        StageConfigResponse stageConfig = getStageByKey(stageKey);
        
        return StageTemplateResponse.builder()
                .stageKey(stageConfig.getKey())
                .stageName(stageConfig.getName())
                .stageDescription(stageConfig.getDescription())
                .possibleNextStages(stageConfig.getPossibleNextStages())
                .availableSubStages(stageConfig.getSubStages())
                .build();
    }
    
    @Override
    public List<StageFilterResponse> getAllActiveStages() {
        return stageConfigRepositoryWrapper.findAllActiveStages().stream()
                .map(StageFilterResponse::from)
                .collect(Collectors.toList());
    }
}
