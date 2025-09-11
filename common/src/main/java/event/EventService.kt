package event

import java.util.concurrent.CompletableFuture

interface EventService {
    fun <T> publishEvent(
        event: T,
        topic: String
    ): CompletableFuture<Unit>

    fun <T> publishEventAndWait(
        event: T,
        topic: String,
        responseType: Class<T>,
        timeoutMs: Long = 5000
    ): CompletableFuture<T>
}
