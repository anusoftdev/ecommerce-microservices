package com.ecommerce.productservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.ecommerce.productservice.dto.response.ProductResponse;
import com.ecommerce.commonlib.util.PageResponse;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${cache.product.ttl}")
    private long productTtlSeconds;

    /**
     * Shared ObjectMapper for Redis — clean, no DefaultTyping.
     * JavaTimeModule handles LocalDateTime serialization.
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    /**
     * RedisTemplate for manual key-value operations.
     * Uses ProductResponse-specific serializer to avoid
     * LinkedHashMap cast issues.
     */
    @Bean
    public RedisTemplate<String, ProductResponse> redisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, ProductResponse> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        Jackson2JsonRedisSerializer<ProductResponse> serializer =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper(),
                        ProductResponse.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Helper — builds a RedisCacheConfiguration for a specific type.
     * By binding the serializer to a concrete class, Jackson always
     * knows exactly what to deserialize into — no LinkedHashMap.
     */
    private <T> RedisCacheConfiguration cacheConfig(
            Class<T> type, long ttlSeconds) {
        Jackson2JsonRedisSerializer<T> serializer =
                new Jackson2JsonRedisSerializer<>(redisObjectMapper(), type);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(ttlSeconds))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer))
                .disableCachingNullValues();
    }

    /**
     * CacheManager with per-cache type-safe configurations.
     *
     * Each cache name gets its own serializer bound to the
     * exact return type of the cached method.
     * This is the KEY fix — no more LinkedHashMap casts.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // product:id cache → deserializes into ProductResponse
        cacheConfigs.put(CacheNames.PRODUCT_BY_ID,
                cacheConfig(ProductResponse.class, productTtlSeconds));

        // product:sku cache → deserializes into ProductResponse
        cacheConfigs.put(CacheNames.PRODUCT_BY_SKU,
                cacheConfig(ProductResponse.class, productTtlSeconds));

        // products:all cache → deserializes into PageResponse
        // PageResponse contains a List<ProductResponse> internally
        cacheConfigs.put(CacheNames.PRODUCTS_ALL,
                cacheConfig(PageResponse.class, productTtlSeconds));

        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations(cacheConfigs)
                // Fallback for any cache not explicitly configured
                .cacheDefaults(cacheConfig(Object.class, productTtlSeconds))
                .build();
    }
}