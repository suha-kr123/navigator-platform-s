package com.nivasafinance.integrations.framework.core.data;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationResponseTest {

    // ── isSuccess ──

    @Test
    void isSuccess_whenStatusIsSuccess_returnsTrue() {
        IntegrationResponse response = IntegrationResponse.successResponse(HttpStatus.OK, "body");

        assertTrue(response.isSuccess(), "Should return true when response status is SUCCESS");
    }

    @Test
    void isSuccess_whenStatusIsClientError_returnsFalse() {
        IntegrationResponse response = IntegrationResponse.clientErrorResponse(HttpStatus.BAD_REQUEST, "error");

        assertFalse(response.isSuccess(), "Should return false when response status is CLIENT_ERROR");
    }

    @Test
    void isSuccess_whenStatusIsServerError_returnsFalse() {
        IntegrationResponse response = IntegrationResponse.serverErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "error");

        assertFalse(response.isSuccess(), "Should return false when response status is SERVER_ERROR");
    }

    // ── successResponse ──

    @Test
    void successResponse_withHttpStatusAndBody_setsAllFields() {
        IntegrationResponse response = IntegrationResponse.successResponse(HttpStatus.OK, "response-body");

        assertEquals(IntegrationResponseStatus.SUCCESS, response.getResponseStatus(),
                "Response status should be SUCCESS");
        assertEquals(HttpStatus.OK, response.getHttpStatus(),
                "HTTP status should be OK");
        assertEquals("response-body", response.getResponseBody(),
                "Response body should match the provided body");
        assertNull(response.getErrorMessage(),
                "Error message should be null for success responses");
    }

    // ── clientErrorResponse ──

    @Test
    void clientErrorResponse_withHttpStatusAndBody_setsCorrectStatus() {
        IntegrationResponse response = IntegrationResponse.clientErrorResponse(HttpStatus.BAD_REQUEST, "bad request");

        assertEquals(IntegrationResponseStatus.CLIENT_ERROR, response.getResponseStatus(),
                "Response status should be CLIENT_ERROR");
        assertEquals(HttpStatus.BAD_REQUEST, response.getHttpStatus(),
                "HTTP status should be BAD_REQUEST");
        assertEquals("bad request", response.getResponseBody(),
                "Response body should contain the error details");
    }

    @Test
    void clientErrorResponse_withErrorMessageOnly_setsErrorMessageAndNullHttpStatus() {
        IntegrationResponse response = IntegrationResponse.clientErrorResponse("connection failed");

        assertEquals(IntegrationResponseStatus.CLIENT_ERROR, response.getResponseStatus(),
                "Response status should be CLIENT_ERROR");
        assertNull(response.getHttpStatus(),
                "HTTP status should be null when only error message is provided");
        assertEquals("connection failed", response.getErrorMessage(),
                "Error message should match the provided message");
    }

    // ── serverErrorResponse ──

    @Test
    void serverErrorResponse_withHttpStatusAndBody_setsCorrectStatus() {
        IntegrationResponse response = IntegrationResponse.serverErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "internal error");

        assertEquals(IntegrationResponseStatus.SERVER_ERROR, response.getResponseStatus(),
                "Response status should be SERVER_ERROR");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus(),
                "HTTP status should be INTERNAL_SERVER_ERROR");
        assertEquals("internal error", response.getResponseBody(),
                "Response body should contain the error details");
    }

    @Test
    void serverErrorResponse_withErrorMessageOnly_setsErrorMessageAndNullHttpStatus() {
        IntegrationResponse response = IntegrationResponse.serverErrorResponse("timeout");

        assertEquals(IntegrationResponseStatus.SERVER_ERROR, response.getResponseStatus(),
                "Response status should be SERVER_ERROR");
        assertNull(response.getHttpStatus(),
                "HTTP status should be null when only error message is provided");
        assertEquals("timeout", response.getErrorMessage(),
                "Error message should match the provided message");
    }
}
