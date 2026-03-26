package com.nivasafinance.externals.whatsapp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppStatusTrackerRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppStatusTrackerResponse;
import com.nivasafinance.externals.whatsapp.service.WhatsAppStatusTrackerService;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@AllArgsConstructor
public class WhatsAppStatusTrackerServiceImpl implements WhatsAppStatusTrackerService {

    private static final String LEAD_TABLE = "n_lead_whatsapp_notification";
    private static final String ADVISOR_TABLE = "n_advisor_whatsapp_notification";
    private static final ZoneId IST_ZONE_ID = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter RESPONSE_TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final List<String> SUPPORTED_STATUSES = List.of("queued", "delivered", "read", "failed");
    private static final String INTERACTION_REPLY = "reply";
    private static final String INTERACTION_CLICK = "click";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public WhatsAppStatusTrackerResponse trackStatus(WhatsAppStatusTrackerRequest request) {
        LocalDateTime eventTimeIst = toIstLocalDateTime(request.getTimestamp());
        String status = normalize(request.getStatus());
        String interaction = normalize(request.getInteraction());

        int leadUpdatedRows = 0;
        int advisorUpdatedRows = 0;

        if (INTERACTION_REPLY.equals(interaction)) {
            leadUpdatedRows = mergeStatusTrack(LEAD_TABLE, request.getId(),
                    jsonPatch("reply_time", eventTimeIst, "reply", request.getReply()));
            advisorUpdatedRows = mergeStatusTrack(ADVISOR_TABLE, request.getId(),
                    jsonPatch("reply_time", eventTimeIst, "reply", request.getReply()));
        } else if (INTERACTION_CLICK.equals(interaction)) {
            leadUpdatedRows = mergeStatusTrack(LEAD_TABLE, request.getId(),
                    jsonPatch("click_time", eventTimeIst, "click", request.getClick()));
            advisorUpdatedRows = mergeStatusTrack(ADVISOR_TABLE, request.getId(),
                    jsonPatch("click_time", eventTimeIst, "click", request.getClick()));
        } else if (SUPPORTED_STATUSES.contains(status)) {
            leadUpdatedRows = updateStatusJson(LEAD_TABLE, request.getId(), status, eventTimeIst, request.getErrors());
            advisorUpdatedRows = updateStatusJson(ADVISOR_TABLE, request.getId(), status, eventTimeIst, request.getErrors());
        }

        String eventType = status != null ? status : interaction;
        return WhatsAppStatusTrackerResponse.builder()
                .whatsappMessageId(request.getId())
                .eventType(eventType)
                .eventTimestampIst(eventTimeIst.format(RESPONSE_TIMESTAMP_FORMATTER))
                .updatedInLeadNotification(leadUpdatedRows > 0)
                .updatedInAdvisorNotification(advisorUpdatedRows > 0)
                .build();
    }

    private int updateStatusJson(
            String tableName,
            String whatsappMessageId,
            String status,
            LocalDateTime eventTimeIst,
            JsonNode errors
    ) {
        if ("queued".equals(status)) {
            return mergeStatusTrack(tableName, whatsappMessageId, jsonPatch("queued_time", eventTimeIst));
        } else if ("delivered".equals(status)) {
            return mergeStatusTrack(tableName, whatsappMessageId, jsonPatch("delivered_time", eventTimeIst));
        } else if ("read".equals(status)) {
            return mergeStatusTrack(tableName, whatsappMessageId, jsonPatch("read_time", eventTimeIst));
        } else if ("failed".equals(status)) {
            return mergeStatusTrack(tableName, whatsappMessageId, jsonPatch("failed_time", eventTimeIst, "errors", errors));
        }
        return 0;
    }

    private int mergeStatusTrack(String tableName, String whatsappMessageId, JsonNode patch) {
        String sql = "UPDATE " + tableName + " SET status_track = COALESCE(status_track, '{}'::jsonb) || CAST(? AS jsonb) WHERE whatsapp_message_id = ?";
        return jdbcTemplate.update(sql, toJsonString(patch), whatsappMessageId);
    }

    private LocalDateTime toIstLocalDateTime(String unixEpochSeconds) {
        long epochSeconds = Long.parseLong(unixEpochSeconds);
        return Instant.ofEpochSecond(epochSeconds).atZone(IST_ZONE_ID).toLocalDateTime();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }

    private JsonNode jsonPatch(String timeKey, LocalDateTime time) {
        String json = "{\"" + timeKey + "\": \"" + time.format(RESPONSE_TIMESTAMP_FORMATTER) + "\"}";
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed building JSON patch", e);
        }
    }

    private JsonNode jsonPatch(String timeKey, LocalDateTime time, String payloadKey, JsonNode payloadNode) {
        ObjectNode patch = objectMapper.createObjectNode();
        patch.put(timeKey, time.format(RESPONSE_TIMESTAMP_FORMATTER));
        if (payloadNode != null && !payloadNode.isNull()) {
            patch.set(payloadKey, payloadNode);
        }
        return patch;
    }

    private String toJsonString(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return "null";
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Failed to parse JSON payload for status tracker", exception);
        }
    }
}
