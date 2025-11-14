package com.nivasafinance.features.master.codemaster.service;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse;
import com.nivasafinance.features.master.codemaster.entity.MasterCode;

import java.util.List;

public interface CodeMasterService {
    
    List<CodeValueResponse> getAllCodeValuesByCodeKey(String codeKey, Boolean onlyActive);
    
    List<MasterCodeWithValuesResponse> getMasterCodeChildrenWithValues(
            String parentCodeKey,
            Boolean onlyActive
    );

    MasterCodeResponse getMasterCodeByKey(String key);
    List<MasterCodeResponse> getMasterCodesByKeys(List<String> keys);
}

