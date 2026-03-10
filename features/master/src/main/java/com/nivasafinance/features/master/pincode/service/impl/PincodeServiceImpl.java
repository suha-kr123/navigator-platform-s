package com.nivasafinance.features.master.pincode.service.impl;

import com.nivasafinance.common.base.BaseNavigatorService;
import com.nivasafinance.common.base.model.MasterLanguageResolver;
import com.nivasafinance.features.master.location.entity.Country;
import com.nivasafinance.features.master.location.entity.District;
import com.nivasafinance.features.master.location.entity.State;
import com.nivasafinance.features.master.location.entity.Taluka;
import com.nivasafinance.features.master.location.repository.CountryRepository;
import com.nivasafinance.features.master.location.repository.DistrictRepository;
import com.nivasafinance.features.master.location.repository.StateRepository;
import com.nivasafinance.features.master.location.repository.TalukaRepository;
import com.nivasafinance.features.master.pincode.dto.PincodeResponse;
import com.nivasafinance.features.master.pincode.exception.PincodeExceptionFactory;
import com.nivasafinance.features.master.pincode.repository.PincodeRepository;
import com.nivasafinance.features.master.pincode.service.PincodeService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PincodeServiceImpl extends BaseNavigatorService implements PincodeService {
    
    private final PincodeRepository pincodeRepository;
    private final CountryRepository countryRepository;
    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final TalukaRepository talukaRepository;
    private final MessageSource messageSource;
    private final PincodeExceptionFactory pincodeExceptionFactory;
    
    public PincodeServiceImpl(PincodeRepository pincodeRepository,
                               CountryRepository countryRepository,
                               StateRepository stateRepository,
                               DistrictRepository districtRepository,
                               TalukaRepository talukaRepository,
                               MessageSource messageSource) {
        this.pincodeRepository = pincodeRepository;
        this.countryRepository = countryRepository;
        this.stateRepository = stateRepository;
        this.districtRepository = districtRepository;
        this.talukaRepository = talukaRepository;
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
        com.nivasafinance.features.master.pincode.entity.Pincode firstPincode = pincodes.get(0);
        
        // Fetch location entities by ID when available
        String districtName = null;
        String stateName = null;
        String countryName = null;
        String talukaName = null;
        String districtCode = null;
        String stateCode = null;
        String countryCode = null;
        String talukaCode = null;
        
        // Fetch country if ID is present
        if (firstPincode.getCountryId() != null) {
            Optional<Country> countryOpt = countryRepository.findById(firstPincode.getCountryId());
            if (countryOpt.isPresent()) {
                Country country = countryOpt.get();
                countryName = country.getName();
                countryCode = country.getCode();
            }
        }
        
        // Fetch state if ID is present
        if (firstPincode.getStateId() != null) {
            Optional<State> stateOpt = stateRepository.findById(firstPincode.getStateId());
            if (stateOpt.isPresent()) {
                State state = stateOpt.get();
                stateName = state.getName();
                stateCode = state.getCode();
            }
        }
        
        // Fetch district if ID is present
        if (firstPincode.getDistrictId() != null) {
            Optional<District> districtOpt = districtRepository.findById(firstPincode.getDistrictId());
            if (districtOpt.isPresent()) {
                District district = districtOpt.get();
                districtName = MasterLanguageResolver.getDisplayValue(district.getNameValues());
                districtCode = district.getCode();
            }
        }
        
        // Fetch taluka if ID is present
        if (firstPincode.getTalukaId() != null) {
            Optional<Taluka> talukaOpt = talukaRepository.findById(firstPincode.getTalukaId());
            if (talukaOpt.isPresent()) {
                Taluka taluka = talukaOpt.get();
                talukaName = MasterLanguageResolver.getDisplayValue(taluka.getNameValues());
                talukaCode = taluka.getCode();
            }
        }
        
        return PincodeResponse.builder()
                .pincode(firstPincode.getPincode())
                .district(districtName)
                .state(stateName)
                .country(countryName)
                .taluka(talukaName)
                .districtCode(districtCode)
                .stateCode(stateCode)
                .countryCode(countryCode)
                .talukaCode(talukaCode)
                .districtId(firstPincode.getDistrictId())
                .stateId(firstPincode.getStateId())
                .countryId(firstPincode.getCountryId())
                .talukaId(firstPincode.getTalukaId())
                .isServicable(firstPincode.getIsServicable())
                .build();
    }
}

