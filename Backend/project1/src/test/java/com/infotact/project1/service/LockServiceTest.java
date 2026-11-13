package com.infotact.project1.service;

import com.infotact.project1.service.LockService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/*
 * Unit tests for LockService.
 *
 * Covers:
 * - Lock acquisition
 * - Lock release
 * - Interrupted threads
 *
 * External dependencies are mocked using Mockito.
 */

@ExtendWith(MockitoExtension.class)
class LockServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @InjectMocks
    private LockService lockService;

    /*
     * Verifies that a distributed
     * lock is acquired successfully.
     */
    @Test
    void acquireLock_ShouldReturnLock_WhenLockAcquired()
            throws Exception {

        when(redissonClient.getLock("roomType:1"))
                .thenReturn(lock);

        when(lock.tryLock(
                5,
                30,
                TimeUnit.SECONDS))
                .thenReturn(true);

        RLock result =
                lockService.acquireLock("roomType:1");

        assertNotNull(result);

        assertEquals(lock, result);

        verify(redissonClient)
                .getLock("roomType:1");

        verify(lock)
                .tryLock(
                        5,
                        30,
                        TimeUnit.SECONDS);
    }

    /*
     * Verifies that an exception
     * is thrown when the lock
     * cannot be acquired.
     */
    @Test
    void acquireLock_ShouldThrowException_WhenLockCannotBeAcquired()
            throws Exception {

        when(redissonClient.getLock("roomType:1"))
                .thenReturn(lock);

        when(lock.tryLock(
                5,
                30,
                TimeUnit.SECONDS))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> lockService.acquireLock(
                                "roomType:1"));

        assertEquals(
                "Unable to acquire lock: roomType:1",
                exception.getMessage());
    }

    /*
     * Verifies that an interrupted
     * thread results in a
     * RuntimeException.
     */
    @Test
    void acquireLock_ShouldThrowException_WhenThreadInterrupted()
            throws Exception {

        when(redissonClient.getLock("roomType:1"))
                .thenReturn(lock);

        when(lock.tryLock(
                5,
                30,
                TimeUnit.SECONDS))
                .thenThrow(new InterruptedException());

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> lockService.acquireLock(
                                "roomType:1"));

        assertEquals(
                "Thread interrupted while acquiring lock: roomType:1",
                exception.getMessage());

        assertTrue(
                Thread.currentThread().isInterrupted());
    }

    /*
     * Verifies that a held
     * lock is released
     * successfully.
     */
    @Test
    void releaseLock_ShouldUnlockHeldLock() {

        when(lock.isHeldByCurrentThread())
                .thenReturn(true);

        lockService.releaseLock(lock);

        verify(lock).unlock();
    }

    /*
     * Verifies that releasing
     * a null lock does
     * nothing.
     */
    @Test
    void releaseLock_ShouldDoNothing_WhenLockIsNull() {

        assertDoesNotThrow(() ->
                lockService.releaseLock(null));

        verifyNoInteractions(lock);
    }

    /*
     * Verifies that a lock
     * owned by another thread
     * is not unlocked.
     */
    @Test
    void releaseLock_ShouldDoNothing_WhenCurrentThreadDoesNotHoldLock() {

        when(lock.isHeldByCurrentThread())
                .thenReturn(false);

        lockService.releaseLock(lock);

        verify(lock, never()).unlock();
    }

}

