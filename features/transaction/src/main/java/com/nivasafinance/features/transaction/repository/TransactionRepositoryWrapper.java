package com.nivasafinance.features.transaction.repository;

import com.nivasafinance.common.base.model.PaginatedResponse;
import com.nivasafinance.common.base.model.PaginationInfo;
import com.nivasafinance.common.base.model.PaginationRequest;
import com.nivasafinance.features.transaction.dto.DisbursedLeadResponse;
import com.nivasafinance.features.transaction.dto.LeadTransactionResponse;
import com.nivasafinance.features.transaction.dto.TransactionDashboardFilters;
import com.nivasafinance.features.transaction.entity.LeadTransaction;
import com.nivasafinance.features.transaction.entity.Transaction;
import com.nivasafinance.features.transaction.entity.TransactionEvent;
import com.nivasafinance.features.transaction.entity.TransactionPayment;
import com.nivasafinance.features.transaction.exception.TransactionExceptionFactory;
import com.nivasafinance.features.transaction.exception.TransactionOperationException;
import com.nivasafinance.features.transaction.repository.mapper.DisbursedLeadRowMapper;
import com.nivasafinance.features.transaction.repository.mapper.LeadTransactionRowMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class TransactionRepositoryWrapper {

    private static final String LOG_SAVE_FAILED = "Failed to save transaction";
    private static final String LOG_SEARCH_FAILED = "Failed to search lead transactions";

    private final TransactionRepository transactionRepository;
    private final TransactionEventRepository transactionEventRepository;
    private final TransactionPaymentRepository transactionPaymentRepository;
    private final LeadTransactionRepository leadTransactionRepository;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final LeadTransactionRowMapper leadTransactionRowMapper;
    private final DisbursedLeadRowMapper disbursedLeadRowMapper;
    private final MessageSource messageSource;

    public Transaction saveTransaction(Transaction transaction) {
        try {
            return transactionRepository.saveAndFlush(transaction);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new TransactionOperationException("Concurrent modification on transaction", e);
        } catch (DataAccessException e) {
            log.error(LOG_SAVE_FAILED, e);
            throw new TransactionOperationException("Failed to save transaction", e);
        }
    }

    public Transaction findByIdentifier(UUID identifier) {
        return transactionRepository.findByIdentifier(identifier)
                .orElseThrow(() -> TransactionExceptionFactory.notFound(identifier, messageSource));
    }

    public Optional<Transaction> findByIdempotencyKey(String idempotencyKey) {
        return transactionRepository.findByIdempotencyKey(idempotencyKey);
    }

    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return transactionRepository.existsByIdempotencyKey(idempotencyKey);
    }

    public TransactionEvent saveEvent(TransactionEvent event) {
        return transactionEventRepository.save(event);
    }

    public List<TransactionEvent> getEventHistory(Long transactionId) {
        return transactionEventRepository.findByTransactionIdOrderByEventTimestampAsc(transactionId);
    }

    public TransactionPayment savePayment(TransactionPayment payment) {
        return transactionPaymentRepository.save(payment);
    }

    public List<TransactionPayment> getPayments(Long transactionId) {
        return transactionPaymentRepository.findByTransactionId(transactionId);
    }

    public LeadTransaction saveLeadTransaction(LeadTransaction leadTransaction) {
        return leadTransactionRepository.save(leadTransaction);
    }

    public List<LeadTransaction> findLeadTransactionsByLead(Long leadId) {
        return leadTransactionRepository.findByLeadId(leadId);
    }

    public Optional<LeadTransaction> findLeadTransactionByTransaction(Long transactionId) {
        return leadTransactionRepository.findByTransactionId(transactionId);
    }

    public List<LeadTransaction> findLeadTransactionsByReferralCode(String referralCode) {
        return leadTransactionRepository.findByReferralCode(referralCode);
    }

    public UUID findLeadIdentifierByLeadId(Long leadId) {
        String sql = "SELECT l.lead_identifier FROM n_lead l WHERE l.id = :leadId";
        MapSqlParameterSource params = new MapSqlParameterSource("leadId", leadId);
        String result = namedParameterJdbcTemplate.queryForObject(sql, params, String.class);
        return result != null ? UUID.fromString(result) : null;
    }

    public PaginatedResponse<DisbursedLeadResponse> getDisbursedLeadsPendingTransaction(
            PaginationRequest paginationRequest) {
        try {
            String whereClause = buildDisbursedLeadsWhereClause();

            String countSql = "SELECT COUNT(*) FROM n_lead l "
                    + "LEFT JOIN n_sourcing_channel_details sc ON sc.id = l.sourcing_channel_id "
                    + "LEFT JOIN n_referral_code_registry rcr ON rcr.referral_code = sc.marketing_details->>'referredByCode' "
                    + whereClause;

            MapSqlParameterSource params = new MapSqlParameterSource();
            Long totalElements = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
            if (totalElements == null) {
                totalElements = 0L;
            }

            params.addValue("limit", paginationRequest.getLimit());
            params.addValue("offset", paginationRequest.getOffset());

            String dataSql = buildDisbursedLeadsQuery(whereClause)
                    + "LIMIT :limit OFFSET :offset";

            List<DisbursedLeadResponse> content = namedParameterJdbcTemplate.query(dataSql, params, disbursedLeadRowMapper);

            int limit = paginationRequest.getLimit();
            int offset = paginationRequest.getOffset();
            int totalPages = limit > 0 ? (int) Math.ceil((double) totalElements / limit) : 0;
            int currentPage = limit > 0 ? (offset / limit) : 0;

            PaginationInfo paginationInfo = PaginationInfo.builder()
                    .offset(offset)
                    .limit(limit)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .currentPage(currentPage)
                    .hasNext(offset + limit < totalElements)
                    .hasPrevious(offset > 0)
                    .build();

            return PaginatedResponse.<DisbursedLeadResponse>builder()
                    .content(content)
                    .pagination(paginationInfo)
                    .build();
        } catch (DataAccessException e) {
            log.error("Failed to search disbursed leads pending transaction", e);
            throw new TransactionOperationException("Failed to search disbursed leads", e);
        }
    }

    public PaginatedResponse<LeadTransactionResponse> getTransactionDashboard(
            PaginationRequest paginationRequest,
            TransactionDashboardFilters filters) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            StringBuilder whereClause = buildWhereClause(filters, params);

            String countSql = "SELECT COUNT(*) FROM n_transaction t "
                    + "JOIN n_lead_transaction lt ON lt.transaction_id = t.id "
                    + whereClause;

            Long totalElements = namedParameterJdbcTemplate.queryForObject(countSql, params, Long.class);
            if (totalElements == null) {
                totalElements = 0L;
            }

            String dataSql = buildSearchQuery(whereClause.toString(), true);
            params.addValue("limit", paginationRequest.getLimit());
            params.addValue("offset", paginationRequest.getOffset());

            List<LeadTransactionResponse> content = namedParameterJdbcTemplate.query(dataSql, params, leadTransactionRowMapper);

            int limit = paginationRequest.getLimit();
            int offset = paginationRequest.getOffset();
            int totalPages = limit > 0 ? (int) Math.ceil((double) totalElements / limit) : 0;
            int currentPage = limit > 0 ? (offset / limit) : 0;

            PaginationInfo paginationInfo = PaginationInfo.builder()
                    .offset(offset)
                    .limit(limit)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .currentPage(currentPage)
                    .hasNext(offset + limit < totalElements)
                    .hasPrevious(offset > 0)
                    .build();

            return PaginatedResponse.<LeadTransactionResponse>builder()
                    .content(content)
                    .pagination(paginationInfo)
                    .build();
        } catch (DataAccessException e) {
            log.error(LOG_SEARCH_FAILED, e);
            throw new TransactionOperationException("Failed to search transactions", e);
        }
    }

    public List<LeadTransactionResponse> searchByLeadIdentifier(UUID leadIdentifier) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("leadIdentifier", leadIdentifier);

        StringBuilder whereClause = new StringBuilder("WHERE l.lead_identifier = :leadIdentifier ");
        String sql = buildSearchQuery(whereClause.toString(), false);

        return namedParameterJdbcTemplate.query(sql, params, leadTransactionRowMapper);
    }

    public List<LeadTransactionResponse> searchByReferralCode(String referralCode) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("referralCode", referralCode);

        StringBuilder whereClause = new StringBuilder("WHERE lt.referral_code = :referralCode ");
        String sql = buildSearchQuery(whereClause.toString(), false);

        return namedParameterJdbcTemplate.query(sql, params, leadTransactionRowMapper);
    }

    public List<LeadTransactionResponse> searchByLeadMobileNumber(String mobileNumber) {
        String phoneJson = buildPhoneNumberJsonb(mobileNumber.trim());
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("phoneJson", phoneJson);

        String whereClause = "WHERE EXISTS ("
                + "SELECT 1 FROM n_applicant app "
                + "JOIN n_person p ON p.id = app.person_id "
                + "WHERE app.id = l.applicant "
                + "AND p.mobile_numbers @> :phoneJson::jsonb"
                + ") ";
        String sql = buildSearchQuery(whereClause, false);

        return namedParameterJdbcTemplate.query(sql, params, leadTransactionRowMapper);
    }

    public List<LeadTransactionResponse> searchByAdvisorMobileNumber(String mobileNumber) {
        String phoneJson = buildPhoneNumberJsonb(mobileNumber.trim());
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("phoneJson", phoneJson);

        String whereClause = "WHERE EXISTS ("
                + "SELECT 1 FROM n_advisor a "
                + "JOIN n_user u ON u.username = a.username "
                + "JOIN n_person p ON p.id = u.person_id "
                + "WHERE a.identifier = rcr.entity_identifier "
                + "AND p.mobile_numbers @> :phoneJson::jsonb"
                + ") ";
        String sql = buildSearchQuery(whereClause, false);

        return namedParameterJdbcTemplate.query(sql, params, leadTransactionRowMapper);
    }

    public List<DisbursedLeadResponse> searchDisbursedLeadsByLeadMobile(String mobileNumber) {
        String phoneJson = buildPhoneNumberJsonb(mobileNumber.trim());
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("phoneJson", phoneJson);

        String whereClause = buildDisbursedLeadsWhereClause()
                + "AND p.mobile_numbers @> :phoneJson::jsonb ";
        String sql = buildDisbursedLeadsQuery(whereClause);

        return namedParameterJdbcTemplate.query(sql, params, disbursedLeadRowMapper);
    }

    public List<DisbursedLeadResponse> searchDisbursedLeadsByAdvisorMobile(String mobileNumber) {
        String phoneJson = buildPhoneNumberJsonb(mobileNumber.trim());
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("phoneJson", phoneJson);

        String whereClause = buildDisbursedLeadsWhereClause()
                + "AND adv_p.mobile_numbers @> :phoneJson::jsonb ";
        String sql = buildDisbursedLeadsQuery(whereClause);

        return namedParameterJdbcTemplate.query(sql, params, disbursedLeadRowMapper);
    }

    private static String buildPhoneNumberJsonb(String mobileNumber) {
        return "[{\"number\":\"" + mobileNumber + "\"}]";
    }

    private StringBuilder buildWhereClause(TransactionDashboardFilters filters, MapSqlParameterSource params) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");

        if (filters.getStatus() != null) {
            where.append("AND t.status = :status ");
            params.addValue("status", filters.getStatus().name());
        }

        return where;
    }

    private String buildSearchQuery(String whereClause, boolean paginated) {
        String query = "SELECT "
                + "t.identifier, t.amount, "
                + "t.status, t.created_by, t.created_at, "
                + "lt.domain_type, lt.referral_code, "
                + "l.lead_identifier, "
                + "rcr.entity_type AS payee_type, "
                + "rcr.entity_identifier AS payee_identifier, "
                + "latest_evt.event_type AS latest_event_type, "
                + "latest_evt.actor_username AS latest_actor, "
                + "latest_evt.event_timestamp AS latest_event_at, "
                + "latest_evt.remarks, "
                + "latest_pmt.payment_mode, "
                + "latest_pmt.external_reference, "
                + "latest_pmt.payment_status, "
                + "latest_pmt.payment_data "
                + "FROM n_transaction t "
                + "JOIN n_lead_transaction lt ON lt.transaction_id = t.id "
                + "JOIN n_lead l ON l.id = lt.lead_id "
                + "LEFT JOIN n_referral_code_registry rcr ON rcr.referral_code = lt.referral_code "
                + "LEFT JOIN LATERAL ("
                + "  SELECT e.event_type, e.actor_username, e.event_timestamp, e.remarks "
                + "  FROM n_transaction_event e "
                + "  WHERE e.transaction_id = t.id "
                + "  ORDER BY e.event_timestamp DESC LIMIT 1"
                + ") latest_evt ON true "
                + "LEFT JOIN LATERAL ("
                + "  SELECT p.payment_mode, p.external_reference, p.payment_status, p.payment_data "
                + "  FROM n_transaction_payment p "
                + "  WHERE p.transaction_id = t.id "
                + "  ORDER BY p.id DESC LIMIT 1"
                + ") latest_pmt ON true "
                + whereClause
                + "ORDER BY t.created_at DESC ";
        if (paginated) {
            query += "LIMIT :limit OFFSET :offset";
        }
        return query;
    }

    private String buildDisbursedLeadsWhereClause() {
        return "WHERE l.is_deleted = false "
                + "AND l.disbursement_details->>'disbursedAmount' IS NOT NULL "
                + "AND CAST(l.disbursement_details->>'disbursedAmount' AS NUMERIC) > 0 "
                + "AND sc.marketing_details->>'referredByCode' IS NOT NULL "
                + "AND rcr.entity_type = 'ADVISOR' "
                + "AND NOT EXISTS ("
                + "  SELECT 1 FROM n_lead_transaction lt WHERE lt.lead_id = l.id"
                + ") ";
    }

    private String buildDisbursedLeadsQuery(String whereClause) {
        return "SELECT "
                + "l.lead_identifier, "
                + "COALESCE(p.display_name, CONCAT(p.first_name, ' ', p.last_name)) AS applicant_name, "
                + "l.product_code, "
                + "CAST(l.disbursement_details->>'disbursedAmount' AS NUMERIC) AS disbursed_amount, "
                + "TO_DATE(l.disbursement_details->>'disbursedDate', 'DD-MM-YYYY') AS disbursed_date, "
                + "l.office_key, "
                + "sc.marketing_details->>'referredByCode' AS referral_code, "
                + "rcr.entity_type AS payee_type, "
                + "rcr.entity_identifier AS payee_identifier, "
                + "COALESCE(adv_p.display_name, CONCAT(adv_p.first_name, ' ', adv_p.last_name)) AS payee_name, "
                + "l.created_at "
                + "FROM n_lead l "
                + "LEFT JOIN n_applicant app ON app.id = l.applicant "
                + "LEFT JOIN n_person p ON p.id = app.person_id "
                + "LEFT JOIN n_sourcing_channel_details sc ON sc.id = l.sourcing_channel_id "
                + "LEFT JOIN n_referral_code_registry rcr ON rcr.referral_code = sc.marketing_details->>'referredByCode' "
                + "LEFT JOIN n_advisor a ON a.identifier = rcr.entity_identifier "
                + "LEFT JOIN n_user adv_u ON adv_u.username = a.username "
                + "LEFT JOIN n_person adv_p ON adv_p.id = adv_u.person_id "
                + whereClause
                + "ORDER BY l.created_at DESC ";
    }
}
