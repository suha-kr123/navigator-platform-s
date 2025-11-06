package com.nivasafinance.features.leadstages.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.leadstages.entity.LeadStageHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeadStageHistoryRepositoryWrapper {

    private final LeadStageHistoryRepository repository;

    public LeadStageHistory save(LeadStageHistory leadStageHistory) {
        return repository.save(leadStageHistory);
    }

    public Optional<LeadStageHistory> findLatestEntry(Long leadId) {
        return repository.findFirstByLeadIdOrderByEnteredAtDesc(leadId);
    }

    public PaginatedResponse<LeadStageHistory> findByLeadIdOrderByEnteredAtDesc(Long leadId, PaginationRequest paginationRequest) {
        int offset = paginationRequest.getOffset();
        int limit = paginationRequest.getLimit();
        String sortBy = paginationRequest.getSortBy() != null ? paginationRequest.getSortBy() : "enteredAt";
        String sortDirection = paginationRequest.getSortDirection() != null ? paginationRequest.getSortDirection() : "DESC";
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<LeadStageHistory> page = repository.findByLeadIdOrderByEnteredAtDesc(leadId, pageable);
        long totalElements = page.getTotalElements();
        int totalPages = page.getTotalPages();
        int currentPage = page.getNumber();
        boolean hasNext = page.hasNext();
        boolean hasPrevious = page.hasPrevious();
        
        PaginationInfo paginationInfo = new PaginationInfo(
                offset, limit, totalElements, totalPages, currentPage, hasNext, hasPrevious);
        
        return new PaginatedResponse<>(page.getContent(), paginationInfo);
    }
}

