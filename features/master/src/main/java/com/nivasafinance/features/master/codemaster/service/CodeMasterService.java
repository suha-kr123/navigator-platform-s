package com.nivasafinance.features.master.codemaster.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse;

import java.util.List;

public interface CodeMasterService {
    
    List<CodeValueResponse> getAllCodeValuesByCodeKey(String codeKey, Boolean onlyActive);
    
    List<MasterCodeWithValuesResponse> getMasterCodeChildrenWithValues(
            String parentCodeKey,
            Boolean onlyActive
    );

    MasterCodeResponse getMasterCodeByKey(String key);
    List<MasterCodeResponse> getMasterCodesByKeys(List<String> keys);

    PaginatedResponse<MasterCodeResponse> getAllMasterCodes(PaginationRequest paginationRequest);

    MasterCodeValueResponse updateMasterCodeWithValues(String masterCodeKey, MasterCodeWithValuesRequest masterCodeWithValuesRequest);
}

