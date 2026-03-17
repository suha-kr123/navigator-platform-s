package com.nivasafinance.features.master.codemaster.service.impl;

import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueResponse;
import com.nivasafinance.features.master.codemaster.entity.MasterCode;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeValueRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.master.codemaster.utils.MasterCodeKeyUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CodeValueMasterServiceImpl implements CodeValueMasterService {


    public static final String MASTER_CODE_VALUE_KEY_SUFFIX = "MASTER_CODE_VALUE";
    public static final String DEFAULT_NAME_KEY = "default";

    private final MasterCodeValueRepositoryWrapper masterCodeValueRepositoryWrapper;
    private final MasterCodeRepositoryWrapper masterCodeRepositoryWrapper;
    
    @Override
    public CodeValueResponse getByKey(String key) {
        return CodeValueResponse.from(masterCodeValueRepositoryWrapper.findByKeyWithException(key));
    }

    @Override
    public CodeValueResponse getByKeyOrNull(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        return masterCodeValueRepositoryWrapper.findByKey(key)
                .map(CodeValueResponse::from)
                .orElse(null);
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

    @Override
	@Transactional
	public MasterCodeValueResponse createMasterCodeValues(
			String masterCodeKey,
			List<MasterCodeValueRequest> masterCodeValueRequests) {

		MasterCode masterCode = masterCodeRepositoryWrapper.findByKeyWithException(masterCodeKey);

		Set<String> existingMasterCodeValueKeys = new HashSet<>(
				masterCodeValueRepositoryWrapper.findAllKeysWithException());

		List<MasterCodeValue> masterCodeValues = buildMasterCodeValues(
				masterCodeValueRequests,
				existingMasterCodeValueKeys, masterCodeKey);

	List<MasterCodeValue> savedMasterCodeValues = masterCodeValueRepositoryWrapper
				.saveAllWithException(masterCodeValues);

		return MasterCodeValueResponse.from(masterCode, savedMasterCodeValues);
	}

	private List<MasterCodeValue> buildMasterCodeValues(
			List<MasterCodeValueRequest> requests,
			Set<String> existingKeys, String masterCodeKey) {
		List<MasterCodeValue> values = new ArrayList<>();

		for (MasterCodeValueRequest request : requests) {
			String valueKey = MasterCodeKeyUtil.generateUniqueKey(
					request.getValueMap().get(DEFAULT_NAME_KEY),
					MASTER_CODE_VALUE_KEY_SUFFIX,
					existingKeys);

			existingKeys.add(valueKey);
			values.add(request.toEntity(valueKey, masterCodeKey));
		}

		return values;
	}

	@Override
	@Transactional
	public MasterCodeValueResponse enableDisableMasterCodeValue(String masterCodeKey, String masterCodeValueKey) {
		MasterCode masterCode = masterCodeRepositoryWrapper.findByKeyWithException(masterCodeKey);
		MasterCodeValue masterCodeValue = masterCodeValueRepositoryWrapper
				.findByKeyWithException(masterCodeValueKey);
		masterCodeValue.setIsActive(!masterCodeValue.getIsActive());
		masterCodeValueRepositoryWrapper.saveWithException(masterCodeValue);
		List<MasterCodeValue> masterCodeValues = masterCodeValueRepositoryWrapper.findByCodeKeyWithException(masterCodeKey);
		return MasterCodeValueResponse.from(masterCode, masterCodeValues);
	}

}
