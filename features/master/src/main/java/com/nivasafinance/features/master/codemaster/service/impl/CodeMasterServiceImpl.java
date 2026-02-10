package com.nivasafinance.features.master.codemaster.service.impl;

import com.nivasafinance.common.base.model.MasterLanguageData;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeWithValuesResponse;
import com.nivasafinance.features.master.codemaster.entity.MasterCode;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeValueRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
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
			Boolean onlyActive) {
		com.nivasafinance.features.master.codemaster.entity.MasterCode parentMasterCode = masterCodeRepositoryWrapper
				.findByKeyWithException(parentCodeKey);

		if (parentMasterCode.getId() == null) {
			throw new IllegalStateException("Parent master code ID is null");
		}

		List<com.nivasafinance.features.master.codemaster.entity.MasterCode> children = masterCodeRepositoryWrapper
				.findByParentIdWithException(parentMasterCode.getId());

		return children.stream()
				.map(child -> {
					List<MasterCodeValue> childValues = Boolean.TRUE.equals(onlyActive)
							? masterCodeValueRepositoryWrapper.findByCodeKeyAndIsActiveTrueWithException(child.getKey())
							: masterCodeValueRepositoryWrapper.findByCodeKeyWithException(child.getKey());

					return MasterCodeWithValuesResponse.builder()
							.id(child.getId())
							.key(child.getKey())
							.name(child.getName() != null && child.getName().getDefaultValue() != null
									? child.getName().getDefaultValue()
									: "")
							.description(
									child.getDescription() != null && child.getDescription().getDefaultValue() != null
											? child.getDescription().getDefaultValue()
											: "")
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

	@Override
	public PaginatedResponse<MasterCodeResponse> getAllMasterCodes(PaginationRequest paginationRequest) {
		PaginatedResponse<MasterCode> paginatedMasterCodes = masterCodeRepositoryWrapper
				.findAllWithException(paginationRequest);
		return new PaginatedResponse<>(
				paginatedMasterCodes.getContent().stream()
						.map(MasterCodeResponse::from)
						.collect(Collectors.toList()),
				paginatedMasterCodes.getPagination());
	}

	@Override
	@Transactional
	public MasterCodeValueResponse updateMasterCodeWithValues(
			String masterCodeKey,
			MasterCodeWithValuesRequest request) {

		MasterCode masterCode = masterCodeRepositoryWrapper.findByKeyWithException(masterCodeKey);

		applyMasterCodePatch(masterCode, request);
		masterCodeRepositoryWrapper.saveWithException(masterCode);

		applyMasterCodeValuePatch(request);

		List<MasterCodeValue> masterCodeValues = masterCodeValueRepositoryWrapper.findByCodeKeyWithException(masterCodeKey);
		return MasterCodeValueResponse.from(masterCode, masterCodeValues);
	}

	private void applyMasterCodePatch(
			MasterCode masterCode,
			MasterCodeWithValuesRequest request) {

		if (hasDefaultValue(request.getNameMap())) {
			masterCode.setName(
					buildLanguageData(request.getNameMap()));
		}

		if (hasDefaultValue(request.getDescriptionMap())) {
			masterCode.setDescription(
					buildLanguageData(request.getDescriptionMap()));
		}

		if (request.getParentId() != null) {
			masterCode.setParentId(request.getParentId());
		}
	}

	private void applyMasterCodeValuePatch(
			MasterCodeWithValuesRequest request) {

		if (request.getMasterCodeValueRequests() == null ||
				request.getMasterCodeValueRequests().isEmpty()) {
			return;
		}

		for (MasterCodeValueRequest valueRequest : request.getMasterCodeValueRequests()) {

			MasterCodeValue masterCodeValue = masterCodeValueRepositoryWrapper
					.findByKeyWithException(valueRequest.getKey());

			if (hasDefaultValue(valueRequest.getValueMap())) {
				masterCodeValue.setValue(
						buildLanguageData(valueRequest.getValueMap()));
			}

			if (hasDefaultValue(valueRequest.getDescriptionMap())) {
				masterCodeValue.setDescription(
						buildLanguageData(valueRequest.getDescriptionMap()));
			}

			masterCodeValueRepositoryWrapper.saveWithException(masterCodeValue);
		}
	}

	private MasterLanguageData buildLanguageData(Map<String, String> map) {
		return MasterLanguageData.builder()
				.defaultValue(map.get("default"))
				.build();
	}

	private boolean hasDefaultValue(Map<String, String> map) {
		return map != null && map.get("default") != null;
	}

}