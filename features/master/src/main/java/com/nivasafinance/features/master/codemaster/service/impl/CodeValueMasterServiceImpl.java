package com.nivasafinance.features.master.codemaster.service.impl;

import com.nivasafinance.features.document.dto.DocumentCreateRequestInputStream;
import com.nivasafinance.features.document.service.DocumentWriteService;
import com.nivasafinance.features.master.codemaster.dto.CodeValueResponse;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueIconUploadRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueRequest;
import com.nivasafinance.features.master.codemaster.dto.MasterCodeValueResponse;
import com.nivasafinance.features.master.codemaster.entity.MasterCode;
import com.nivasafinance.features.master.codemaster.entity.MasterCodeValue;
import com.nivasafinance.features.master.codemaster.enums.IconContext;
import com.nivasafinance.features.master.codemaster.enums.IconSizeType;
import com.nivasafinance.features.master.codemaster.exception.CodeMasterExceptionFactory;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.repository.MasterCodeValueRepositoryWrapper;
import com.nivasafinance.features.master.codemaster.service.CodeValueMasterService;
import com.nivasafinance.features.master.codemaster.utils.MasterCodeKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CodeValueMasterServiceImpl implements CodeValueMasterService {

    private static final Pattern SAFE_KEY_PATTERN = Pattern.compile("[^a-zA-Z0-9_-]");
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/png", "image/jpeg", "image/gif", "image/svg+xml");

    public static final String MASTER_CODE_VALUE_KEY_SUFFIX = "MASTER_CODE_VALUE";
    public static final String DEFAULT_NAME_KEY = "default";

    private final MasterCodeValueRepositoryWrapper masterCodeValueRepositoryWrapper;
    private final MasterCodeRepositoryWrapper masterCodeRepositoryWrapper;
    private final DocumentWriteService documentWriteService;
    private final MessageSource messageSource;

    @Value("${master.icons.base-url:}")
    private String iconBaseUrl;
    
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

	@Override
	@Transactional
	public CodeValueResponse uploadIcon(MasterCodeValueIconUploadRequest request, MultipartFile file) {
		validateIconFile(file);
		if (iconBaseUrl == null || iconBaseUrl.isBlank()) {
			throw new CodeMasterExceptionFactory(messageSource).updateFailed(messageSource);
		}

		MasterCodeValue masterCodeValue = masterCodeValueRepositoryWrapper.findByKeyAndCodeKeyWithException(
				request.getMasterCodeValueKey(), request.getMasterCodeKey());
		IconContext iconContext = IconContext.from(request.getContext());
		IconSizeType sizeVariant = IconSizeType.from(request.getSize());

		MasterCodeValue.IconsData iconsData = masterCodeValue.getIcons() != null ? masterCodeValue.getIcons() : new MasterCodeValue.IconsData();
		MasterCodeValue.IconSizeData existingSizeData = iconContext.getIconSizeDataFrom(iconsData);
		Long existingDocumentId = sizeVariant.getDocumentIdFrom(existingSizeData);
		if (existingDocumentId != null) {
			documentWriteService.deleteDocumentById(existingDocumentId);
		}

		String sanitizedKey = SAFE_KEY_PATTERN.matcher(request.getMasterCodeValueKey()).replaceAll("_").toLowerCase();
		String contextLower = request.getContext() != null ? request.getContext().toLowerCase() : "default";
		String sizeLower = request.getSize() != null ? request.getSize().toLowerCase() : "medium";
		String ext = getFileExtension(file);
		String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
		long sizeBytes = file.getSize() >= 0 ? file.getSize() : 0;
		long timestamp = System.currentTimeMillis();
		String path = "assets/icons/" + sanitizedKey + "/" + contextLower + "/" + sizeLower + "-" + timestamp + "." + ext;
		String documentName = "icon_" + sanitizedKey + "_" + contextLower + "_" + sizeLower;

		DocumentCreateRequestInputStream createRequest = DocumentCreateRequestInputStream.builder()
				.name(documentName)
				.file(getInputStream(file))
				.customPath(path)
				.contentType(contentType)
				.size(sizeBytes)
				.build();
		com.nivasafinance.features.document.dto.DocumentCreateResponse response = documentWriteService.createDocument(createRequest);

		String base = iconBaseUrl.replaceAll("/$", "");
		String urlPath = path.startsWith("assets/") ? path.substring(7) : path;
		String fullUrl = base + "/" + urlPath;

		setIconUrlForContextAndSize(iconsData, iconContext, sizeVariant, fullUrl, response.getId());
		masterCodeValue.setIcons(iconsData);
		masterCodeValueRepositoryWrapper.saveWithException(masterCodeValue);
		return CodeValueResponse.from(masterCodeValue);
	}

	private InputStream getInputStream(MultipartFile file) {
		try {
			return file.getInputStream();
		} catch (IOException e) {
			throw new CodeMasterExceptionFactory(messageSource).updateFailed(messageSource);
		}
	}

	private void validateIconFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new CodeMasterExceptionFactory(messageSource).updateFailed(messageSource);
		}
		String contentType = file.getContentType();
		if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
			throw new CodeMasterExceptionFactory(messageSource).updateFailed(messageSource);
		}
	}

	private String getFileExtension(MultipartFile file) {
		String name = file.getOriginalFilename();
		if (name == null || !name.contains(".")) {
			return "png";
		}
		return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
	}

	private void setIconUrlForContextAndSize(MasterCodeValue.IconsData iconsData, IconContext context,
			IconSizeType size, String url, Long documentId) {
		MasterCodeValue.IconSizeData sizeData = context.getIconSizeDataFrom(iconsData);
		if (sizeData == null) {
			sizeData = new MasterCodeValue.IconSizeData();
			context.setIconSizeDataOn(iconsData, sizeData);
		}
		size.setUrlAndDocumentIdOn(sizeData, url, documentId);
	}
}
