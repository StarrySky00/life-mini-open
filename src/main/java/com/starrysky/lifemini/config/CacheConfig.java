package com.starrysky.lifemini.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.starrysky.lifemini.common.constant.CacheConstant;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {
    /**
     * 自定义 Jackson2JsonRedisSerializer 配置
     */
    private Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }


    /**
     * 定义一个caffeineCacheManager的本地缓存管理器
     */
    @Bean(CacheConstant.CAFFEINE_CACHE_MANAGER)
    @Primary//其中一个设置为默认，否则两个都不是默认的，服务启动失败
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // 配置 Caffeine 缓存策略
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .initialCapacity(50)           // 初始容量
            .maximumSize(1000)             // 最大缓存项数（防内存溢出）
            .expireAfterWrite(12, TimeUnit.HOURS)   // 写入12小时后过期
            .recordStats()                 // 开启统计（用于监控命中率等）
        );
        return cacheManager;
    }
    /**
     * 定义一个caffeineShortCacheManager的本地缓存管理器
     */
    @Bean(CacheConstant.CAFFEINE_SHORT_CACHE_MANAGER)
    //其中一个设置为默认，否则两个都不是默认的，服务启动失败
    public CacheManager caffeineShortCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // 配置 Caffeine 缓存策略
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(50)           // 初始容量
                .maximumSize(1000)             // 最大缓存项数（防内存溢出）
                .expireAfterWrite(20, TimeUnit.MINUTES)   // 写入20分钟过期
                .recordStats()                 // 开启统计（用于监控命中率等）
        );
        return cacheManager;
    }
    /**
     * RedisCacheManager 配置
     */
    @Bean(CacheConstant.REDIS_CACHE_MANAGER)
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Key 序列化器
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        // Value 序列化器
        Jackson2JsonRedisSerializer<Object> valueSerializer = jackson2JsonRedisSerializer();

        // 配置缓存的序列化方式
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(6)) // 缓存过期时间 6 小时
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
}