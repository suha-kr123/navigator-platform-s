package com.nivasafinance.services.creditbureau.provider.crifhighmark;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nivasafinance.integrations.framework.core.exception.NavigatorIntegrationServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrifResponseHandler {

    private static final Pattern REPORT_ID_PATTERN = Pattern.compile("reportId[\\s:=]+([\\w-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern STATUS_CODE_PATTERN = Pattern.compile("S(\\d{2})", Pattern.CASE_INSENSITIVE);

    private final ObjectMapper objectMapper;

    public String parseStage1Response(String response) {
        if (!StringUtils.hasText(response)) {
            throw new NavigatorIntegrationServerException("Stage-I response is empty");
        }

        String statusCode = extractStatusCode(response);
        if (!"S06".equalsIgnoreCase(statusCode)) {
            throw new NavigatorIntegrationServerException("Stage-I failed: Expected S06, got " + statusCode);
        }

        String reportId = extractReportId(response);
        if (!StringUtils.hasText(reportId)) {
            throw new NavigatorIntegrationServerException("Stage-I response does not contain reportId");
        }

        log.debug("Stage-I successful: reportId={}", reportId);
        return reportId;
    }

    public void parseStage2Response(String response) {
        if (!StringUtils.hasText(response)) {
            throw new NavigatorIntegrationServerException("Stage-II response is empty");
        }

        String statusCode = extractStatusCode(response);
        if (!"S01".equalsIgnoreCase(statusCode) && !"S10".equalsIgnoreCase(statusCode)) {
            String errorMsg = "Stage-II failed: Expected S01 or S10, got " + statusCode;
            log.error(errorMsg);
            throw new NavigatorIntegrationServerException(errorMsg);
        }

        log.debug("Stage-II successful: statusCode={}", statusCode);
    }


    private String extractStatusCode(String response) {
        Matcher matcher = STATUS_CODE_PATTERN.matcher(response);
        if (matcher.find()) {
            return "S" + matcher.group(1);
        }
        return null;
    }

    private String extractReportId(String response) {
        Matcher matcher = REPORT_ID_PATTERN.matcher(response);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        // Try JSON parsing if it's a JSON response
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            if (jsonNode.has("reportId")) {
                JsonNode reportIdNode = jsonNode.get("reportId");
                if (reportIdNode != null && !reportIdNode.isNull()) {
                    return reportIdNode.asText();
                }
            }
            if (jsonNode.has("report_id")) {
                JsonNode reportIdNode = jsonNode.get("report_id");
                if (reportIdNode != null && !reportIdNode.isNull()) {
                    return reportIdNode.asText();
                }
            }
        } catch (Exception e) {
            log.debug("Response is not JSON, trying regex pattern");
        }
        return null;
    }
}
