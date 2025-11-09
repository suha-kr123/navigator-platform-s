package com.nivasafinance.features.leadactivity.service.impl;

import com.nivasafinance.features.leadactivity.entity.LeadActivity;
import com.nivasafinance.features.leadactivity.repository.LeadActivityRepositoryWrapper;
import com.nivasafinance.features.leadactivity.service.LeadActivityWriteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class LeadActivityWriteServiceImpl implements LeadActivityWriteService {
    private final LeadActivityRepositoryWrapper leadActivityRepositoryWrapper;
}


