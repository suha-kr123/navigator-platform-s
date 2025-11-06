package com.nivasafinance.features.master.codemaster.service;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;

import java.util.List;

public interface CodeValueMasterService {
    
    CodeValueResponse getByKey(String key);
    
    List<CodeValueResponse> getByKeys(List<String> keys);
    
    CodeValueResponse getCodeValueByKeyAndCodeKey(String key, String codeKey);
    
    List<CodeValueResponse> getCodeValueByKeysAndCodeKey(List<String> keys, String codeKey);
}

