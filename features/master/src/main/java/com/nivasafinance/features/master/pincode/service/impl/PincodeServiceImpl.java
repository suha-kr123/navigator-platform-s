package com.nivasafinance.features.master.pincode.service.impl;

import com.nivasafinance.common.base.BaseNavigatorService;
import com.nivasafinance.features.master.pincode.dto.PincodeResponse;
import com.nivasafinance.features.master.pincode.exception.PincodeExceptionFactory;
import com.nivasafinance.features.master.pincode.repository.PincodeRepository;
import com.nivasafinance.features.master.pincode.service.PincodeService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PincodeServiceImpl extends BaseNavigatorService implements PincodeService {
    
    private final PincodeRepository pincodeRepository;
    private final MessageSource messageSource;
    private final PincodeExceptionFactory pincodeExceptionFactory;
    
    public PincodeServiceImpl(PincodeRepository pincodeRepository, MessageSource messageSource) {
        this.pincodeRepository = pincodeRepository;
        this.messageSource = messageSource;
        this.pincodeExceptionFactory = new PincodeExceptionFactory(messageSource);
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PincodeResponse getPincodeDetails(String pincode) {
        List<com.nivasafinance.features.master.pincode.entity.Pincode> pincodes = 
                pincodeRepository.findAllByPincode(pincode);
        if (pincodes.isEmpty()) {
            throw pincodeExceptionFactory.notFound(pincode, messageSource);
        }
        List<String> areas = pincodes.stream()
                .map(com.nivasafinance.features.master.pincode.entity.Pincode::getArea)
                .collect(Collectors.toList());
        com.nivasafinance.features.master.pincode.entity.Pincode firstPincode = pincodes.get(0);
        return PincodeResponse.builder()
                .pincode(firstPincode.getPincode())
                .area(areas)
                .district(firstPincode.getDistrict())
                .state(firstPincode.getState())
                .country(firstPincode.getCountry())
                .isServicable(firstPincode.getIsServicable())
                .build();
    }
}

