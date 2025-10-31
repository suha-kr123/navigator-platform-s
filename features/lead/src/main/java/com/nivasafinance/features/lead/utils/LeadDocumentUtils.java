package com.nivasafinance.features.lead.utils;

import java.util.UUID;
import java.util.regex.Pattern;

public final class LeadDocumentUtils {

    private static final Pattern NON_COMPLIANT_CHARS = Pattern.compile("[^A-Za-z0-9._-]");

    private static String sanitizeFileName(String fileName) {
        String trimmed = fileName.trim();
        if (trimmed.isEmpty()) {
            trimmed = "document";
        }
        String normalisedWhitespace = trimmed.replaceAll("\\s+", "_");
        return NON_COMPLIANT_CHARS.matcher(normalisedWhitespace).replaceAll("_");
    }

    public static String generateDocumentPathForLead(UUID leadIdentifier, String fileName) {
        String sanitisedFileName = sanitizeFileName(fileName);
        return "leads/" + leadIdentifier + "/" + System.currentTimeMillis() + "_" + sanitisedFileName;
    }
}
