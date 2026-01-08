package com.nivasafinance.features.master.pincode.controller;

import com.nivasafinance.features.master.pincode.dto.PincodeResponse;
import com.nivasafinance.features.master.pincode.service.PincodeService;
import com.nivasafinance.common.annotations.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/pincodes")
@RequiredArgsConstructor
public class PincodeController {
    
    private final PincodeService pincodeService;
    
    @GetMapping("/{pincode}")
    @RequirePermission(permissionName = "READ_MASTER_PINCODES")
    public ResponseEntity<PincodeResponse> getPincodeDetails(@PathVariable String pincode) {
        PincodeResponse pincodes = pincodeService.getPincodeDetails(pincode);
        return ResponseEntity.ok(pincodes);
    }
}

