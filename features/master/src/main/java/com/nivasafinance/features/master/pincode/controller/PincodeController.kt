package com.nivasafinance.features.master.pincode.controller

import com.nivasafinance.features.master.pincode.dto.PincodeResponseDto
import com.nivasafinance.features.master.pincode.service.PincodeReadService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/master/pincode")
class PincodeController(
    private val pincodeReadService: PincodeReadService
) {

    @GetMapping("/{pincode}")
    fun getPincodeDetails(@PathVariable pincode: String): PincodeResponseDto {
        return pincodeReadService.getByPincode(pincode)
    }
}
