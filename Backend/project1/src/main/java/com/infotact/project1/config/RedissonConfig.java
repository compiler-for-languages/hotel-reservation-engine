package com.infotact.project1.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/*
 Configure Redis and Redisson for the application
 Redis is used for:
 -> Distributre Locking
 -> Booking held management
 -> Concurrency control

 Redisson provides a high-level Java API on top of Redis,
 allowing services to acquire distributed locks using Rlock
 */

@Configuration
@Profile("!test")
public class RedissonConfig {

    /*
    Creates a singleton RedissonClient bean
    Spring Initializes this bean during application startup
    and makes it available for dependency injection

    Ex: private final RedissonClient redissonClient;
     */

    @Bean
    public RedissonClient redissonClient() {

        // Holds Redis server configuration settings
        Config config = new Config();

        /*
        Configures Redisson to connect to a single Redis server

        Current setup:
        Host: 127.0.0.1 (localhost)
        Port: 6379 (default Redis port)

        In production environments this can be replaced with:
        -> Redis Cluster
        -> Redis Sentinel
        -> Managed Redis services
         */
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:6379");

        // Printed once when the application successfully creates the Redisson Client
        System.out.println("Redis connected successfully");

        /*
        Creates and returns the Redisson client instance
        This client is later used by LockService to:
        -> Acquire distributed locks
        -> Release distributed locks
        -> Prevent concurrent room booking conflicts
        */
        return Redisson.create(config);
    }
}