package com.nivasafinance.features.leadtasks.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.common.utils.ValidationUtils;
import com.nivasafinance.features.leadtasks.entity.LeadTask;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LeadTaskRepositoryWrapper {

    private final LeadTaskRepository repository;
    
    public LeadTask save(LeadTask leadTask) {
        return repository.save(leadTask);
    }
    
    public List<LeadTask> findByLeadId(Long leadId) {
        return repository.findByLeadId(leadId);
    }
    
    public PaginatedResponse<LeadTask> findByLeadIdOrderByCreatedAtDesc(Long leadId, PaginationRequest paginationRequest) {
        int offset = paginationRequest.getOffset();
        int limit = paginationRequest.getLimit();
        String sortBy = paginationRequest.getSortBy() != null ? paginationRequest.getSortBy() : "createdAt";
        String sortDirection = paginationRequest.getSortDirection() != null ? paginationRequest.getSortDirection() : "DESC";
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(offset / limit, limit, sort);
        
        Page<LeadTask> page = repository.findByLeadIdOrderByCreatedAtDesc(leadId, pageable);
        long totalElements = page.getTotalElements();
        int totalPages = page.getTotalPages();
        int currentPage = page.getNumber();
        boolean hasNext = page.hasNext();
        boolean hasPrevious = page.hasPrevious();
        
        PaginationInfo paginationInfo = new PaginationInfo(
                offset, limit, totalElements, totalPages, currentPage, hasNext, hasPrevious);
        
        return new PaginatedResponse<>(page.getContent(), paginationInfo);
    }
    
    public List<LeadTask> findByTaskIds(List<Long> taskIds) {
        if (!ValidationUtils.isNonNull(taskIds) || taskIds.isEmpty()) {
            return Collections.emptyList();
        }
        return repository.findByTaskIdIn(taskIds);
    }

    public Optional<LeadTask> findByTaskId(Long taskId) {
        return repository.findByTaskId(taskId);
    }
}

