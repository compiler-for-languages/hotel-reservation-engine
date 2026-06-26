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

    // Redisson client used to communicate with Redis
    private final RedissonClient redissonClient;

    // Acquires a distributed lock for the given resource
    public RLock acquireLock(String lockName) {

        // Obtain a distributed lock object from Redis
        RLock lock = redissonClient.getLock(lockName);

        try {
            // wait up to 5 seconds to acquire the lock
            // automatically release it after 30 seconds if not unlocked manually
            boolean lockAcquired =
                    lock.tryLock(
                            5,
                            30,
                            TimeUnit.SECONDS);
            // Prevent multiple users from accessing the same resource simultaneously
            if (!lockAcquired) {

                throw new RuntimeException(
                        "Unable to acquire lock: "
                                + lockName);
            }

            return lock;

        } catch (InterruptedException exception) {

            // Restore interrupted thread status
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrupted while acquiring lock: "
                            + lockName,
                    exception);
        }
    }

    // Release distributed lock after the critical section completes
    public void releaseLock(RLock lock) {

        // unlock only if the current thread owns the lock
        if (lock != null
                && lock.isHeldByCurrentThread()) {

            lock.unlock();
        }
    }
}