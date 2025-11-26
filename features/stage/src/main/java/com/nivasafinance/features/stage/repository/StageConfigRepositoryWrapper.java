package com.nivasafinance.features.stage.repository;

import com.nivasafinance.features.stage.entity.StageConfig;
import com.nivasafinance.features.stage.exception.StageConfigNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StageConfigRepositoryWrapper {

    private final StageConfigRepository stageConfigRepository;
    private final MessageSource messageSource;

    public StageConfig findByKeyWithException(String key) {
        return stageConfigRepository.findByKeyAndIsActive(key, true)
                .orElseThrow(() -> StageConfigNotFoundException.stageConfigNotFound(key, messageSource));
    }
    
    public List<StageConfig> findAllActiveStages() {
        return stageConfigRepository.findByIsActiveOrderByNameAsc(true);
    }
}

