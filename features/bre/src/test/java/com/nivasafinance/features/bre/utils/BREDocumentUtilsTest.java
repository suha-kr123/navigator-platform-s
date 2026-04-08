package com.nivasafinance.features.bre.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BREDocumentUtilsTest {

    // ── generateDocumentPathForRuleFile ──

    @Test
    void generateDocumentPathForRuleFile_withValidFileName_returnsExpectedPathStructure() {
        String result = BREDocumentUtils.generateDocumentPathForRuleFile(1L, "my-rule.json");

        assertTrue(result.startsWith("bre/1/rules/"),
                "Path should start with bre/{configId}/rules/");
        assertTrue(result.endsWith("_my-rule.json"),
                "Path should end with sanitised file name");
    }

    @Test
    void generateDocumentPathForRuleFile_withSpecialCharsInFileName_sanitisesCharacters() {
        String result = BREDocumentUtils.generateDocumentPathForRuleFile(5L, "rule file (v2).json");

        assertTrue(result.startsWith("bre/5/rules/"),
                "Path should start with bre/{configId}/rules/");
        assertFalse(result.contains(" "),
                "Sanitised path should not contain spaces");
        assertFalse(result.contains("("),
                "Sanitised path should not contain parentheses");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void generateDocumentPathForRuleFile_withNullOrEmptyFileName_defaultsToRuleJson(String fileName) {
        String result = BREDocumentUtils.generateDocumentPathForRuleFile(3L, fileName);

        assertTrue(result.startsWith("bre/3/rules/"),
                "Path should start with bre/{configId}/rules/");
        assertTrue(result.endsWith("_rule.json"),
                "Should default to 'rule.json' when file name is null or empty");
    }

    @Test
    void generateDocumentPathForRuleFile_withWhitespaceOnlyFileName_defaultsToRuleJson() {
        String result = BREDocumentUtils.generateDocumentPathForRuleFile(4L, "   ");

        assertTrue(result.endsWith("_rule.json"),
                "Should default to 'rule.json' when file name is whitespace only");
    }

    @Test
    void generateDocumentPathForRuleFile_withWhitespaceInFileName_replacesWithUnderscore() {
        String result = BREDocumentUtils.generateDocumentPathForRuleFile(6L, "my rule file.json");

        assertTrue(result.endsWith("_my_rule_file.json"),
                "Whitespace in file name should be replaced with underscores");
    }
}
