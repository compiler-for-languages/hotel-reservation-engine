package com.infotact.project1.integration.config;

import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/*
 * Test Redis configuration.
 *
 * Replaces the real Redis client with Mockito
 * mocks so integration tests can run without
 * a Redis server.
 */

@TestConfiguration
public class TestRedisConfig {

    @Bean
    public RedissonClient redissonClient() {

        RedissonClient client =
                Mockito.mock(RedissonClient.class);

        RLock lock =
                Mockito.mock(RLock.class);

        Mockito.when(client.getLock(Mockito.anyString()))
                .thenReturn(lock);

        return client;
    }

}