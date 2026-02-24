package com.nivasafinance.features.bre.service.impl;

import com.nivasafinance.features.document.dto.DocumentCreateRequest;
import com.nivasafinance.features.document.dto.DocumentCreateResponse;
import com.nivasafinance.features.document.service.DocumentWriteService;
import com.nivasafinance.features.bre.dto.BREConfigDetailedResponse;
import com.nivasafinance.features.bre.dto.BREConfigRuleFileUploadResponse;
import com.nivasafinance.features.bre.dto.CreateBREConfigRequest;
import com.nivasafinance.features.bre.entity.BREConfigs;
import com.nivasafinance.features.bre.repository.BREConfigRepositoryWrapper;
import com.nivasafinance.features.bre.service.BREConfigWriteService;
import com.nivasafinance.features.bre.utils.BREDocumentUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class BREConfigWriteServiceImpl implements BREConfigWriteService {

    private final BREConfigRepositoryWrapper breConfigRepositoryWrapper;
    private final DocumentWriteService documentWriteService;

    @Override
    public BREConfigDetailedResponse createBREConfig(CreateBREConfigRequest request) {
        BREConfigs.Configs configs = BREConfigs.Configs.builder()
                .provider(request.getProvider())
                .dataProviderId(request.getDataProviderId())
                .goRulesProviderDetails(null)
                .build();
        BREConfigs entity = BREConfigs.builder()
                .uname(request.getUname())
                .configs(configs)
                .build();
        BREConfigs saved = breConfigRepositoryWrapper.saveWithException(entity);
        return BREConfigDetailedResponse.from(saved, null);
    }

    @Override
    public BREConfigRuleFileUploadResponse uploadRuleFile(String uname, MultipartFile file) {
        BREConfigs config = breConfigRepositoryWrapper.findByUnameWithException(uname);
        BREConfigs.Configs configs = config.getConfigs();
        BREConfigs.GoRulesProviderDetails existingDetails = configs != null ? configs.getGoRulesProviderDetails() : null;
        if (existingDetails != null && existingDetails.getRuleJsonFileId() != null) {
            documentWriteService.deleteDocumentById(existingDetails.getRuleJsonFileId());
        }
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "rule.json";
        DocumentCreateRequest documentRequest = new DocumentCreateRequest(
                fileName,
                file,
                BREDocumentUtils.generateDocumentPathForRuleFile(config.getId(), fileName)
        );
        DocumentCreateResponse documentResponse = documentWriteService.createDocument(documentRequest);
        BREConfigs.GoRulesProviderDetails newDetails = new BREConfigs.GoRulesProviderDetails(documentResponse.getId());
        BREConfigs.Configs updatedConfigs = configs != null
                ? BREConfigs.Configs.builder()
                        .provider(configs.getProvider())
                        .dataProviderId(configs.getDataProviderId())
                        .goRulesProviderDetails(newDetails)
                        .build()
                : BREConfigs.Configs.builder()
                        .goRulesProviderDetails(newDetails)
                        .build();
        config.setConfigs(updatedConfigs);
        breConfigRepositoryWrapper.saveWithException(config);
        return BREConfigRuleFileUploadResponse.builder()
                .ruleJsonFileId(documentResponse.getId())
                .documentIdentifier(documentResponse.getIdentifier())
                .build();
    }

}
