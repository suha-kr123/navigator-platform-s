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
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@Slf4j
public class RedisCacheConfig {

    @Value("${redis.cache.ttl.hours:1}")
    private long cacheTtlHours;

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        log.info("Configuring Redis CacheManager with JSON serialization (TTL: {} hours)", cacheTtlHours);
        
        // Create a copy of the existing ObjectMapper to avoid modifying the shared instance
        // The existing ObjectMapper already has JavaTimeModule configured
        ObjectMapper redisObjectMapper = objectMapper.copy();
        
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

        RedisCacheManager cacheManager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .transactionAware()
                .build();
        
        log.info("Redis CacheManager configured successfully with JSON serialization (Java 8 time support + type information, TTL: {} hours)", cacheTtlHours);
        return cacheManager;
    }
}

