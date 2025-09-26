package com.nivasafinance.features.offices.controller

import com.nivasafinance.features.offices.dto.OfficeCreateRequest
import com.nivasafinance.features.offices.dto.OfficeResponse
import com.nivasafinance.features.offices.service.OfficeReadService
import com.nivasafinance.features.offices.service.OfficeWriteService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/v1/offices")
class OfficeController(
    private val officeReadService: OfficeReadService,
    private val officeWriteService: OfficeWriteService
) {

    @PostMapping
    fun createOffice(@RequestBody request: OfficeCreateRequest): ResponseEntity<OfficeResponse> {
        val createdOffice = officeWriteService.createOffice(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOffice)
    }

    @GetMapping("/{id}")
    fun getOffice(@PathVariable id: UUID): ResponseEntity<OfficeResponse> {
        val office = officeReadService.getOffice(id)
        return ResponseEntity.ok(office)
    }
}
