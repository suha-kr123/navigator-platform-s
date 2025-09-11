package com.nivasafinance.features.advisorleadmapping.client.impl

import com.nivasafinance.features.advisorleadmapping.client.PaymentClient
import com.nivasafinance.features.advisorleadmapping.client.PaymentCreateRequest
import com.nivasafinance.features.advisorleadmapping.client.PaymentResponse
import com.nivasafinance.features.advisorleadmapping.client.PaymentUpdateRequest
import event.CreatePaymentRequest
import event.DeletePaymentRequest
import event.EventTopics
import event.GetPaymentRequest
import event.UpdatePaymentRequest
import event.impl.SpringEventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
@Suppress("NoWildcardImports", "NoUnusedImports", "NoTrailingSpaces", "WildcardImport", "VarCouldBeVal")
class PaymentClientImpl : PaymentClient {

    @Autowired
    private lateinit var eventService: SpringEventService

    override fun createPayment(request: PaymentCreateRequest): PaymentResponse {
        val requestId = UUID.randomUUID().toString()
        val correlationId = UUID.randomUUID().toString()

        val eventRequest = CreatePaymentRequest(
            requestId = requestId,
            advisorId = request.advisorId,
            leadId = request.leadId,
            amount = request.amount,
            paymentType = request.paymentType,
            description = request.description,
            correlationId = correlationId
        )

        eventService.publishEvent(eventRequest, EventTopics.PAYMENT_CREATE_REQUEST)

        // In a real implementation, you'd wait for the response event
        // For now, we'll simulate a successful response
        return PaymentResponse(
            id = UUID.randomUUID(),
            advisorId = request.advisorId,
            leadId = request.leadId,
            amount = request.amount,
            paymentType = request.paymentType,
            status = "PENDING",
            description = request.description
        )
    }

    override fun getPayment(id: UUID): PaymentResponse {
        val requestId = UUID.randomUUID().toString()
        val correlationId = UUID.randomUUID().toString()

        val request = GetPaymentRequest(
            requestId = requestId,
            paymentId = id,
            correlationId = correlationId
        )

        eventService.publishEvent(request, EventTopics.PAYMENT_GET_REQUEST)

        // In a real implementation, you'd wait for the response event
        return PaymentResponse(
            id = id,
            advisorId = UUID.randomUUID(),
            leadId = UUID.randomUUID(),
            amount = BigDecimal("5000"),
            paymentType = "COMMISSION",
            status = "COMPLETED",
            description = "Commission payment"
        )
    }

    override fun updatePayment(id: UUID, request: PaymentUpdateRequest): PaymentResponse {
        val requestId = UUID.randomUUID().toString()
        val correlationId = UUID.randomUUID().toString()

        val eventRequest = UpdatePaymentRequest(
            requestId = requestId,
            paymentId = id,
            amount = request.amount,
            paymentType = request.paymentType,
            description = request.description,
            correlationId = correlationId
        )

        eventService.publishEvent(eventRequest, EventTopics.PAYMENT_UPDATE_REQUEST)

        // In a real implementation, you'd wait for the response event
        return PaymentResponse(
            id = id,
            advisorId = UUID.randomUUID(),
            leadId = UUID.randomUUID(),
            amount = request.amount ?: BigDecimal("5000"),
            paymentType = request.paymentType ?: "COMMISSION",
            status = "UPDATED",
            description = request.description
        )
    }

    override fun deletePayment(id: UUID) {
        val requestId = UUID.randomUUID().toString()
        val correlationId = UUID.randomUUID().toString()

        val request = DeletePaymentRequest(
            requestId = requestId,
            paymentId = id,
            correlationId = correlationId
        )

        eventService.publishEvent(request, EventTopics.PAYMENT_DELETE_REQUEST)

        // In a real implementation, you'd wait for the response event
    }
}
