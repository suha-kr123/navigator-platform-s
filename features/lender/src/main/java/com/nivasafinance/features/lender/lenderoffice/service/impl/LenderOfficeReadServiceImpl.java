package com.nivasafinance.features.lender.lenderoffice.service.impl;

import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData;
import com.nivasafinance.features.lender.lenderoffice.entity.LenderOffice;
import com.nivasafinance.features.lender.lenderoffice.enums.LenderOfficeStatus;
import com.nivasafinance.features.lender.lenderoffice.repository.LenderOfficeRepositoryWrapper;
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LenderOfficeReadServiceImpl implements LenderOfficeReadService {

    private final LenderOfficeRepositoryWrapper lenderOfficeRepositoryWrapper;

    @Autowired
    public LenderOfficeReadServiceImpl(LenderOfficeRepositoryWrapper lenderOfficeRepositoryWrapper) {
        this.lenderOfficeRepositoryWrapper = lenderOfficeRepositoryWrapper;
    }

    @Override
    public LenderOfficeReponseData getByKey(String key) {
        LenderOffice lenderOffice = lenderOfficeRepositoryWrapper.findByKeyWithException(key);
        return toResponse(lenderOffice);
    }

    @Override
    public LenderOfficeReponseData getById(UUID id) {
        LenderOffice lenderOffice = lenderOfficeRepositoryWrapper.findByIdWithException(id);
        return toResponse(lenderOffice);
    }

    @Override
    public List<LenderOfficeReponseData> getByLenderKeyAndStatus(String lenderKey, LenderOfficeStatus status) {
        return lenderOfficeRepositoryWrapper.findByLenderKeyAndStatus(lenderKey, status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private LenderOfficeReponseData toResponse(LenderOffice lenderOffice) {
        if (lenderOffice.getId() == null) {
            throw new IllegalStateException("Lender office ID cannot be null");
        }
        // Unwrap AddressData from nested structure
        com.nivasafinance.common.dto.AddressData addressData = null;
        if (lenderOffice.getAddressDetails() != null) {
            addressData = lenderOffice.getAddressDetails().getAddress();
        }
        return new LenderOfficeReponseData(
                lenderOffice.getId(),
                lenderOffice.getName(),
                lenderOffice.getKey(),
                lenderOffice.getLenderKey(),
                addressData
        );
    }
}

