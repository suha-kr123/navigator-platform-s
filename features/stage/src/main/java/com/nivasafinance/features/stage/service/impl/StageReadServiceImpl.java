package com.nivasafinance.features.stage.service.impl;

import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.stage.dto.StageConfigResponse;
import com.nivasafinance.features.stage.entity.StageConfig;
import com.nivasafinance.features.stage.repository.StageConfigRepositoryWrapper;
import com.nivasafinance.features.stage.service.StageReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        
        List<String> possibleNextStages = extractPossibleNextStages(stageConfig.getStageConfig());
        List<String> assigneeRoles = extractAssigneeRoles(stageConfig.getAssigneeRoles());
        List<CodeValueResponse> subStages = fetchSubStages(stageConfig.getSubStagesCode());
        
        return StageConfigResponse.from(stageConfig, possibleNextStages, assigneeRoles, subStages);
    }

    @SuppressWarnings("unchecked")
    private List<String> extractPossibleNextStages(Map<String, Object> stageConfig) {
        if (!ValidationUtils.isNonNull(stageConfig)) {
            return Collections.emptyList();
        }
        
        Object nextStages = stageConfig.get("possible_next_stages");
        if (nextStages instanceof List) {
            return (List<String>) nextStages;
        }
        
        return Collections.emptyList();
    }

    private List<String> extractAssigneeRoles(Map<String, Object> assigneeRoles) {
        if (!ValidationUtils.isNonNull(assigneeRoles)) {
            return Collections.emptyList();
        }
        
        Object roles = assigneeRoles.get("roles");
        if (roles instanceof List) {
            return ((List<?>) roles).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }

    private List<CodeValueResponse> fetchSubStages(String subStagesCode) {
        if (!ValidationUtils.isNonNullOrEmpty(subStagesCode)) {
            return Collections.emptyList();
        }
        
        List<CodeValueResponse> codeValues = codeMasterService.getAllCodeValuesByCodeKey(subStagesCode, true);
        
        return codeValues;
    }
}
