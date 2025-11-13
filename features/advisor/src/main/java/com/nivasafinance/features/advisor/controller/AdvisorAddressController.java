package com.nivasafinance.features.advisor.controller;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.advisor.dto.AddressIdentifierResponse;
import com.nivasafinance.features.advisor.service.AdvisorAddressReadService;
import com.nivasafinance.features.advisor.service.AdvisorAddressWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.V1 + "/advisors")
@RequiredArgsConstructor
public class AdvisorAddressController {

    private final AdvisorAddressWriteService advisorAddressWriteService;
    private final AdvisorAddressReadService advisorAddressReadService;

    @PostMapping("/{identifier}/address")
    public ResponseEntity<AddressIdentifierResponse> addAddress(
            @PathVariable UUID identifier,
            @Valid @RequestBody AddressRequest request) {
        String addressId = advisorAddressWriteService.addAddress(identifier, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AddressIdentifierResponse(addressId));
    }

    @GetMapping("/{identifier}/addresses")
    public ResponseEntity<List<AddressData>> getAddresses(@PathVariable UUID identifier) {
        List<AddressData> addresses = advisorAddressReadService.getAddresses(identifier);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{identifier}/address/{addressId}")
    public ResponseEntity<AddressData> getAddress(
            @PathVariable UUID identifier,
            @PathVariable String addressId) {
        AddressData address = advisorAddressReadService.getAddress(identifier, addressId);
        return ResponseEntity.ok(address);
    }

    @PutMapping("/{identifier}/address/{addressId}")
    public ResponseEntity<Void> updateAddress(
            @PathVariable UUID identifier,
            @PathVariable String addressId,
            @Valid @RequestBody AddressRequest request) {
        advisorAddressWriteService.updateAddress(identifier, addressId, request);
        return ResponseEntity.noContent().build();
    }

}


