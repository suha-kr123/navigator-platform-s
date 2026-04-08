package com.nivasafinance.notification.orchestrator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.notification.orchestrator.dto.WatiSendTemplateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WatiResponseParserTest {

    private WatiResponseParser watiResponseParser;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        watiResponseParser = new WatiResponseParser(objectMapper);
        watiResponseParser.init();
    }

    @Test
    void parseSendTemplateResponse_nullInput_returnsNull() {
        WatiSendTemplateResponse result = watiResponseParser.parseSendTemplateResponse(null);

        assertNull(result, "Should return null for null input");
    }

    @Test
    void parseSendTemplateResponse_blankInput_returnsNull() {
        WatiSendTemplateResponse result = watiResponseParser.parseSendTemplateResponse("  ");

        assertNull(result, "Should return null for blank input");
    }

    @Test
    void parseSendTemplateResponse_emptyInput_returnsNull() {
        WatiSendTemplateResponse result = watiResponseParser.parseSendTemplateResponse("");

        assertNull(result, "Should return null for empty input");
    }

    @Test
    void parseSendTemplateResponse_validJson_parsesSuccessfully() {
        String json = """
                {
                  "result": true,
                  "error": null,
                  "templateName": "test_template",
                  "receivers": [
                    {
                      "localMessageId": "msg-001",
                      "waId": "919876543210",
                      "isValidWhatsAppNumber": true,
                      "errors": []
                    }
                  ]
                }
                """;

        WatiSendTemplateResponse result = watiResponseParser.parseSendTemplateResponse(json);

        assertNotNull(result, "Should parse valid JSON");
        assertTrue(result.getResult(), "Result should be true");
        assertEquals("test_template", result.getTemplateName(), "Template name should match");
        assertEquals(1, result.getReceivers().size(), "Should have 1 receiver");
        assertEquals("msg-001", result.getReceivers().get(0).getLocalMessageId(), "Local message ID should match");
        assertEquals("919876543210", result.getReceivers().get(0).getWaId(), "WaId should match");
        assertTrue(result.getReceivers().get(0).getIsValidWhatsAppNumber(), "Should be valid WhatsApp number");
    }

    @Test
    void parseSendTemplateResponse_jsonWithUnknownProperties_ignoresThem() {
        String json = """
                {
                  "result": true,
                  "templateName": "test_template",
                  "parameters": [{"name": "1", "value": "test"}],
                  "receivers": [
                    {
                      "localMessageId": "msg-001",
                      "waId": "919876543210",
                      "isValidWhatsAppNumber": true,
                      "someUnknownField": "value"
                    }
                  ]
                }
                """;

        WatiSendTemplateResponse result = watiResponseParser.parseSendTemplateResponse(json);

        assertNotNull(result, "Should parse JSON with unknown properties");
        assertEquals("test_template", result.getTemplateName(), "Template name should match");
    }

    @Test
    void parseSendTemplateResponse_invalidJson_returnsNull() {
        String invalidJson = "not a json string at all";

        WatiSendTemplateResponse result = watiResponseParser.parseSendTemplateResponse(invalidJson);

        assertNull(result, "Should return null for invalid JSON");
    }

    @Test
    void parseSendTemplateResponse_multipleReceivers_parsesAll() {
        String json = """
                {
                  "result": true,
                  "templateName": "template",
                  "receivers": [
                    { "localMessageId": "msg-001", "waId": "911111111111", "isValidWhatsAppNumber": true },
                    { "localMessageId": "msg-002", "waId": "912222222222", "isValidWhatsAppNumber": false }
                  ]
                }
                """;

        WatiSendTemplateResponse result = watiResponseParser.parseSendTemplateResponse(json);

        assertNotNull(result, "Should parse multi-receiver response");
        assertEquals(2, result.getReceivers().size(), "Should have 2 receivers");
    }

    @Test
    void parseSendTemplateResponse_errorResponse_parsesErrorField() {
        String json = """
                {
                  "result": false,
                  "error": "Template not found",
                  "templateName": null,
                  "receivers": null
                }
                """;

        WatiSendTemplateResponse result = watiResponseParser.parseSendTemplateResponse(json);

        assertNotNull(result, "Should parse error response");
        assertFalse(result.getResult(), "Result should be false");
        assertEquals("Template not found", result.getError(), "Error message should match");
    }
}
