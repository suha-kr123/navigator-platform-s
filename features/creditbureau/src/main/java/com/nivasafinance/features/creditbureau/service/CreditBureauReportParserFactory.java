package com.nivasafinance.features.creditbureau.service;

public interface CreditBureauReportParserFactory {
    CreditBureauReportParser getParser(String providerName);
}
