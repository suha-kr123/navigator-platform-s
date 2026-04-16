package com.nivasafinance.features.master.location.service;

import com.nivasafinance.features.master.location.dto.*;

import java.util.List;

public interface LocationMasterService {

    List<CountryResponse> getAllCountries();
    List<StateResponse> getStatesByCountryId(Long countryId);
    List<RegionResponse> getRegionsByStateId(Long stateId);
    List<DistrictResponse> getDistrictsByRegionId(Long regionId);
    List<DistrictResponse> getDistrictsByStateId(Long stateId);
    List<TalukaResponse> getTalukasByDistrictId(Long districtId);
    List<VillageResponse> getVillagesByTalukaId(Long talukaId);
    List<DistrictResponse> getServiceableDistrictsByStateId(Long stateId);
    List<TalukaResponse> getServiceableTalukasByDistrictId(Long districtId);
}

