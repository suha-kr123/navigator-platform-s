package com.nivasafinance.features.master.codemaster.service;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse;

import java.util.List;

public interface CodeMasterService {
    
    List<CodeValueResponse> getAllCodeValuesByCodeKey(String codeKey, Boolean onlyActive);
    
    List<MasterCodeWithValuesResponse> getMasterCodeChildrenWithValues(
            String parentCodeKey,
            Boolean onlyActive
    );
}

