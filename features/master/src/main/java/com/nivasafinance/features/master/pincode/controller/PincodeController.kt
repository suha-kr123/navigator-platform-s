package com.nivasafinance.features.master.pincode.controller

import com.nivasafinance.features.master.pincode.dto.PincodeResponse
import com.nivasafinance.features.master.pincode.service.PincodeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/pincodes")
class PincodeController(
    private val pincodeService: PincodeService
) {

    @GetMapping("/{pincode}")
    fun getPincodeDetails(@PathVariable pincode: String): ResponseEntity<PincodeResponse> {
        val pincodes = pincodeService.getPincodeDetails(pincode)
        return ResponseEntity.ok(pincodes)
    }
}
