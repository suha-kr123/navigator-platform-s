package com.nivasafinance.features.campaign.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.campaign.entity.CampaignConfig;
import com.nivasafinance.features.campaign.enums.CampaignConfigStatus;
import com.nivasafinance.features.campaign.exception.CampaignConfigExceptionFactory;
import com.nivasafinance.features.campaign.exception.CampaignConfigNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignConfigRepositoryWrapper {

    private final CampaignConfigRepository campaignConfigRepository;
    private final MessageSource messageSource;

    public CampaignConfig saveWithException(CampaignConfig campaignConfig) {
        try {
            return campaignConfigRepository.save(campaignConfig);
        } catch (DataAccessException e) {
            throw CampaignConfigExceptionFactory.saveFailed(messageSource);
        }
    }

    public CampaignConfig findByIdWithException(Long id) {
        try {
            return campaignConfigRepository.findById(id).orElseThrow(() ->
                    CampaignConfigExceptionFactory.notFoundById(id, messageSource));
        } catch (CampaignConfigNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            throw CampaignConfigExceptionFactory.retrieveByIdFailed(id, messageSource);
        }
    }
    
    public CampaignConfig findByIdentifierWithException(UUID identifier) {
        try {
            return campaignConfigRepository.findByIdentifier(identifier).orElseThrow(() ->
                    CampaignConfigExceptionFactory.notFoundByIdentifier(identifier, messageSource));
        } catch (CampaignConfigNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            throw CampaignConfigExceptionFactory.retrieveByIdentifierFailed(identifier, messageSource);
        }
    }

    public List<CampaignConfig> findByIdsWithException(List<Long> ids) {
        try {
            return campaignConfigRepository.findAllById(ids);
        } catch (DataAccessException e) {
            throw CampaignConfigExceptionFactory.retrieveByIdsFailed(ids, messageSource);
        }
    }

    public PaginatedResponse<CampaignConfig> findByStatusInWithException(
            List<CampaignConfigStatus> statuses, PaginationRequest paginationRequest) {
        try {
            int offset = paginationRequest.getOffset();
            int limit = paginationRequest.getLimit();
            String sortBy = "createdAt";
            String sortDirection = paginationRequest.getSortDirection() != null ? paginationRequest.getSortDirection() : "DESC";

            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(offset / limit, limit, sort);

            Page<CampaignConfig> page = campaignConfigRepository.findByStatusIn(statuses, pageable);
            long totalElements = page.getTotalElements();
            int totalPages = page.getTotalPages();
            int currentPage = page.getNumber();
            boolean hasNext = page.hasNext();
            boolean hasPrevious = page.hasPrevious();

            PaginationInfo paginationInfo = new PaginationInfo(
                    offset, limit, totalElements, totalPages, currentPage, hasNext, hasPrevious);

            return new PaginatedResponse<>(page.getContent(), paginationInfo);
        } catch (DataAccessException e) {
            throw CampaignConfigExceptionFactory.retrieveByIdsFailed(null, messageSource);
        }
    }
}

