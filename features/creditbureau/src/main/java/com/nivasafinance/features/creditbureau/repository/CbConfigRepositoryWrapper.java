package com.nivasafinance.features.creditbureau.repository;

import com.nivasafinance.features.creditbureau.entity.CbConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CbConfigRepositoryWrapper {

    private final CbConfigRepository cbConfigRepository;

    public Optional<CbConfig> findByConfigKey(String configKey) {
        return cbConfigRepository.findByConfigKey(configKey);
    }
}
