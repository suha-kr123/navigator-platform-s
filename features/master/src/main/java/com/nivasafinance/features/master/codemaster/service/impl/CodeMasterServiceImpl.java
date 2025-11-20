package com.nivasafinance.features.master.codemaster.service.impl;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeValueRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CodeMasterServiceImpl implements CodeMasterService {

    private final MasterCodeRepositoryWrapper masterCodeRepositoryWrapper;
    private final MasterCodeValueRepositoryWrapper masterCodeValueRepositoryWrapper;

    @Override
    public List<CodeValueResponse> getAllCodeValuesByCodeKey(String codeKey, Boolean onlyActive) {
        masterCodeRepositoryWrapper.findByKeyWithException(codeKey);

        List<MasterCodeValue> codeValues = Boolean.TRUE.equals(onlyActive)
                ? masterCodeValueRepositoryWrapper.findByCodeKeyAndIsActiveTrueWithException(codeKey)
                : masterCodeValueRepositoryWrapper.findByCodeKeyWithException(codeKey);

        return codeValues.stream()
                .map(CodeValueResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public List<MasterCodeWithValuesResponse> getMasterCodeChildrenWithValues(
            String parentCodeKey,
            Boolean onlyActive
    ) {
        com.nivasafinance.features.master.codemaster.entity.MasterCode parentMasterCode =
                masterCodeRepositoryWrapper.findByKeyWithException(parentCodeKey);

        if (parentMasterCode.getId() == null) {
            throw new IllegalStateException("Parent master code ID is null");
        }

        List<com.nivasafinance.features.master.codemaster.entity.MasterCode> children =
                masterCodeRepositoryWrapper.findByParentIdWithException(parentMasterCode.getId());

        return children.stream()
                .map(child -> {
                    List<MasterCodeValue> childValues = Boolean.TRUE.equals(onlyActive)
                            ? masterCodeValueRepositoryWrapper.findByCodeKeyAndIsActiveTrueWithException(child.getKey())
                            : masterCodeValueRepositoryWrapper.findByCodeKeyWithException(child.getKey());

                    return MasterCodeWithValuesResponse.builder()
                            .id(child.getId())
                            .key(child.getKey())
                            .name(child.getName() != null && child.getName().getDefaultValue() != null
                                    ? child.getName().getDefaultValue() : "")
                            .description(child.getDescription() != null && child.getDescription().getDefaultValue() != null
                                    ? child.getDescription().getDefaultValue() : "")
                            .isSystemDefined(child.getIsSystemDefined())
                            .parentId(child.getParentId())
                            .values(childValues.stream()
                                    .map(CodeValueResponse::from)
                                    .collect(Collectors.toList()))
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public MasterCodeResponse getMasterCodeByKey(String key) {
        return MasterCodeResponse.from(masterCodeRepositoryWrapper.findByKeyWithException(key));
    }

    @Override
    public List<MasterCodeResponse> getMasterCodesByKeys(List<String> keys) {
        return keys.stream()
                .map(singleKey -> MasterCodeResponse.from(
                        masterCodeRepositoryWrapper.findByKeyWithException(singleKey)))
                .collect(Collectors.toList());
    }
}

