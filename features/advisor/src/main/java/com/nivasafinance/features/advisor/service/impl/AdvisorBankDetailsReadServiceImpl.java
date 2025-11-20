package com.nivasafinance.features.advisor.service.impl;

import com.nivasafinance.features.advisor.dto.BankDetails;
import com.nivasafinance.features.advisor.dto.BankDetailsResponse;
import com.nivasafinance.features.advisor.entity.Advisor;
import com.nivasafinance.features.advisor.repository.AdvisorRepositoryWrapper;
import com.nivasafinance.features.advisor.service.AdvisorBankDetailsReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdvisorBankDetailsReadServiceImpl implements AdvisorBankDetailsReadService {

    private final AdvisorRepositoryWrapper advisorRepositoryWrapper;

    @Override
    public List<BankDetailsResponse> getAllBankDetails(UUID advisorIdentifier) {
        Advisor advisor = advisorRepositoryWrapper.findByIdentifierWithException(advisorIdentifier);

        if (advisor.getBankDetails() == null) {
            return new ArrayList<>();
        }

        return advisor.getBankDetails().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BankDetailsResponse mapToResponse(BankDetails bankDetails) {
        return BankDetailsResponse.builder()
                .bankIdentifier(bankDetails.getBankIdentifier())
                .isPrimary(bankDetails.getIsPrimary())
                .nameAsPerPassbook(bankDetails.getNameAsPerPassbook())
                .accountNo(bankDetails.getAccountNo())
                .bankName(bankDetails.getBankName())
                .ifscCode(bankDetails.getIfscCode())
                .upid(bankDetails.getUpid())
                .status(bankDetails.getStatus())
                .build();
    }
}


