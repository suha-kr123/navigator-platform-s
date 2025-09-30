package com.nivasafinance.features.lender.lenderoffice.controller

import com.nivasafinance.features.lender.lenderoffice.dto.LenderOfficeReponseData
import com.nivasafinance.features.lender.lenderoffice.service.LenderOfficeReadService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class LenderOfficeController(
    private val lenderOfficeReadService: LenderOfficeReadService
) {

    @GetMapping("/{officeKey}")
    fun getLenderOfficeByKey(
        @PathVariable officeKey: String
    ): LenderOfficeReponseData {
        return lenderOfficeReadService.getByKey(officeKey)
    }
}
