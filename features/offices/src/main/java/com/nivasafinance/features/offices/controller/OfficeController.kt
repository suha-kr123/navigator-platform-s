package com.nivasafinance.features.offices.controller

import com.nivasafinance.features.offices.dto.OfficeResponse
import com.nivasafinance.features.offices.service.OfficeService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/offices")
class OfficeController(
    private val officeService: OfficeService
) {

    @GetMapping("/{id}")
    fun getOffice(@PathVariable id: UUID): ResponseEntity<OfficeResponse> {
        val office = officeService.getOffice(id)
        return ResponseEntity.ok(office)
    }
}
