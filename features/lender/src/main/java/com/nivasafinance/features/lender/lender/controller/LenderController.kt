package com.nivasafinance.features.lender.lender.controller

import com.nivasafinance.features.lender.lender.dto.LenderWithOfficesResponse
import com.nivasafinance.features.lender.lender.enum.LenderStatus
import com.nivasafinance.features.lender.lender.service.LenderReadService
import com.nivasafinance.features.lender.lenderoffice.enum.LenderOfficeStatus
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/lender")
class LenderController(
    private val lenderReadService: LenderReadService,
    private val lenderOfficeReadService: LenderOfficeReadService
) {

    @GetMapping
    fun getAllLendersWithOffices(): List<LenderWithOfficesResponse> {
        val lenders = lenderReadService.getAllByStatus(LenderStatus.ACTIVE)

        return lenders.map { lender ->
            val offices = lenderOfficeReadService.getByLenderKeyAndStatus(lender.key, LenderOfficeStatus.ACTIVE)

            LenderWithOfficesResponse(
                name = lender.name,
                key = lender.key,
                offices = offices
            )
        }
    }
}
