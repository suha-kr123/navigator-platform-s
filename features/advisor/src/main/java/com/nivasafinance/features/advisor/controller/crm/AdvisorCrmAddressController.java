package com.nivasafinance.features.advisor.controller.crm;

import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.common.dto.AddressData;
import com.nivasafinance.common.dto.AddressRequest;
import com.nivasafinance.features.advisor.dto.AddressIdentifierResponse;
import com.nivasafinance.features.advisor.service.AdvisorAddressReadService;
import com.nivasafinance.features.advisor.service.AdvisorAddressWriteService;
import com.nivasafinance.common.annotations.RequirePermission;
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
public class AdvisorCrmAddressController {

    private final AdvisorAddressWriteService advisorAddressWriteService;
    private final AdvisorAddressReadService advisorAddressReadService;

    @PostMapping("/{identifier}/address")
    @RequirePermission(permissionName = "CREATE_ADVISOR_ADDRESS")
    public ResponseEntity<AddressIdentifierResponse> addAddress(
            @PathVariable UUID identifier,
            @Valid @RequestBody AddressRequest request) {
        String addressId = advisorAddressWriteService.addAddress(identifier, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AddressIdentifierResponse(addressId));
    }

    @GetMapping("/{identifier}/addresses")
    @RequirePermission(permissionName = "READ_ADVISOR_ADDRESS")
    public ResponseEntity<List<AddressData>> getAddresses(@PathVariable UUID identifier) {
        List<AddressData> addresses = advisorAddressReadService.getAddresses(identifier);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{identifier}/address/{addressId}")
    @RequirePermission(permissionName = "READ_ADVISOR_ADDRESS")
    public ResponseEntity<AddressData> getAddress(
            @PathVariable UUID identifier,
            @PathVariable String addressId) {
        AddressData address = advisorAddressReadService.getAddress(identifier, addressId);
        return ResponseEntity.ok(address);
    }

    @PutMapping("/{identifier}/address/{addressId}")
    @RequirePermission(permissionName = "UPDATE_ADVISOR_ADDRESS")
    public ResponseEntity<Void> updateAddress(
            @PathVariable UUID identifier,
            @PathVariable String addressId,
            @Valid @RequestBody AddressRequest request) {
        advisorAddressWriteService.updateAddress(identifier, addressId, request);
        return ResponseEntity.noContent().build();
    }

}


