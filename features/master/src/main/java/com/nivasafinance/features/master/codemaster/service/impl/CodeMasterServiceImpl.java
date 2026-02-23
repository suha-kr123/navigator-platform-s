package com.nivasafinance.features.master.codemaster.service.impl;

import com.nivasafinance.common.base.model.MasterLanguageData;
import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.master.codemaster.dto.*;
import com.nivasafinance.features.master.codemaster.entity.MasterCode;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeValueRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.service.CodeMasterService;
import com.nivasafinance.features.master.codemaster.utils.MasterCodeKeyUtil;

import com.nivasafinance.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CodeMasterServiceImpl implements CodeMasterService {

	public static final String MASTER_CODE_VALUE_KEY_SUFFIX = "MASTER_CODE_VALUE";
	public static final String MASTER_CODE_KEY_SUFFIX = "MASTER_CODE";

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
	public PaginatedResponse<CodeValueResponse> getCodeValuesByCodeKeyPaginated(
			String codeKey, Boolean onlyActive, PaginationRequest paginationRequest) {
		masterCodeRepositoryWrapper.findByKeyWithException(codeKey);
		PaginatedResponse<MasterCodeValue> paginated = masterCodeValueRepositoryWrapper
				.findByCodeKeyWithException(codeKey, onlyActive, paginationRequest);
		return new PaginatedResponse<>(
				paginated.getContent().stream().map(CodeValueResponse::from).collect(Collectors.toList()),
				paginated.getPagination());
	}

	@Override
	public List<MasterCodeWithValuesResponse> getMasterCodeChildrenWithValues(
			String parentCodeKey,
			Boolean onlyActive) {
		MasterCode parentMasterCode = masterCodeRepositoryWrapper.findByKeyWithException(parentCodeKey);

		if (parentMasterCode.getId() == null) {
			throw new IllegalStateException("Parent master code ID is null");
		}

		List<MasterCode> children = masterCodeRepositoryWrapper
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
	public PaginatedResponse<MasterCodeWithValuesResponse> getMasterCodeChildrenWithValuesPaginated(
			String parentCodeKey, Boolean onlyActive, PaginationRequest paginationRequest) {
		MasterCode parentMasterCode = masterCodeRepositoryWrapper.findByKeyWithException(parentCodeKey);
		if (parentMasterCode.getId() == null) {
			throw new IllegalStateException("Parent master code ID is null");
		}
		PaginatedResponse<MasterCode> paginatedChildren = masterCodeRepositoryWrapper
				.findByParentIdWithException(parentMasterCode.getId(), paginationRequest);
		List<MasterCodeWithValuesResponse> content = paginatedChildren.getContent().stream()
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
		return new PaginatedResponse<>(content, paginatedChildren.getPagination());
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

	@Override
	public List<MasterCodeTreeResponse> getMasterCodeTree(String parentCodeKey) {
		MasterCode parentMasterCode = masterCodeRepositoryWrapper.findByKeyWithException(parentCodeKey);
		return List.of(buildTree(parentMasterCode));
	}

	private MasterCodeTreeResponse buildTree(MasterCode node) {
		List<MasterCode> children = masterCodeRepositoryWrapper.findByParentIdWithException(node.getId());
		List<MasterCodeTreeResponse> childTree = children.stream()
				.map(this::buildTree)
				.collect(Collectors.toList());

		return MasterCodeTreeResponse.from(node, childTree);
	}

	@Override
	@Transactional
	public List<MasterCodeTreeResponse> addChildToTree(String parentCodeKey, MasterCodeWithValuesRequest child) {
		MasterCode parentMasterCode = masterCodeRepositoryWrapper.findByKeyWithException(parentCodeKey);

		Set<String> existingKeys = masterCodeRepositoryWrapper.findAllWithException()
				.stream()
				.map(MasterCode::getKey)
				.collect(Collectors.toSet());

		String codeKey = MasterCodeKeyUtil.generateUniqueKey(parentCodeKey, MASTER_CODE_KEY_SUFFIX, existingKeys);

		MasterCode childMasterCode = child.toEntity(codeKey);
		childMasterCode.setParentId(parentMasterCode.getId());
		masterCodeRepositoryWrapper.saveWithException(childMasterCode);
		if (child.getMasterCodeValueRequests() != null) {
			for (MasterCodeValueRequest valueRequest : child.getMasterCodeValueRequests()) {
				String valueKey = MasterCodeKeyUtil.generateUniqueKey(codeKey, MASTER_CODE_VALUE_KEY_SUFFIX,
						existingKeys);
				MasterCodeValue masterCodeValue = valueRequest.toEntity(valueKey, codeKey);
				masterCodeValueRepositoryWrapper.saveWithException(masterCodeValue);
				existingKeys.add(valueKey);
			}
		}

		return getMasterCodeTree(parentCodeKey);
	}

	@Override
	public MasterCodeSearchMultiSectionResponse searchMasterCodes(String searchTerm,
																  List<SearchContext> searchContexts, String codeKey, PaginationRequest paginationRequest) {
		String normalizedTerm = normalizeSearchTerm(searchTerm);
		List<SearchContext> distinctContexts = searchContexts.stream().distinct().toList();

		if (distinctContexts.contains(SearchContext.VALUE) && !StringUtils.hasText(codeKey)) {
			throw new BadRequestException("codeKey is required when searching values");
		}

		var builder = MasterCodeSearchMultiSectionResponse.builder();

		for (SearchContext context : distinctContexts) {
			switch (context) {
				case MASTER ->
						builder.masterMatches(masterCodeRepositoryWrapper.searchMasterCodesOnly(normalizedTerm, paginationRequest));
				case CHILD ->
						builder.childMatches(masterCodeRepositoryWrapper.searchChildCodesOnly(normalizedTerm, paginationRequest));
				case VALUE ->
						builder.valueMatches(masterCodeValueRepositoryWrapper.searchMasterCodeValues(normalizedTerm, codeKey, paginationRequest));
			}
		}

		return builder.build();
	}

	private String normalizeSearchTerm(String searchTerm) {
		if (!StringUtils.hasText(searchTerm)) {
			throw new BadRequestException("Search term is required");
		}
		String trimmed = searchTerm.trim();
		if (trimmed.length() < MasterCodeSearchRequest.MIN_SEARCH_TERM_LENGTH) {
			throw new BadRequestException("Search term must be at least "
					+ MasterCodeSearchRequest.MIN_SEARCH_TERM_LENGTH + " characters long");
		}
		return trimmed;
	}

}