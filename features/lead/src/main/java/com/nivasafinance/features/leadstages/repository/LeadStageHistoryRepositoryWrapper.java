package com.nivasafinance.features.leadstages.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.leadstages.dto.LeadStageHistoryDisplayResponse;
import com.nivasafinance.features.leadstages.entity.LeadStageHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeadStageHistoryRepositoryWrapper {

    private static final String SQL_STAGE_HISTORY_WITH_DISPLAY_LABELS = """
            SELECT sc.stage_config->>'externalDisplayName' AS display_label,
                   h.entered_at,
                   h.exited_at
            FROM n_lead_stage_history h
            JOIN n_stage_config sc ON sc."key" = h.stage_key AND sc.is_active = true
              AND sc.stage_config->>'externalDisplayName' IS NOT NULL
              AND TRIM(sc.stage_config->>'externalDisplayName') <> ''
            WHERE h.lead_id = ?
            ORDER BY h.entered_at ASC
            """;

    private static final RowMapper<LeadStageHistoryDisplayResponse> DISPLAY_ROW_MAPPER = new RowMapper<>() {
        @Override
        public LeadStageHistoryDisplayResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
            java.time.LocalDateTime enteredAt = rs.getTimestamp("entered_at") != null
                    ? rs.getTimestamp("entered_at").toLocalDateTime() : null;
            java.time.LocalDateTime exitedAt = rs.getTimestamp("exited_at") != null
                    ? rs.getTimestamp("exited_at").toLocalDateTime() : null;
            return LeadStageHistoryDisplayResponse.builder()
                    .displayLabel(rs.getString("display_label"))
                    .enteredAt(enteredAt)
                    .exitedAt(exitedAt)
                    .build();
        }
    };

    private final LeadStageHistoryRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public LeadStageHistory save(LeadStageHistory leadStageHistory) {
        return repository.save(leadStageHistory);
    }

    public Optional<LeadStageHistory> findLatestEntry(Long leadId) {
        return repository.findFirstByLeadIdOrderByEnteredAtDesc(leadId);
    }

    public PaginatedResponse<LeadStageHistory> findByLeadIdOrderByEnteredAtDesc(Long leadId, PaginationRequest paginationRequest) {
        int offset = paginationRequest.getOffset();
        int limit = paginationRequest.getLimit();
        String sortBy = paginationRequest.getSortBy();
        
        // Map common field names from snake_case to camelCase (entity property names)
        if ("created_at".equals(sortBy)) {
            sortBy = "createdAt";
        } else if ("entered_at".equals(sortBy)) {
            sortBy = "enteredAt";
        } else if ("exited_at".equals(sortBy)) {
            sortBy = "exitedAt";
        } else if ("stage_from".equals(sortBy)) {
            sortBy = "stageFrom";
        } else if ("stage_key".equals(sortBy)) {
            sortBy = "stageKey";
        } else if ("sub_stage_key".equals(sortBy)) {
            sortBy = "subStageKey";
        } else if ("moved_by".equals(sortBy)) {
            sortBy = "movedBy";
        } else if (sortBy == null || sortBy.isEmpty()) {
            // Default to enteredAt for stage history (more appropriate than createdAt)
            sortBy = "enteredAt";
        }
        
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

    public List<LeadStageHistory> findAllByLeadId(Long leadId) {
        return repository.findByLeadIdOrderByEnteredAtDesc(leadId);
    }

    /**
     * Returns stage history for the lead with external display labels from n_stage_config.
     */
    public List<LeadStageHistoryDisplayResponse> findStageHistoryWithDisplayLabelsByLeadId(Long leadId) {
        return jdbcTemplate.query(SQL_STAGE_HISTORY_WITH_DISPLAY_LABELS, DISPLAY_ROW_MAPPER, leadId);
    }
}

