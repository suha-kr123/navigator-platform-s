package com.nivasafinance.features.creditbureau.service.impl;

import com.nivasafinance.features.creditbureau.service.CreditBureauReportParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CreditBureauReportParserFactoryImplTest {

    @Mock
    private CrifReportParser crifReportParser;

    private CreditBureauReportParserFactoryImpl parserFactory;

    @BeforeEach
    void setUp() {
        Set<CreditBureauReportParser> parsers = Set.of(crifReportParser);
        parserFactory = new CreditBureauReportParserFactoryImpl(parsers);
        ReflectionTestUtils.invokeMethod(parserFactory, "initializeParserMap");
    }

    // ==================== getParser() Tests ====================

    @Test
    void getParser_withNullProviderName_throwsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> parserFactory.getParser(null),
                "Should throw IllegalArgumentException for null provider name");
    }

    @Test
    void getParser_withEmptyProviderName_throwsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> parserFactory.getParser(""),
                "Should throw IllegalArgumentException for empty provider name");
    }

    @Test
    void getParser_withBlankProviderName_throwsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> parserFactory.getParser("   "),
                "Should throw IllegalArgumentException for blank provider name");
    }

    @Test
    void getParser_withCrifHighmark_returnsCrifParser() {
        // Act
        CreditBureauReportParser result = parserFactory.getParser("CRIF_HIGHMARK");

        // Assert
        assertNotNull(result, "Should return a parser for CRIF_HIGHMARK");
        assertSame(crifReportParser, result, "Should return the registered CRIF parser instance");
    }

    @Test
    void getParser_withCrif_returnsCrifParser() {
        // Act
        CreditBureauReportParser result = parserFactory.getParser("CRIF");

        // Assert
        assertNotNull(result, "Should return a parser for CRIF");
        assertSame(crifReportParser, result, "Should return the registered CRIF parser instance");
    }

    @Test
    void getParser_withLowercaseProviderName_normalizesAndReturnsCrifParser() {
        // Act
        CreditBureauReportParser result = parserFactory.getParser("crif_highmark");

        // Assert
        assertNotNull(result, "Should normalize provider name to uppercase and find parser");
        assertSame(crifReportParser, result, "Should return the registered CRIF parser instance");
    }

    @Test
    void getParser_withUnknownProvider_fallsBackToCrifHighmark() {
        // Act
        CreditBureauReportParser result = parserFactory.getParser("EQUIFAX");

        // Assert
        assertNotNull(result, "Should fall back to CRIF_HIGHMARK parser for unknown provider");
        assertSame(crifReportParser, result, "Should return CRIF_HIGHMARK parser as fallback");
    }

    @Test
    void getParser_withUnknownProviderAndNoCrifRegistered_throwsIllegalArgumentException() {
        // Arrange
        CreditBureauReportParserFactoryImpl emptyFactory =
                new CreditBureauReportParserFactoryImpl(Collections.emptySet());
        ReflectionTestUtils.invokeMethod(emptyFactory, "initializeParserMap");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> emptyFactory.getParser("EQUIFAX"),
                "Should throw when no parser is registered and provider is unknown");
    }
}
