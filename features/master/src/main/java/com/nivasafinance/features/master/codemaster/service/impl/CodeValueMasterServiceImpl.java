package com.nivasafinance.features.master.codemaster.service.impl;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeValueRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CodeValueMasterServiceImpl implements CodeValueMasterService {
    
    private final MasterCodeValueRepositoryWrapper masterCodeValueRepositoryWrapper;
    
    @Override
    @org.springframework.cache.annotation.Cacheable(cacheNames = "codeValues", key = "#key")
    public CodeValueResponse getByKey(String key) {
        return CodeValueResponse.from(masterCodeValueRepositoryWrapper.findByKeyWithException(key));
    }
    
    @Override
    public List<CodeValueResponse> getByKeys(List<String> keys) {
        return keys.stream()
                .map(key -> CodeValueResponse.from(masterCodeValueRepositoryWrapper.findByKeyWithException(key)))
                .collect(Collectors.toList());
    }
    
    @Override
    public CodeValueResponse getCodeValueByKeyAndCodeKey(String key, String codeKey) {
        return CodeValueResponse.from(masterCodeValueRepositoryWrapper.findByKeyAndCodeKeyWithException(key, codeKey));
    }
    
    @Override
    public List<CodeValueResponse> getCodeValueByKeysAndCodeKey(List<String> keys, String codeKey) {
        return keys.stream()
                .map(singleKey -> CodeValueResponse.from(
                        masterCodeValueRepositoryWrapper.findByKeyAndCodeKeyWithException(singleKey, codeKey)))
                .collect(Collectors.toList());
    }
}

