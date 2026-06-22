package com.infotact.project1.service;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class LockService {

    // Dependency remains immutable after injection
    private final RedissonClient redissonClient;

    // Acquire distributed lock
    public RLock acquireLock(String lockName) {

        RLock lock = redissonClient.getLock(lockName);

        try {

            boolean lockAcquired =
                    lock.tryLock(
                            5,
                            30,
                            TimeUnit.SECONDS);

            if (!lockAcquired) {

                throw new RuntimeException(
                        "Unable to acquire lock: "
                                + lockName);
            }

            return lock;

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrupted while acquiring lock: "
                            + lockName,
                    exception);
        }
    }

    // Release distributed lock
    public void releaseLock(RLock lock) {

        if (lock != null
                && lock.isHeldByCurrentThread()) {

            lock.unlock();
        }
    }
}