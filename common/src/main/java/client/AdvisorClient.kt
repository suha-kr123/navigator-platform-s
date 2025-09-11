package client

import event.AdvisorInfo
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface AdvisorClient {
    fun getAdvisor(id: UUID): CompletableFuture<AdvisorInfo>
    fun validateAdvisor(id: UUID): CompletableFuture<Boolean>
    fun getAdvisorByMobile(mobileNumber: String): CompletableFuture<AdvisorInfo?>
}
