package com.nivasafinance.features.bre.service.impl;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.document.dto.DocumentResponse;
import com.nivasafinance.features.document.service.DocumentReadService;
import com.nivasafinance.features.bre.dto.BREConfigDetailedResponse;
import com.nivasafinance.features.bre.dto.BREConfigResponse;
import com.nivasafinance.features.bre.entity.BREConfigs;
import com.nivasafinance.features.bre.repository.BREConfigRepositoryWrapper;
import com.nivasafinance.features.bre.service.BREConfigReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BREConfigReadServiceImpl implements BREConfigReadService {

    private final BREConfigRepositoryWrapper breConfigRepositoryWrapper;
    private final DocumentReadService documentReadService;

    @Override
    public PaginatedResponse<BREConfigResponse> getAllBREConfigs(PaginationRequest paginationRequest) {
        PaginatedResponse<BREConfigs> paginatedEntities =
                breConfigRepositoryWrapper.findAllWithException(paginationRequest);
        List<BREConfigResponse> responses = paginatedEntities.getContent().stream()
                .map(BREConfigResponse::from)
                .collect(Collectors.toList());
        return new PaginatedResponse<>(responses, paginatedEntities.getPagination());
    }

    @Override
    public BREConfigDetailedResponse getBREConfigByUname(String uname) {
        BREConfigs entity = breConfigRepositoryWrapper.findByUnameWithException(uname);
        DocumentResponse ruleFileDocument = null;
        if (entity.getConfigs() != null
                && entity.getConfigs().getGoRulesProviderDetails() != null
                && entity.getConfigs().getGoRulesProviderDetails().getRuleJsonFileId() != null) {
            ruleFileDocument = documentReadService.getDocumentById(
                    entity.getConfigs().getGoRulesProviderDetails().getRuleJsonFileId());
        }
        return BREConfigDetailedResponse.from(entity, ruleFileDocument);
    }
}
