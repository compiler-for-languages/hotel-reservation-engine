package com.infotact.project1.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient() {

        Config config = new Config();

        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");

        System.out.println("Redis connected successfully");
        return Redisson.create(config);
    }
}