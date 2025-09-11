package client

import event.PaymentInfo
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface PaymentClient {
    fun createPayment(
        advisorId: UUID,
        leadId: UUID,
        amount: BigDecimal,
        paymentType: String,
        description: String?
    ): CompletableFuture<PaymentInfo>
    fun getPayment(id: UUID): CompletableFuture<PaymentInfo>
    fun updatePayment(
        id: UUID,
        amount: BigDecimal?,
        paymentType: String?,
        description: String?
    ): CompletableFuture<PaymentInfo>
    fun deletePayment(id: UUID): CompletableFuture<Boolean>
}
