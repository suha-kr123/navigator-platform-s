package com.nivasafinance.externals.masters.location.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.master.location.dto.*;
import com.nivasafinance.features.master.location.service.LocationMasterService;
import com.nivasafinance.features.master.pincode.dto.PincodeResponse;
import com.nivasafinance.features.master.pincode.service.PincodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.OPEN_API_V1 + "/masters")
@RequiredArgsConstructor
public class LocationExternalController {

    private final LocationMasterService locationMasterService;
    private final PincodeService pincodeService;

    @GetMapping("/countries")
    public ResponseEntity<List<CountryResponse>> getCountries() {
        List<CountryResponse> countries = locationMasterService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/states/{countryId}")
    public ResponseEntity<List<StateResponse>> getStates(@PathVariable Long countryId) {
        List<StateResponse> states = locationMasterService.getStatesByCountryId(countryId);
        return ResponseEntity.ok(states);
    }

    @GetMapping("/states/{stateId}/regions")
    public ResponseEntity<List<RegionResponse>> getRegions(@PathVariable Long stateId) {
        List<RegionResponse> regions = locationMasterService.getRegionsByStateId(stateId);
        return ResponseEntity.ok(regions);
    }

    @GetMapping("/states/{stateId}/districts")
    public ResponseEntity<List<DistrictResponse>> getDistricts(@PathVariable Long stateId) {
        List<DistrictResponse> districts = locationMasterService.getDistrictsByStateId(stateId);
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/regions/{regionId}/districts")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByRegionId(@PathVariable Long regionId) {
        List<DistrictResponse> districts = locationMasterService.getDistrictsByRegionId(regionId);
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/districts/{districtId}/talukas")
    public ResponseEntity<List<TalukaResponse>> getTalukas(@PathVariable Long districtId) {
        List<TalukaResponse> talukas = locationMasterService.getTalukasByDistrictId(districtId);
        return ResponseEntity.ok(talukas);
    }

    @GetMapping("/serviceable/districts/{stateId}")
    public ResponseEntity<List<DistrictResponse>> getServiceableDistricts(@PathVariable Long stateId) {
        List<DistrictResponse> districts = locationMasterService.getServiceableDistrictsByStateId(stateId);
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/serviceable/talukas/{districtId}")
    public ResponseEntity<List<TalukaResponse>> getServiceableTalukas(@PathVariable Long districtId) {
        List<TalukaResponse> talukas = locationMasterService.getServiceableTalukasByDistrictId(districtId);
        return ResponseEntity.ok(talukas);
    }

    @GetMapping("/pincodes/{pincode}")
    public ResponseEntity<PincodeResponse> getPincodeDetails(@PathVariable String pincode) {
        PincodeResponse response = pincodeService.getPincodeDetails(pincode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/regions/{regionId}/operating-areas")
    public ResponseEntity<List<OperatingAreaResponse>> getOperatingAreas(@PathVariable Long regionId) {
        List<OperatingAreaResponse> operatingAreas = locationMasterService.getOperatingAreasByRegionId(regionId);
        return ResponseEntity.ok(operatingAreas);
    }
}
