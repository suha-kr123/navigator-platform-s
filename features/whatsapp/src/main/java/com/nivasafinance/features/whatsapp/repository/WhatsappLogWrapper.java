package com.nivasafinance.features.whatsapp.repository;

import com.nivasafinance.features.whatsapp.dto.WhatsappLogFilters;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@AllArgsConstructor
public class WhatsappLogWrapper {

    private static final Logger logger = LoggerFactory.getLogger(WhatsappLogWrapper.class);

    private final JdbcTemplate jdbcTemplate;

    public Page<Long> findLeadWhatsappLogIds(Long leadId, WhatsappLogFilters filters, Pageable pageable) {
        return findWhatsappLogIds("n_whatsapp_log_lead", "lead_id", leadId, filters, pageable);
    }

    public Page<Long> findAdvisorWhatsappLogIds(Long advisorId, WhatsappLogFilters filters, Pageable pageable) {
        return findWhatsappLogIds("n_whatsapp_log_advisor", "advisor_id", advisorId, filters, pageable);
    }

    private Page<Long> findWhatsappLogIds(
            String mappingTable, String ownerColumn, Long ownerId,
            WhatsappLogFilters filters, Pageable pageable) {

        WhatsappLogFilters effectiveFilters = filters != null ? filters : new WhatsappLogFilters();
        List<Object> queryParams = new ArrayList<>();
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");

        whereClause.append(" AND m.").append(ownerColumn).append(" = ? ");
        queryParams.add(ownerId);

        appendMessageTypeFilter(effectiveFilters, whereClause, queryParams);
        appendSentByFilter(effectiveFilters, whereClause, queryParams);
        appendStatusFilter(effectiveFilters, whereClause, queryParams);

        String fromClause = " FROM " + mappingTable + " m "
                + " JOIN n_whatsapp_log l ON l.id = m.whatsapp_log_id ";

        String countSql = "SELECT COUNT(*) " + fromClause + whereClause;
        String dataSql = "SELECT m.whatsapp_log_id " + fromClause + whereClause
                + " ORDER BY m.whatsapp_log_id DESC LIMIT ? OFFSET ?";

        try {
            Long total = jdbcTemplate.queryForObject(countSql, Long.class, queryParams.toArray());
            long totalElements = total != null ? total : 0L;

            List<Object> dataParams = new ArrayList<>(queryParams);
            dataParams.add(pageable.getPageSize());
            dataParams.add(pageable.getOffset());

            List<Long> ids = jdbcTemplate.queryForList(dataSql, Long.class, dataParams.toArray());

            return new PageImpl<>(ids, pageable, totalElements);
        } catch (Exception e) {
            logger.error("Failed to fetch whatsapp log ids. SQL: {}", dataSql, e);
            logger.error("Query params: {}", queryParams, e);
            throw new RuntimeException("Failed to fetch whatsapp log ids: " + e.getMessage(), e);
        }
    }

    private void appendMessageTypeFilter(WhatsappLogFilters filters, StringBuilder whereClause, List<Object> params) {
        if (CollectionUtils.isEmpty(filters.getMessageType())) return;
        List<String> normalized = normalize(filters.getMessageType());
        if (normalized.isEmpty()) return;
        whereClause.append(" AND l.message_type::text IN (")
                .append(createPlaceholders(normalized.size())).append(") ");
        params.addAll(normalized);
    }

    private void appendSentByFilter(WhatsappLogFilters filters, StringBuilder whereClause, List<Object> params) {
        if (CollectionUtils.isEmpty(filters.getSentBy())) return;
        List<String> normalized = normalize(filters.getSentBy());
        if (normalized.isEmpty()) return;
        whereClause.append(" AND l.sent_by::text IN (")
                .append(createPlaceholders(normalized.size())).append(") ");
        params.addAll(normalized);
    }

    private void appendStatusFilter(WhatsappLogFilters filters, StringBuilder whereClause, List<Object> params) {
        if (CollectionUtils.isEmpty(filters.getStatus())) return;
        List<String> normalized = normalize(filters.getStatus());
        if (normalized.isEmpty()) return;
        whereClause.append(" AND l.status::text IN (")
                .append(createPlaceholders(normalized.size())).append(") ");
        params.addAll(normalized);
    }

    private static List<String> normalize(List<String> values) {
        return values.stream()
                .filter(Objects::nonNull)
                .map(v -> v.toUpperCase(Locale.ROOT).trim())
                .filter(v -> !v.isEmpty())
                .toList();
    }

    private String createPlaceholders(int count) {
        return String.join(",", Collections.nCopies(count, "?"));
    }
}
