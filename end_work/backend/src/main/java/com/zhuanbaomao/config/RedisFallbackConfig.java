package com.zhuanbaomao.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "spring.redis.host")
public class RedisFallbackConfig {

    @Bean
    @Lazy
    public RedisConnectionFactory redisConnectionFactory(RedisProperties properties) {
        log.info("Redis connection factory (lazy mode): {}:{}", properties.getHost(), properties.getPort());
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(properties.getHost());
        config.setPort(properties.getPort());
        config.setDatabase(properties.getDatabase());
        if (properties.getPassword() != null) {
            config.setPassword(org.springframework.data.redis.connection.RedisPassword.of(properties.getPassword()));
        }
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config,
                org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.builder()
                        .commandTimeout(Duration.ofSeconds(3))
                        .shutdownTimeout(Duration.ofMillis(200))
                        .build());
        return factory;
    }
}
