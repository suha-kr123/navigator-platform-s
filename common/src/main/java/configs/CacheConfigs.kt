package configs

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableCaching
class CacheConfigs(val mapper: ObjectMapper) {

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val myMapper = mapper.copy()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .activateDefaultTyping(
                jacksonObjectMapper().polymorphicTypeValidator,
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
            )
        val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    StringRedisSerializer()
                )
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer(myMapper)
                )
            )
            .disableCachingNullValues()
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(redisCacheConfiguration)
            .withCacheConfiguration("advisor_lead_mapping", redisCacheConfiguration)
            .withCacheConfiguration("payment", redisCacheConfiguration)
            .withCacheConfiguration("advisor", redisCacheConfiguration)
            .withCacheConfiguration("advisor_wrapper", redisCacheConfiguration)
            .withCacheConfiguration("applicant", redisCacheConfiguration)
            .withCacheConfiguration("applicant_wrapper", redisCacheConfiguration)
            .withCacheConfiguration("person", redisCacheConfiguration)
            .withCacheConfiguration("address", redisCacheConfiguration)
            .withCacheConfiguration("pincode", redisCacheConfiguration)
            .withCacheConfiguration("leads", redisCacheConfiguration)
            .withCacheConfiguration("person_addresses", redisCacheConfiguration)
            .build()
    }
}
