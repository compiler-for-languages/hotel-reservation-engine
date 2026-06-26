package com.infotact.project1.controller;

import com.infotact.project1.service.LockService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lock")

// Lombok generates constructor for final fields
@RequiredArgsConstructor
public class LockController {

    private final LockService lockService;

    // Test distributed locking
    @GetMapping("/test")
    public String testLock() {

        // Acquire an exclusive lock for the specified room type
        RLock lock =
                lockService.acquireLock("roomType:3");

        try {
            // Simulate a critical section protected by the lock
            System.out.println(
                    "Lock acquired by thread: "
                            + Thread.currentThread().getName());

            // Simulate a long running operation
            Thread.sleep(10000);

            return "Lock acquired successfully";

        } catch (InterruptedException exception) {

            // Restore interrupted thread status
            Thread.currentThread().interrupt();

            throw new RuntimeException(
                    "Thread interrupted",
                    exception);

        } finally {

            // Always release the lock to avoid deadlocks
            lockService.releaseLock(lock);

            System.out.println(
                    "Lock released by thread: "
                            + Thread.currentThread().getName());
        }
    }
}