package com.nivasafinance.features.staff.repository;

import com.nivasafinance.features.staff.dto.StaffTodayActivityResponse.CallStats;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StaffActivityRepositoryWrapper {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String CALL_STATS_QUERY = """
            SELECT
                cl.direction,
                COUNT(*)                                                                  AS total,
                COUNT(*) FILTER (WHERE cl.status = 'COMPLETED')                          AS connected,
                COUNT(*) FILTER (WHERE cl.status = 'NO_ANSWER')                          AS no_answer,
                COUNT(*) FILTER (WHERE cl.status = 'BUSY')                               AS busy,
                COUNT(*) FILTER (WHERE cl.status = 'FAILED')                             AS failed,
                COALESCE(SUM((cl.completion_details->>'duration')::bigint)
                         FILTER (WHERE cl.status = 'COMPLETED'), 0)                      AS total_connect_time_seconds,
                MIN(cl.created_at)                                                        AS first_call_at,
                MAX(cl.created_at)                                                        AS last_call_at
            FROM n_call_log cl
            WHERE cl.created_at >= :startOfDay
              AND cl.created_at <  :endOfDay
              AND (
                  (cl.direction = 'OUTBOUND' AND RIGHT(REGEXP_REPLACE(cl.from_number, '[^0-9]', '', 'g'), 10) = :staffPhone)
               OR (cl.direction = 'INBOUND'  AND RIGHT(REGEXP_REPLACE(cl.to_number,   '[^0-9]', '', 'g'), 10) = :staffPhone)
              )
            GROUP BY cl.direction
            """;

    private static final String STAGE_MOVE_COUNT_QUERY = """
            SELECT COUNT(*)
            FROM n_lead_stage_history
            WHERE moved_by   = :staffUsername
              AND entered_at >= :startOfDay
              AND entered_at <  :endOfDay
              AND stage_from IS NOT NULL
            """;

    public Map<String, Object> getCallStats(String staffPhone, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("staffPhone", staffPhone)
                .addValue("startOfDay", startOfDay)
                .addValue("endOfDay", endOfDay);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(CALL_STATS_QUERY, params);

        LocalDateTime firstCallAt = rows.stream()
                .map(r -> toLocalDateTime(r.get("first_call_at")))
                .filter(dt -> dt != null)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime lastCallAt = rows.stream()
                .map(r -> toLocalDateTime(r.get("last_call_at")))
                .filter(dt -> dt != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        Map<String, Object> result = new HashMap<>();
        result.put("inbound", buildCallStats(rows, "INBOUND"));
        result.put("outbound", buildCallStats(rows, "OUTBOUND"));
        result.put("firstCallAt", firstCallAt);
        result.put("lastCallAt", lastCallAt);
        return result;
    }

    public long getStageMoveCount(String staffUsername, LocalDateTime startOfDay, LocalDateTime endOfDay) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("staffUsername", staffUsername)
                .addValue("startOfDay", startOfDay)
                .addValue("endOfDay", endOfDay);

        Long count = jdbcTemplate.queryForObject(STAGE_MOVE_COUNT_QUERY, params, Long.class);
        return count != null ? count : 0L;
    }

    private CallStats buildCallStats(List<Map<String, Object>> rows, String direction) {
        return rows.stream()
                .filter(r -> direction.equals(r.get("direction")))
                .findFirst()
                .map(r -> {
                    long connected = toLong(r.get("connected"));
                    long totalConnectTime = toLong(r.get("total_connect_time_seconds"));
                    return CallStats.builder()
                            .total(toLong(r.get("total")))
                            .connected(connected)
                            .noAnswer(toLong(r.get("no_answer")))
                            .busy(toLong(r.get("busy")))
                            .failed(toLong(r.get("failed")))
                            .totalConnectTimeSeconds(totalConnectTime)
                            .avgConnectTimeSeconds(connected > 0 ? totalConnectTime / connected : 0L)
                            .build();
                })
                .orElse(CallStats.builder().build());
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Long l) return l;
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }

    private LocalDateTime toLocalDateTime(Object val) {
        if (val == null) return null;
        if (val instanceof Timestamp ts) return ts.toLocalDateTime();
        return null;
    }
}
