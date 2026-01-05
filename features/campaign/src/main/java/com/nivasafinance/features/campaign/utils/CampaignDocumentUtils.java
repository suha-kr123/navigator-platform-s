package com.nivasafinance.features.campaign.utils;

import com.nivasafinance.redash.dto.FileType;

import java.util.UUID;
import java.util.regex.Pattern;

public final class CampaignDocumentUtils {

    private static final Pattern NON_COMPLIANT_CHARS = Pattern.compile("[^A-Za-z0-9._-]");

    private static String sanitizeFileName(String fileName) {
        String trimmed = fileName.trim();
        if (trimmed.isEmpty()) {
            trimmed = "document";
        }
        String normalisedWhitespace = trimmed.replaceAll("\\s+", "_");
        return NON_COMPLIANT_CHARS.matcher(normalisedWhitespace).replaceAll("_");
    }

    public static String generateDocumentPathForContactsFile(Long campaignId, String fileName) {
        String sanitisedFileName = sanitizeFileName(fileName);
        return "campaigns/" + campaignId + "/documents/" + sanitisedFileName;
    }

    public static String generateDocumentPathForReports(Long campaignId, String fileName) {
        String sanitisedFileName = sanitizeFileName(fileName);
        return "campaigns/" + campaignId + "/reports/" + sanitisedFileName;
    }
}
