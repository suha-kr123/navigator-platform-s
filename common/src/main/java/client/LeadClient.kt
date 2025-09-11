package client

import event.LeadInfo
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface LeadClient {
    fun getLead(id: UUID): CompletableFuture<LeadInfo>
    fun validateLead(id: UUID): CompletableFuture<Boolean>
}
