package com.nivasafinance.externals.masters.location.controller;

import com.nivasafinance.features.master.location.dto.CountryResponse;
import com.nivasafinance.features.master.location.dto.DistrictResponse;
import com.nivasafinance.features.master.location.dto.StateResponse;
import com.nivasafinance.features.master.location.dto.TalukaResponse;
import com.nivasafinance.features.master.location.service.LocationMasterService;
import com.nivasafinance.features.master.pincode.dto.PincodeResponse;
import com.nivasafinance.features.master.pincode.service.PincodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationExternalControllerTest {

    @Mock
    private LocationMasterService locationMasterService;

    @Mock
    private PincodeService pincodeService;

    @InjectMocks
    private LocationExternalController controller;

    @Test
    void getCountries_success_returnsOk() {
        List<CountryResponse> countries = List.of(new CountryResponse());
        when(locationMasterService.getAllCountries()).thenReturn(countries);

        ResponseEntity<List<CountryResponse>> result = controller.getCountries();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getStates_success_returnsOk() {
        List<StateResponse> states = List.of(new StateResponse());
        when(locationMasterService.getStatesByCountryId(1L)).thenReturn(states);

        ResponseEntity<List<StateResponse>> result = controller.getStates(1L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
        verify(locationMasterService).getStatesByCountryId(1L);
    }

    @Test
    void getDistricts_success_returnsOk() {
        List<DistrictResponse> districts = List.of(new DistrictResponse());
        when(locationMasterService.getDistrictsByStateId(5L)).thenReturn(districts);

        ResponseEntity<List<DistrictResponse>> result = controller.getDistricts(5L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getTalukas_success_returnsOk() {
        List<TalukaResponse> talukas = List.of(new TalukaResponse());
        when(locationMasterService.getTalukasByDistrictId(10L)).thenReturn(talukas);

        ResponseEntity<List<TalukaResponse>> result = controller.getTalukas(10L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1, result.getBody().size());
    }

    @Test
    void getServiceableDistricts_success_returnsOk() {
        List<DistrictResponse> districts = List.of(new DistrictResponse());
        when(locationMasterService.getServiceableDistrictsByStateId(5L)).thenReturn(districts);

        ResponseEntity<List<DistrictResponse>> result = controller.getServiceableDistricts(5L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(locationMasterService).getServiceableDistrictsByStateId(5L);
    }

    @Test
    void getServiceableTalukas_success_returnsOk() {
        List<TalukaResponse> talukas = List.of(new TalukaResponse());
        when(locationMasterService.getServiceableTalukasByDistrictId(10L)).thenReturn(talukas);

        ResponseEntity<List<TalukaResponse>> result = controller.getServiceableTalukas(10L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(locationMasterService).getServiceableTalukasByDistrictId(10L);
    }

    @Test
    void getPincodeDetails_success_returnsOk() {
        PincodeResponse pincodeResponse = PincodeResponse.builder()
                .pincode("560001")
                .district("Bangalore Urban")
                .state("Karnataka")
                .country("India")
                .build();
        when(pincodeService.getPincodeDetails("560001")).thenReturn(pincodeResponse);

        ResponseEntity<PincodeResponse> result = controller.getPincodeDetails("560001");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("560001", result.getBody().getPincode());
        assertEquals("Bangalore Urban", result.getBody().getDistrict());
    }

    @Test
    void getCountries_emptyList_returnsOkWithEmptyBody() {
        when(locationMasterService.getAllCountries()).thenReturn(List.of());

        ResponseEntity<List<CountryResponse>> result = controller.getCountries();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertTrue(result.getBody().isEmpty());
    }
}
