package com.nivasafinance.features.master.location.controller;

import com.nivasafinance.features.master.location.dto.*;
import com.nivasafinance.features.master.location.service.LocationMasterService;
import com.nivasafinance.common.annotations.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LocationMasterController {

    private final LocationMasterService locationMasterService;

    @GetMapping("/countries")
    @RequirePermission(permissionName = "READ_MASTER_LOCATION")
    public ResponseEntity<List<CountryResponse>> getAllCountries() {
        List<CountryResponse> countries = locationMasterService.getAllCountries();
        return ResponseEntity.ok(countries);
    }

    @GetMapping("/countries/{countryId}/states")
    @RequirePermission(permissionName = "READ_MASTER_LOCATION")
    public ResponseEntity<List<StateResponse>> getStatesByCountryId(@PathVariable Long countryId) {
        List<StateResponse> states = locationMasterService.getStatesByCountryId(countryId);
        return ResponseEntity.ok(states);
    }

    @GetMapping("/states/{stateId}/regions")
    @RequirePermission(permissionName = "READ_MASTER_LOCATION")
    public ResponseEntity<List<RegionResponse>> getRegionsByStateId(@PathVariable Long stateId) {
        List<RegionResponse> regions = locationMasterService.getRegionsByStateId(stateId);
        return ResponseEntity.ok(regions);
    }

    @GetMapping("/states/{stateId}/districts")
    @RequirePermission(permissionName = "READ_MASTER_LOCATION")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByStateId(@PathVariable Long stateId) {
        List<DistrictResponse> districts = locationMasterService.getDistrictsByStateId(stateId);
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/regions/{regionId}/districts")
    @RequirePermission(permissionName = "READ_MASTER_LOCATION")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByRegionId(@PathVariable Long regionId) {
        List<DistrictResponse> districts = locationMasterService.getDistrictsByRegionId(regionId);
        return ResponseEntity.ok(districts);
    }

    @GetMapping("/districts/{districtId}/talukas")
    @RequirePermission(permissionName = "READ_MASTER_LOCATION")
    public ResponseEntity<List<TalukaResponse>> getTalukasByDistrictId(@PathVariable Long districtId) {
        List<TalukaResponse> talukas = locationMasterService.getTalukasByDistrictId(districtId);
        return ResponseEntity.ok(talukas);
    }

    @GetMapping("/talukas/{talukaId}/villages")
    @RequirePermission(permissionName = "READ_MASTER_LOCATION")
    public ResponseEntity<List<VillageResponse>> getVillagesByTalukaId(@PathVariable Long talukaId) {
        List<VillageResponse> villages = locationMasterService.getVillagesByTalukaId(talukaId);
        return ResponseEntity.ok(villages);
    }
}

