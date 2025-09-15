package configs

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisStreamsHealthIndicator(
    private val redisTemplate: RedisTemplate<String, Any>
) : HealthIndicator {

    override fun health(): Health {
        return try {
            redisTemplate.opsForValue().set("health:check", "ok")
            val result = redisTemplate.opsForValue().get("health:check")
            if (result == "ok") {
                Health.up()
                    .withDetail("redis", "Available")
                    .withDetail("streams", "Ready")
                    .build()
            } else {
                Health.down()
                    .withDetail("redis", "Connection failed")
                    .build()
            }
        } catch (e: Exception) {
            Health.down()
                .withDetail("redis", "Connection failed")
                .withDetail("error", e.message)
                .build()
        }
    }
}
