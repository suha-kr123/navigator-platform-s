package event.impl

import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.StreamReadOptions
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisStreamListener(
    private val redisTemplate: RedisTemplate<String, Any>
) {

    fun <T> listenToStream(
        streamKey: String,
        consumerGroup: String,
        consumerName: String,
        handler: (T) -> Unit,
        eventType: Class<T>
    ) {
        try {
            redisTemplate.opsForStream<String, Any>().createGroup(streamKey, consumerGroup)
        } catch (e: Exception) {
        }

        while (true) {
            try {
                val readOptions = StreamReadOptions.empty()
                    .count(1)
                    .block(Duration.ofSeconds(1))

                val records = redisTemplate.opsForStream<String, Any>()
                    .read(org.springframework.data.redis.connection.stream.Consumer.from(consumerGroup, consumerName), readOptions, org.springframework.data.redis.connection.stream.StreamOffset.create(streamKey, org.springframework.data.redis.connection.stream.ReadOffset.lastConsumed()))

                records?.let { recordList ->
                    for (record in recordList) {
                        @Suppress("UNCHECKED_CAST")
                        val event = record.value["event"] as? T
                        if (event != null) {
                            handler(event)
                            redisTemplate.opsForStream<String, Any>()
                                .acknowledge(consumerGroup, record)
                        }
                    }
                }
            } catch (e: Exception) {
                Thread.sleep(1000)
            }
        }
    }

    fun <T> listenToStreamWithResponse(
        streamKey: String,
        responseStreamKey: String,
        consumerGroup: String,
        consumerName: String,
        handler: (T) -> Any?,
        eventType: Class<T>
    ) {
        try {
            redisTemplate.opsForStream<String, Any>().createGroup(streamKey, consumerGroup)
        } catch (e: Exception) {
        }

        while (true) {
            try {
                val readOptions = StreamReadOptions.empty()
                    .count(1)
                    .block(Duration.ofSeconds(1))

                val records = redisTemplate.opsForStream<String, Any>()
                    .read(org.springframework.data.redis.connection.stream.Consumer.from(consumerGroup, consumerName), readOptions, org.springframework.data.redis.connection.stream.StreamOffset.create(streamKey, org.springframework.data.redis.connection.stream.ReadOffset.lastConsumed()))

                records?.let { recordList ->
                    for (record in recordList) {
                        @Suppress("UNCHECKED_CAST")
                        val event = record.value["event"] as? T
                        if (event != null) {
                            val response = handler(event)
                            if (response != null) {
                                val responseMap = mapOf(
                                    "event" to response,
                                    "timestamp" to System.currentTimeMillis()
                                )
                                redisTemplate.opsForStream<String, Any>()
                                    .add(responseStreamKey, responseMap)
                            }
                            redisTemplate.opsForStream<String, Any>()
                                .acknowledge(consumerGroup, record)
                        }
                    }
                }
            } catch (e: Exception) {
                Thread.sleep(1000)
            }
        }
    }
}