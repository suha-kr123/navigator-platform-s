package com.nivasafinance.features.master.codemaster.service;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeTreeResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeSearchMultiSectionResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesRequest;
import com.nivasafinance.features.master.codemaster.dto.SearchContext;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse;

import java.util.List;

public interface CodeMasterService {

    List<CodeValueResponse> getAllCodeValuesByCodeKey(String codeKey, Boolean onlyActive);

    PaginatedResponse<CodeValueResponse> getCodeValuesByCodeKeyPaginated(
            String codeKey, Boolean onlyActive, PaginationRequest paginationRequest);

    List<MasterCodeWithValuesResponse> getMasterCodeChildrenWithValues(
            String parentCodeKey,
            Boolean onlyActive
    );

    PaginatedResponse<MasterCodeWithValuesResponse> getMasterCodeChildrenWithValuesPaginated(
            String parentCodeKey, Boolean onlyActive, PaginationRequest paginationRequest);

    MasterCodeResponse getMasterCodeByKey(String key);
    List<MasterCodeResponse> getMasterCodesByKeys(List<String> keys);

    PaginatedResponse<MasterCodeResponse> getAllMasterCodes(PaginationRequest paginationRequest);

    MasterCodeValueResponse updateMasterCodeWithValues(String masterCodeKey, MasterCodeWithValuesRequest masterCodeWithValuesRequest);

    List<MasterCodeTreeResponse> getMasterCodeTree(String parentCodeKey);

    List<MasterCodeTreeResponse> addChildToTree(String parentCodeKey, MasterCodeWithValuesRequest child);

    MasterCodeSearchMultiSectionResponse searchMasterCodes(String searchTerm, List<SearchContext> searchContexts,
            String codeKey, PaginationRequest paginationRequest);
}

