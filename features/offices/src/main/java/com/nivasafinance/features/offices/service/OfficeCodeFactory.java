package com.nivasafinance.features.offices.service;

import java.util.UUID;

public interface OfficeCodeFactory {
    String generateOfficeCode(UUID parentId);
}

