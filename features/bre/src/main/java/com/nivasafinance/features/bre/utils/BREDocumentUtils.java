package com.nivasafinance.features.bre.utils;

import java.util.regex.Pattern;

public final class BREDocumentUtils {

    private static final Pattern NON_COMPLIANT_CHARS = Pattern.compile("[^A-Za-z0-9._-]");

    private static String sanitizeFileName(String fileName) {
        String trimmed = fileName != null ? fileName.trim() : "";
        if (trimmed.isEmpty()) {
            trimmed = "rule.json";
        }
        String normalisedWhitespace = trimmed.replaceAll("\\s+", "_");
        return NON_COMPLIANT_CHARS.matcher(normalisedWhitespace).replaceAll("_");
    }

    public static String generateDocumentPathForRuleFile(Long configId, String fileName) {
        String sanitisedFileName = sanitizeFileName(fileName);
        return "bre/" + configId + "/rules/" + System.currentTimeMillis() + "_" + sanitisedFileName;
    }
}
