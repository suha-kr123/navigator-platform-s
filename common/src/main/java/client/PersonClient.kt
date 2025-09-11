package client

import event.PersonInfo
import java.util.UUID
import java.util.concurrent.CompletableFuture

interface PersonClient {
    fun getPerson(id: UUID): CompletableFuture<PersonInfo>
    fun getPersonByMobile(mobileNumber: String): CompletableFuture<PersonInfo?>
}
