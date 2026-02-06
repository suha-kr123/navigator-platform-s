package com.nivasafinance.features.creditbureau.service.impl;

import com.nivasafinance.features.creditbureau.service.CreditBureauReportParser;
import com.nivasafinance.features.creditbureau.service.CreditBureauReportParserFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditBureauReportParserFactoryImpl implements CreditBureauReportParserFactory {

    private final Set<CreditBureauReportParser> parsers;
    private Map<String, CreditBureauReportParser> parserMap = new HashMap<>();

    @PostConstruct
    private void initializeParserMap() {
        for (CreditBureauReportParser parser : parsers) {
            if (parser instanceof CrifReportParser) {
                parserMap.put("CRIF_HIGHMARK", parser);
                parserMap.put("CRIF", parser);
            }
        }
    }

    @Override
    public CreditBureauReportParser getParser(String providerName) {
        if (providerName == null || providerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Provider name cannot be null or empty");
        }
        
        String normalizedProviderName = providerName.toUpperCase();
        CreditBureauReportParser parser = parserMap.get(normalizedProviderName);
        
        if (parser == null) {
            log.warn("No parser found for provider: {}, using default CRIF parser", providerName);
            parser = parserMap.get("CRIF_HIGHMARK");
        }
        
        if (parser == null) {
            throw new IllegalArgumentException("No credit bureau report parser available for provider: " + providerName);
        }
        
        return parser;
    }
}
