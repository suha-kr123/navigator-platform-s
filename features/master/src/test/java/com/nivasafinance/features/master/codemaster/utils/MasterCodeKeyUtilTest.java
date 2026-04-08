package com.nivasafinance.features.master.codemaster.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MasterCodeKeyUtilTest {

    // ── generateUniqueKey ────────────────────────────────────────────

    @Test
    void generateUniqueKey_noCollision_returnsNormalizedKey() {
        String result = MasterCodeKeyUtil.generateUniqueKey("on hold", "MASTER_CODE", Set.of());

        assertEquals("ON_HOLD_MASTER_CODE", result,
                "Key should be normalized to UPPER_SNAKE_CASE with suffix appended");
    }

    @Test
    void generateUniqueKey_singleCollision_returnsSuffixedKey() {
        Set<String> existing = new HashSet<>(Set.of("ON_HOLD_MASTER_CODE"));

        String result = MasterCodeKeyUtil.generateUniqueKey("on hold", "MASTER_CODE", existing);

        assertEquals("ON_HOLD_MASTER_CODE_1", result,
                "Should append _1 when base key already exists");
    }

    @Test
    void generateUniqueKey_multipleCollisions_incrementsCounter() {
        Set<String> existing = new HashSet<>(Set.of(
                "REASON_MASTER_CODE_VALUE",
                "REASON_MASTER_CODE_VALUE_1",
                "REASON_MASTER_CODE_VALUE_2"));

        String result = MasterCodeKeyUtil.generateUniqueKey("reason", "MASTER_CODE_VALUE", existing);

        assertEquals("REASON_MASTER_CODE_VALUE_3", result,
                "Should skip existing _1 and _2, returning _3");
    }

    @Test
    void generateUniqueKey_nullExistingKeys_returnsBaseKey() {
        String result = MasterCodeKeyUtil.generateUniqueKey("test", "SUFFIX", null);

        assertEquals("TEST_SUFFIX", result,
                "Null existing keys set should be treated as empty, returning base key");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void generateUniqueKey_nullOrBlankBaseKey_throwsIllegalArgument(String baseKey) {
        assertThrows(IllegalArgumentException.class,
                () -> MasterCodeKeyUtil.generateUniqueKey(baseKey, "SUFFIX", Set.of()),
                "Null or blank base key should throw IllegalArgumentException");
    }

    @Test
    void generateUniqueKey_specialCharactersInBaseKey_normalizedToUnderscores() {
        String result = MasterCodeKeyUtil.generateUniqueKey("hello@world#123", "CODE", Set.of());

        assertEquals("HELLO_WORLD_123_CODE", result,
                "Special characters should be replaced by underscores in normalized key");
    }

    @Test
    void generateUniqueKey_leadingTrailingSpaces_trimmedBeforeNormalization() {
        String result = MasterCodeKeyUtil.generateUniqueKey("  loan type  ", "MASTER", Set.of());

        assertEquals("LOAN_TYPE_MASTER", result,
                "Leading and trailing spaces should be trimmed during normalization");
    }

    // ── extractBaseKeyFromNameMap ─────────────────────────────────────

    @Test
    void extractBaseKeyFromNameMap_singleEntry_returnsKey() {
        Map<String, String> nameMap = Map.of("default", "Test Value");

        String result = MasterCodeKeyUtil.extractBaseKeyFromNameMap(nameMap);

        assertNotNull(result, "Should return a non-null key from the name map");
        assertEquals("default", result, "Should return the first key from the map");
    }

    @Test
    void extractBaseKeyFromNameMap_emptyMap_throwsIllegalState() {
        Map<String, String> emptyMap = Map.of();

        assertThrows(IllegalStateException.class,
                () -> MasterCodeKeyUtil.extractBaseKeyFromNameMap(emptyMap),
                "Empty name map should throw IllegalStateException");
    }
}
