package com.nivasafinance.features.lender.lenderoffice.controller

import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData
import com.nivasafinance.features.lender.lenderoffice.enum.LenderOfficeStatus
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/lender/{lenderKey}/office")
class LenderOfficeController(
    private val lenderOfficeReadService: LenderOfficeReadService
) {

    @GetMapping
    fun getOfficesByLender(
        @PathVariable lenderKey: String,
        @RequestParam(defaultValue = "ACTIVE") status: LenderOfficeStatus,
    ): List<LenderOfficeReponseData> {
        return lenderOfficeReadService.getByLenderKeyAndStatus(lenderKey, status)
    }
}
