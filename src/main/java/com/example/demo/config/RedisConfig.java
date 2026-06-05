package com.example.demo.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@EnableCaching
@Configuration
public class RedisConfig {
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {

        // NOTE: Old ObjectMapper and Jackson2JsonRedisSerializer is deprecated!
        RedisSerializer<Object> serializer = RedisSerializer.json();
        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeKeysWith(RedisSerializationContext
                                .SerializationPair
                                .fromSerializer(RedisSerializer.string()))
                        .serializeValuesWith(RedisSerializationContext
                                .SerializationPair.fromSerializer(serializer))
                        .entryTtl(Duration.ofHours(16));

        // whatever namespace has more possibility of changing in the DB must be given less TTL for consistency
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();


        return RedisCacheManager.builder(factory).cacheDefaults(defaultConfig).withInitialCacheConfigurations(cacheConfigs).build();


    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate( // to directly talk to redis rather than using spring cache
            RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template =
                new RedisTemplate<>();

        template.setConnectionFactory(factory);

        template.setKeySerializer(
                RedisSerializer.string());

        template.setValueSerializer(
                RedisSerializer.json());

        template.setHashKeySerializer(
                RedisSerializer.string());

        template.setHashValueSerializer(
                RedisSerializer.json());

        template.afterPropertiesSet();

        return template;

    }
}
