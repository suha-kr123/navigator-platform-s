package com.nivasafinance.features.advisor.controller

import com.nivasafinance.features.advisor.dto.AdvisorDto
import com.nivasafinance.features.advisor.service.AdvisorReadService
import com.nivasafinance.features.advisor.service.AdvisorWriteService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("v1/advisor")
class AdvisorController(
    private val advisorReadService: AdvisorReadService,
    private val advisorWriteService: AdvisorWriteService
) {

    @PostMapping
    fun createAdvisor(@RequestBody @Valid advisorDto: AdvisorDto): ResponseEntity<AdvisorDto> {
        val savedAdvisorDto = advisorWriteService.createAdvisor(advisorDto)
        return ResponseEntity(savedAdvisorDto, HttpStatus.CREATED)
    }

    @GetMapping("/{id}")
    fun getAdvisor(@PathVariable id: UUID): ResponseEntity<AdvisorDto> {
        val advisor = advisorReadService.getAdvisor(id)
        return ResponseEntity.ok(advisor)
    }

    @PutMapping("/{id}")
    fun updateAdvisor(@PathVariable id: UUID, @RequestBody advisorDto: AdvisorDto): ResponseEntity<Unit> {
        advisorWriteService.updateAdvisor(id, advisorDto)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{id}")
    fun deleteAdvisor(@PathVariable id: UUID): ResponseEntity<Unit> {
        advisorWriteService.deleteAdvisor(id)
        return ResponseEntity.noContent().build()
    }
}
