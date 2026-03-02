package com.nivasafinance.features.advisor.controller.self;

import com.nivasafinance.common.annotations.RequirePermission;
import com.nivasafinance.common.annotations.RequireRole;
import com.nivasafinance.common.constants.ApiConstants;
import com.nivasafinance.features.advisor.dto.self.*;
import com.nivasafinance.features.advisor.service.self.AdvisorSelfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.SELF_V1 + "/advisors")
@RequiredArgsConstructor
public class AdvisorSelfController {

    private static final String ROLE_ADVISOR_SELF = "ADVISOR_SELF";

    private final AdvisorSelfService advisorSelfService;

    @PatchMapping
    @RequireRole({ROLE_ADVISOR_SELF})
    public ResponseEntity<Void> updateMyProfile(@Valid @RequestBody SelfAdvisorProfileRequest request) {
        advisorSelfService.updateMyProfile(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @RequireRole({ROLE_ADVISOR_SELF})
    @RequirePermission(permissionName = "READ_ADVISOR")
    public ResponseEntity<SelfAdvisorResponse> getMyProfile() {
        SelfAdvisorResponse response = advisorSelfService.getMyProfile();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/address")
    @RequireRole({ROLE_ADVISOR_SELF})
    @RequirePermission(permissionName = "CREATE_ADVISOR_ADDRESS")
    public ResponseEntity<SelfAddAddressResponse> addMyAddress(@Valid @RequestBody SelfAddressRequest request) {
        SelfAddAddressResponse response = advisorSelfService.addMyAddress(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/address/{addressId}")
    @RequireRole({ROLE_ADVISOR_SELF})
    @RequirePermission(permissionName = "UPDATE_ADVISOR_ADDRESS")
    public ResponseEntity<SelfAddressResponse> updateMyAddress(
            @PathVariable String addressId,
            @Valid @RequestBody SelfAddressRequest request) {
        SelfAddressResponse updated = advisorSelfService.updateMyAddress(addressId, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/addresses")
    @RequireRole({ROLE_ADVISOR_SELF})
    @RequirePermission(permissionName = "READ_ADVISOR_ADDRESS")
    public ResponseEntity<List<SelfAddressResponse>> getMyAddresses() {
        List<SelfAddressResponse> addresses = advisorSelfService.getMyAddresses();
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/address/{addressId}")
    @RequireRole({ROLE_ADVISOR_SELF})
    @RequirePermission(permissionName = "READ_ADVISOR_ADDRESS")
    public ResponseEntity<SelfAddressResponse> getMyAddress(@PathVariable String addressId) {
        SelfAddressResponse address = advisorSelfService.getMyAddress(addressId);
        return ResponseEntity.ok(address);
    }

    @PatchMapping("/bank-details")
    @RequireRole({ROLE_ADVISOR_SELF})
    @RequirePermission(permissionName = "CREATE_ADVISOR_BANK_DETAILS")
    public ResponseEntity<SelfAddBankDetailsResponse> addMyBankDetails(@Valid @RequestBody SelfAddBankDetailsRequest request) {
        SelfAddBankDetailsResponse response = advisorSelfService.addMyBankDetails(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/bank-details/{bankIdentifier}")
    @RequireRole({ROLE_ADVISOR_SELF})
    @RequirePermission(permissionName = "UPDATE_ADVISOR_BANK_DETAILS")
    public ResponseEntity<Void> updateMyBankDetails(
            @PathVariable UUID bankIdentifier,
            @Valid @RequestBody SelfBankDetailsRequest request) {
        advisorSelfService.updateMyBankDetails(bankIdentifier, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bank-details")
    @RequireRole({ROLE_ADVISOR_SELF})
    @RequirePermission(permissionName = "READ_ADVISOR_BANK_DETAILS")
    public ResponseEntity<List<SelfBankDetailsResponse>> getMyBankDetails() {
        List<SelfBankDetailsResponse> list = advisorSelfService.getMyBankDetails();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/bank-details/{bankIdentifier}")
    @RequireRole({ROLE_ADVISOR_SELF})
    @RequirePermission(permissionName = "READ_ADVISOR_BANK_DETAILS")
    public ResponseEntity<SelfBankDetailsResponse> getMyBankDetails(@PathVariable UUID bankIdentifier) {
        SelfBankDetailsResponse response = advisorSelfService.getMyBankDetails(bankIdentifier);
        return ResponseEntity.ok(response);
    }

}
