package com.nivasafinance.features.master.codemaster.service;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CodeValueMasterService {

    CodeValueResponse getByKey(String key);

    CodeValueResponse getByKeyOrNull(String key);

    List<CodeValueResponse> getByKeys(List<String> keys);

    CodeValueResponse getCodeValueByKeyAndCodeKey(String key, String codeKey);

    List<CodeValueResponse> getCodeValueByKeysAndCodeKey(List<String> keys, String codeKey);

    MasterCodeValueResponse createMasterCodeValues(String masterCodeKey,
            List<MasterCodeValueRequest> masterCodeValueRequests);

    MasterCodeValueResponse enableDisableMasterCodeValue(String masterCodeKey, String masterCodeValueKey);

    CodeValueResponse uploadIcon(MasterCodeValueIconUploadRequest request, MultipartFile file);
}
