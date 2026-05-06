package com.nivasafinance.externals.gallabox.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.externals.gallabox.dto.WhatsAppStatusTrackerRequest;
import com.nivasafinance.externals.gallabox.dto.WhatsAppStatusTrackerResponse;
import com.nivasafinance.externals.gallabox.service.WhatsAppStatusTrackerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class WhatsAppStatusTrackerServiceImpl implements WhatsAppStatusTrackerService {

    private static final String LOG_BAD_REQUEST_PREFIX = "WhatsApp status tracker bad request";

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
        requireNonBlankMessageId(request.getId());
        requireNonBlankTimestamp(request.getId(), request.getTimestamp());
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
        } else if (status != null && SUPPORTED_STATUSES.contains(status)) {
            leadUpdatedRows = updateStatusJson(LEAD_TABLE, request.getId(), status, eventTimeIst, request.getErrors());
            advisorUpdatedRows = updateStatusJson(ADVISOR_TABLE, request.getId(), status, eventTimeIst, request.getErrors());
        } else if (status == null || status.isEmpty()) {
            log.warn("{}: messageId={}, reason=status or interaction is required (status missing or blank after normalize)",
                    LOG_BAD_REQUEST_PREFIX, request.getId());
            throw new BadRequestException("status or interaction is required");
        } else {
            log.warn("{}: messageId={}, reason=invalid status, status={}",
                    LOG_BAD_REQUEST_PREFIX, request.getId(), status);
            throw new BadRequestException("invalid status");
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

    private void requireNonBlankMessageId(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            log.warn("{}: messageId is null or blank", LOG_BAD_REQUEST_PREFIX);
            throw new BadRequestException("id is required");
        }
    }

    private void requireNonBlankTimestamp(String messageId, String unixEpochSeconds) {
        if (unixEpochSeconds == null || unixEpochSeconds.isBlank()) {
            log.warn("{}: messageId={}, reason=timestamp is null or blank", LOG_BAD_REQUEST_PREFIX, messageId);
            throw new BadRequestException("timestamp is required");
        }
    }

    private LocalDateTime toIstLocalDateTime(String unixEpochSeconds) {
        try {
            long epochSeconds = Long.parseLong(unixEpochSeconds.trim());
            return Instant.ofEpochSecond(epochSeconds).atZone(IST_ZONE_ID).toLocalDateTime();
        } catch (NumberFormatException e) {
            log.warn("{}: invalid timestamp value (expected Unix epoch seconds), raw={}",
                    LOG_BAD_REQUEST_PREFIX, unixEpochSeconds);
            throw new BadRequestException("timestamp must be a Unix epoch in seconds");
        }
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
