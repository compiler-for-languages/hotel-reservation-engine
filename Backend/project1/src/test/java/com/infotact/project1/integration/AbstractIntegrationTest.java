package com.infotact.project1.integration;

import com.infotact.project1.integration.config.TestRedisConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/*
 * Base class for all integration tests.
 *
 * Responsibilities:
 * -> Starts the complete Spring Boot application
 * -> Uses H2 in-memory database
 * -> Loads test profile
 * -> Configures MockMvc for HTTP request testing
 * -> Rolls back database changes after each test
 * -> Replaces Redis with mocked configuration
 */

@SpringBootTest

@AutoConfigureMockMvc

@ActiveProfiles("test")

@Transactional

@Import(TestRedisConfig.class)
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        /*
         * Common initialization for all
         * integration tests.
         *
         * Currently empty but can later be
         * used for:
         *
         * - Loading common test data
         * - Cleaning resources
         * - Resetting mocks
         */
    }
}