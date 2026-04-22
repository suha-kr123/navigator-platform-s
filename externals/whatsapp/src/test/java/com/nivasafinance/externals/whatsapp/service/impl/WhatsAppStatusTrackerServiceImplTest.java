package com.nivasafinance.externals.whatsapp.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.common.exception.BadRequestException;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppStatusTrackerRequest;
import com.nivasafinance.externals.whatsapp.dto.WhatsAppStatusTrackerResponse;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppStatusTrackerServiceImplTest {

    private static final ZoneId IST_ZONE_ID = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String LEAD_TABLE = "n_lead_whatsapp_notification";
    private static final String ADVISOR_TABLE = "n_advisor_whatsapp_notification";

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private WhatsAppStatusTrackerServiceImpl service;

    // ── trackStatus: reply interaction ──

    @Test
    void trackStatus_withReplyInteraction_updatesLeadAndAdvisorTables() {
        WhatsAppStatusTrackerRequest request = new WhatsAppStatusTrackerRequest();
        request.setId("msg-001");
        request.setTimestamp("1700000000");
        request.setInteraction("reply");
        request.setReply(objectMapper.createObjectNode().put("text", "Hello"));

        when(jdbcTemplate.update(contains(LEAD_TABLE), anyString(), eq("msg-001"))).thenReturn(1);
        when(jdbcTemplate.update(contains(ADVISOR_TABLE), anyString(), eq("msg-001"))).thenReturn(1);

        WhatsAppStatusTrackerResponse result = service.trackStatus(request);

        assertEquals("msg-001", result.getWhatsappMessageId(),
                "WhatsApp message ID should match the request");
        assertEquals("reply", result.getEventType(),
                "Event type should be 'reply' for reply interaction");
        assertTrue(result.isUpdatedInLeadNotification(),
                "Should indicate lead notification was updated");
        assertTrue(result.isUpdatedInAdvisorNotification(),
                "Should indicate advisor notification was updated");
        verify(jdbcTemplate).update(contains(LEAD_TABLE), anyString(), eq("msg-001"));
        verify(jdbcTemplate).update(contains(ADVISOR_TABLE), anyString(), eq("msg-001"));
    }

    @Test
    void trackStatus_withReplyInteraction_whenNoRowsUpdated_returnsFalseFlags() {
        WhatsAppStatusTrackerRequest request = new WhatsAppStatusTrackerRequest();
        request.setId("msg-002");
        request.setTimestamp("1700000000");
        request.setInteraction("reply");

        when(jdbcTemplate.update(contains(LEAD_TABLE), anyString(), eq("msg-002"))).thenReturn(0);
        when(jdbcTemplate.update(contains(ADVISOR_TABLE), anyString(), eq("msg-002"))).thenReturn(0);

        WhatsAppStatusTrackerResponse result = service.trackStatus(request);

        assertFalse(result.isUpdatedInLeadNotification(),
                "Should return false when no lead rows were updated");
        assertFalse(result.isUpdatedInAdvisorNotification(),
                "Should return false when no advisor rows were updated");
    }

    // ── trackStatus: click interaction ──

    @Test
    void trackStatus_withClickInteraction_updatesLeadAndAdvisorTables() {
        WhatsAppStatusTrackerRequest request = new WhatsAppStatusTrackerRequest();
        request.setId("msg-003");
        request.setTimestamp("1700000000");
        request.setInteraction("click");
        request.setClick(objectMapper.createObjectNode().put("url", "https://example.com"));

        when(jdbcTemplate.update(contains(LEAD_TABLE), anyString(), eq("msg-003"))).thenReturn(1);
        when(jdbcTemplate.update(contains(ADVISOR_TABLE), anyString(), eq("msg-003"))).thenReturn(1);

        WhatsAppStatusTrackerResponse result = service.trackStatus(request);

        assertEquals("click", result.getEventType(),
                "Event type should be 'click' for click interaction");
        assertTrue(result.isUpdatedInLeadNotification(),
                "Should indicate lead notification was updated for click");
        assertTrue(result.isUpdatedInAdvisorNotification(),
                "Should indicate advisor notification was updated for click");
    }

    // ── trackStatus: supported statuses ──

    @Test
    void trackStatus_withQueuedStatus_updatesQueuedTime() {
        WhatsAppStatusTrackerRequest request = new WhatsAppStatusTrackerRequest();
        request.setId("msg-004");
        request.setTimestamp("1700000000");
        request.setStatus("queued");

        when(jdbcTemplate.update(contains(LEAD_TABLE), anyString(), eq("msg-004"))).thenReturn(1);
        when(jdbcTemplate.update(contains(ADVISOR_TABLE), anyString(), eq("msg-004"))).thenReturn(1);

        WhatsAppStatusTrackerResponse result = service.trackStatus(request);

        assertEquals("queued", result.getEventType(),
                "Event type should be 'queued'");
        assertTrue(result.isUpdatedInLeadNotification(),
                "Should update lead notification for queued status");
    }

    @Test
    void trackStatus_withDeliveredStatus_updatesDeliveredTime() {
        WhatsAppStatusTrackerRequest request = new WhatsAppStatusTrackerRequest();
        request.setId("msg-005");
        request.setTimestamp("1700000000");
        request.setStatus("delivered");

        when(jdbcTemplate.update(contains(LEAD_TABLE), anyString(), eq("msg-005"))).thenReturn(1);
        when(jdbcTemplate.update(contains(ADVISOR_TABLE), anyString(), eq("msg-005"))).thenReturn(1);

        WhatsAppStatusTrackerResponse result = service.trackStatus(request);

        assertEquals("delivered", result.getEventType(),
                "Event type should be 'delivered'");
        assertTrue(result.isUpdatedInLeadNotification(),
                "Should update lead notification for delivered status");
    }

    @Test
    void trackStatus_withReadStatus_updatesReadTime() {
        WhatsAppStatusTrackerRequest request = new WhatsAppStatusTrackerRequest();
        request.setId("msg-006");
        request.setTimestamp("1700000000");
        request.setStatus("read");

        when(jdbcTemplate.update(contains(LEAD_TABLE), anyString(), eq("msg-006"))).thenReturn(1);
        when(jdbcTemplate.update(contains(ADVISOR_TABLE), anyString(), eq("msg-006"))).thenReturn(1);

        WhatsAppStatusTrackerResponse result = service.trackStatus(request);

        assertEquals("read", result.getEventType(),
                "Event type should be 'read'");
    }

    @Test
    void trackStatus_withFailedStatus_updatesFailedTimeAndErrors() {
        JsonNode errors = objectMapper.createObjectNode().put("code", "131047").put("title", "Rate limit hit");

        WhatsAppStatusTrackerRequest request = new WhatsAppStatusTrackerRequest();
        request.setId("msg-007");
        request.setTimestamp("1700000000");
        request.setStatus("failed");
        request.setErrors(errors);

        when(jdbcTemplate.update(contains(LEAD_TABLE), anyString(), eq("msg-007"))).thenReturn(1);
        when(jdbcTemplate.update(contains(ADVISOR_TABLE), anyString(), eq("msg-007"))).thenReturn(1);

        WhatsAppStatusTrackerResponse result = service.trackStatus(request);

        assertEquals("failed", result.getEventType(),
                "Event type should be 'failed'");
        assertTrue(result.isUpdatedInLeadNotification(),
                "Should update lead notification for failed status with errors");
    }

    // ── trackStatus: unsupported status ──

    @Test
    void trackStatus_withUnsupportedStatus_throwsBadRequestException() {
        WhatsAppStatusTrackerRequest request = new WhatsAppStatusTrackerRequest();
        request.setId("msg-008");
        request.setTimestamp("1700000000");
        request.setStatus("sent");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.trackStatus(request),
                "Unsupported status should be rejected with 400 to avoid silent drops");
        assertTrue(ex.getMessage().toLowerCase().contains("status"),
                "Error message should indicate an invalid status");
        verify(jdbcTemplate, never()).update(anyString(), anyString(), anyString());
    }

    // ── trackStatus: timestamp conversion ──

    @Test
    void trackStatus_convertsUnixTimestampToIstCorrectly() {
        WhatsAppStatusTrackerRequest request = new WhatsAppStatusTrackerRequest();
        request.setId("msg-009");
        request.setTimestamp("1700000000");
        request.setStatus("queued");

        when(jdbcTemplate.update(anyString(), anyString(), anyString())).thenReturn(0);

        WhatsAppStatusTrackerResponse result = service.trackStatus(request);

        LocalDateTime expectedIst = Instant.ofEpochSecond(1700000000L)
                .atZone(IST_ZONE_ID).toLocalDateTime();
        assertEquals(expectedIst.format(FORMATTER), result.getEventTimestampIst(),
                "Timestamp should be converted to IST correctly");
    }

    // ── trackStatus: status normalization ──

    @Test
    void trackStatus_normalizesStatusToLowerCase() {
        WhatsAppStatusTrackerRequest request = new WhatsAppStatusTrackerRequest();
        request.setId("msg-010");
        request.setTimestamp("1700000000");
        request.setStatus("DELIVERED");

        when(jdbcTemplate.update(anyString(), anyString(), anyString())).thenReturn(1);

        WhatsAppStatusTrackerResponse result = service.trackStatus(request);

        assertEquals("delivered", result.getEventType(),
                "Status should be normalized to lowercase");
    }

    @Test
    void trackStatus_normalizesInteractionToLowerCase() {
        WhatsAppStatusTrackerRequest request = new WhatsAppStatusTrackerRequest();
        request.setId("msg-011");
        request.setTimestamp("1700000000");
        request.setInteraction("REPLY");

        when(jdbcTemplate.update(anyString(), anyString(), anyString())).thenReturn(1);

        WhatsAppStatusTrackerResponse result = service.trackStatus(request);

        assertEquals("reply", result.getEventType(),
                "Interaction event type should be normalized to lowercase");
    }

    // ── trackStatus: null status and interaction ──

    @Test
    void trackStatus_withNullStatusAndNullInteraction_throwsBadRequestException() {
        WhatsAppStatusTrackerRequest request = new WhatsAppStatusTrackerRequest();
        request.setId("msg-012");
        request.setTimestamp("1700000000");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.trackStatus(request),
                "Missing status and interaction should be rejected with a clear client error");
        assertTrue(ex.getMessage().contains("status") || ex.getMessage().contains("interaction"),
                "Error message should mention status or interaction");
        verify(jdbcTemplate, never()).update(anyString(), anyString(), anyString());
    }

    @Test
    void trackStatus_withBlankStatusAndNullInteraction_throwsBadRequestException() {
        WhatsAppStatusTrackerRequest request = new WhatsAppStatusTrackerRequest();
        request.setId("msg-013");
        request.setTimestamp("1700000000");
        request.setStatus("   ");

        assertThrows(BadRequestException.class, () -> service.trackStatus(request),
                "Whitespace-only status should be treated as missing");
        verify(jdbcTemplate, never()).update(anyString(), anyString(), anyString());
    }
}
