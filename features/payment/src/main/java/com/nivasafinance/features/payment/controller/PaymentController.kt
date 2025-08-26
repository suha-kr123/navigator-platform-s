package com.nivasafinance.features.payment.controller

import com.nivasafinance.features.payment.dto.PaymentCreateRequest
import com.nivasafinance.features.payment.dto.PaymentResponse
import com.nivasafinance.features.payment.dto.PaymentUpdateRequest
import com.nivasafinance.features.payment.service.PaymentService
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
@RequestMapping("/v1/payments")
class PaymentController(
    private val paymentService: PaymentService
) {

    @GetMapping("/{id}")
    fun getPayment(@PathVariable id: UUID): ResponseEntity<PaymentResponse> {
        val payment = paymentService.getPayment(id)
        return ResponseEntity.ok(payment)
    }

    @PostMapping
    fun createPayment(@RequestBody request: PaymentCreateRequest): ResponseEntity<PaymentResponse> {
        val payment = paymentService.createPayment(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(payment)
    }

    @PutMapping("/{id}")
    fun updatePayment(
        @PathVariable id: UUID,
        @RequestBody request: PaymentUpdateRequest
    ): ResponseEntity<PaymentResponse> {
        val payment = paymentService.updatePayment(id, request)
        return ResponseEntity.ok(payment)
    }

    @DeleteMapping("/{id}")
    fun deletePayment(@PathVariable id: UUID): ResponseEntity<Unit> {
        paymentService.deletePayment(id)
        return ResponseEntity.noContent().build()
    }
}
