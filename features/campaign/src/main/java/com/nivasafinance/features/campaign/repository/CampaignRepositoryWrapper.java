package com.nivasafinance.features.campaign.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.campaign.entity.Campaign;
import com.nivasafinance.features.campaign.exception.CampaignExceptionFactory;
import com.nivasafinance.features.campaign.exception.CampaignNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampaignRepositoryWrapper {

    private final CampaignRepository campaignRepository;
    private final MessageSource messageSource;

    public Campaign saveWithException(Campaign campaign) {
        try {
            return campaignRepository.save(campaign);
        } catch (DataAccessException e) {
            throw CampaignExceptionFactory.saveFailed(messageSource);
        }
    }

    public Campaign findByIdWithException(Long id) {
        try {
            return campaignRepository.findById(id).orElseThrow(() ->
                    CampaignExceptionFactory.notFoundById(id, messageSource));
        } catch (CampaignNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            throw CampaignExceptionFactory.retrieveByIdFailed(id, messageSource);
        }
    }
    
    public Campaign findByIdentifierWithException(UUID identifier) {
        try {
            return campaignRepository.findByIdentifier(identifier).orElseThrow(() ->
                    CampaignExceptionFactory.notFoundByIdentifier(identifier, messageSource));
        } catch (CampaignNotFoundException e) {
            throw e;
        } catch (DataAccessException e) {
            throw CampaignExceptionFactory.retrieveByIdentifierFailed(identifier, messageSource);
        }
    }

    public boolean existsByName(String name) {
        return campaignRepository.existsByName(name);
    }

    public PaginatedResponse<Campaign> findAllWithException(PaginationRequest paginationRequest) {
        try {
            int offset = paginationRequest.getOffset();
            int limit = paginationRequest.getLimit();
            String sortBy = "createdAt";
            String sortDirection = paginationRequest.getSortDirection() != null ? paginationRequest.getSortDirection() : "DESC";

            Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            Pageable pageable = PageRequest.of(offset / limit, limit, sort);

            Page<Campaign> page = campaignRepository.findAll(pageable);
            long totalElements = page.getTotalElements();
            int totalPages = page.getTotalPages();
            int currentPage = page.getNumber();
            boolean hasNext = page.hasNext();
            boolean hasPrevious = page.hasPrevious();

            PaginationInfo paginationInfo = new PaginationInfo(
                    offset, limit, totalElements, totalPages, currentPage, hasNext, hasPrevious);

            return new PaginatedResponse<>(page.getContent(), paginationInfo);
        } catch (DataAccessException e) {
            throw CampaignExceptionFactory.retrieveByIdsFailed(null, messageSource);
        }
    }
}

