package com.nivasafinance.common.configs;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@Slf4j
public class RedisCacheConfig implements CachingConfigurer {

    @Value("${redis.cache.ttl.hours:1}")
    private long cacheTtlHours;

    private RedisConnectionFactory redisConnectionFactory;
    private ObjectMapper objectMapper;

    public RedisCacheConfig(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.objectMapper = objectMapper;
    }

    @Bean
    @Primary
    @Override
    public CacheManager cacheManager() {
        log.info("Configuring Redis CacheManager with JSON serialization (TTL: {} hours)", cacheTtlHours);
        
        // Create a copy of the existing ObjectMapper to avoid modifying the shared instance
        // The existing ObjectMapper already has JavaTimeModule configured
        ObjectMapper redisObjectMapper = this.objectMapper.copy();
        
        // Ensure JavaTimeModule is registered (should already be there, but ensure it)
        if (!redisObjectMapper.getRegisteredModuleIds().contains(JavaTimeModule.class.getName())) {
            redisObjectMapper.registerModule(new JavaTimeModule());
        }
        redisObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        redisObjectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        
        // Enable type information for proper deserialization
        // This ensures objects are deserialized to their original types instead of LinkedHashMap
        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        
        log.debug("Redis ObjectMapper configured with JavaTimeModule: {}, Type info enabled: true", 
                redisObjectMapper.getRegisteredModuleIds());
        
        // GenericJackson2JsonRedisSerializer with custom ObjectMapper that has both
        // Java 8 time support (from the primary ObjectMapper) and type information enabled
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        
        // Configure Redis cache with JSON serialization
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(cacheTtlHours)) // Configurable TTL from properties
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        serializer))
                .disableCachingNullValues(); // Don't cache null values

        // Note: transactionAware() is removed to prevent cache eviction failures from rolling back transactions
        // Cache eviction errors will be logged but won't affect the main transaction
        RedisCacheManager cacheManager = RedisCacheManager.builder(this.redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
        
        log.info("Redis CacheManager configured successfully with JSON serialization (Java 8 time support + type information, TTL: {} hours)", cacheTtlHours);
        return cacheManager;
    }

    /**
     * Cache error handler that logs errors but doesn't throw exceptions.
     * This ensures cache failures don't break application functionality.
     * Implements CachingConfigurer to register this as the default error handler.
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache get error for key {} in cache {}: {}", key, cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Cache put error for key {} in cache {}: {}", key, cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Cache evict error for key {} in cache {}: {}", key, cache.getName(), exception.getMessage());
                // Don't throw - allow transaction to proceed even if cache eviction fails
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Cache clear error for cache {}: {}", cache.getName(), exception.getMessage());
                // Don't throw - allow transaction to proceed even if cache clear fails
            }
        };
    }
}

