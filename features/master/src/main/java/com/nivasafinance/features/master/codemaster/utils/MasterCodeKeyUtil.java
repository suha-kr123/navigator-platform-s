package com.nivasafinance.features.master.codemaster.utils;

import java.util.Map;
import java.util.Set;

public final class MasterCodeKeyUtil {

    private MasterCodeKeyUtil() {
        // utility class
    }

    /**
     * Builds a unique master code key like:
     * ONHOLD_MASTER_REASON, ONHOLD_MASTER_REASON_1, ONHOLD_MASTER_REASON_2 ...
     */
    public static String generateUniqueKey(
            String baseKeyPart,
            String suffix,
            Set<String> existingKeys) {

        if (baseKeyPart == null || baseKeyPart.isBlank()) {
            throw new IllegalArgumentException("Base key part cannot be null or blank");
        }

        String normalizedBase =
                normalize(baseKeyPart) + "_" + normalize(suffix);

        if (existingKeys == null || !existingKeys.contains(normalizedBase)) {
            return normalizedBase;
        }

        int counter = 1;
        String candidate;

        do {
            candidate = normalizedBase + "_" + counter;
            counter++;
        } while (existingKeys.contains(candidate));

        return candidate;
    }

    /**
     * Safely extracts any key from nameMap
     */
    public static String extractBaseKeyFromNameMap(Map<String, String> nameMap) {
        return nameMap.keySet()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("nameMap is empty"));
    }

    /**
     * Normalizes a string to UPPER_SNAKE_CASE
     */
    private static String normalize(String value) {
        return value
                .trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")  // replace spaces & special chars
                .replaceAll("_+", "_")          // collapse multiple underscores
                .replaceAll("^_|_$", "");       // trim underscores
    }
}
