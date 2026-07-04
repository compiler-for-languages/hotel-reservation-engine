package com.infotact.project1.service;

import com.infotact.project1.exception.BusinessExceptions;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LockService {

    private final RedissonClient redissonClient;

    public RLock acquireLock(String lockName) {

        RLock lock = redissonClient.getLock(lockName);

        try {
            boolean lockAcquired =
                    lock.tryLock(5, 30, TimeUnit.SECONDS);

            if (!lockAcquired) {
                throw BusinessExceptions.lockFailed(lockName);
            }

            return lock;

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw BusinessExceptions.threadInterrupted(lockName);
        }
    }

    public void releaseLock(RLock lock) {

        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
