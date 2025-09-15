package event.impl

import event.EventService
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture

@Service
class RedisStreamsEventService(
    private val redisTemplate: RedisTemplate<String, Any>
) : EventService {

    override fun <T> publishEvent(
        event: T,
        topic: String
    ): CompletableFuture<Unit> {
        return CompletableFuture.runAsync {
            val streamKey = "stream:$topic"
            val eventMap = mapOf(
                "event" to event,
                "timestamp" to System.currentTimeMillis()
            )
            redisTemplate.opsForStream<String, Any>().add(streamKey, eventMap)
        }.thenApply { Unit }
    }

    override fun <T> publishEventAndWait(
        event: T,
        topic: String,
        responseType: Class<T>,
        timeoutMs: Long
    ): CompletableFuture<T> {
        return CompletableFuture.supplyAsync {
            val streamKey = "stream:$topic"
            @Suppress("UNCHECKED_CAST")
            val eventMap = mapOf(
                "event" to event,
                "timestamp" to System.currentTimeMillis(),
                "correlationId" to (event as? Map<String, Any>)?.get("correlationId")
            )
            
            val recordId = redisTemplate.opsForStream<String, Any>().add(streamKey, eventMap)
            
            val responseStreamKey = "stream:$topic:response"
            val startTime = System.currentTimeMillis()
            
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                val responses = redisTemplate.opsForStream<String, Any>()
                    .read(responseType, org.springframework.data.redis.connection.stream.StreamReadOptions.empty().count(1), org.springframework.data.redis.connection.stream.StreamOffset.create(responseStreamKey, org.springframework.data.redis.connection.stream.ReadOffset.lastConsumed()))
                
                responses?.let { responseList ->
                    for (response in responseList) {
                        @Suppress("UNCHECKED_CAST")
                        val responseEvent = (response.value as? Map<String, Any>)?.get("event") as? Map<String, Any>
                        val responseCorrelationId = responseEvent?.get("correlationId")
                        @Suppress("UNCHECKED_CAST")
                        val eventCorrelationId = (event as? Map<String, Any>)?.get("correlationId")
                        
                        if (responseCorrelationId == eventCorrelationId) {
                            @Suppress("UNCHECKED_CAST")
                            return@supplyAsync responseEvent as T
                        }
                    }
                }
                
                Thread.sleep(100)
            }
            
            throw RuntimeException("Timeout waiting for response")
        }
    }
}