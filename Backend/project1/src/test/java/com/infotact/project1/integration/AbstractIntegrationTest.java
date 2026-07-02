package com.infotact.project1.integration;

import org.junit.jupiter.api.BeforeEach;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infotact.project1.dto.request.LoginRequestDTO;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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


public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected RedissonClient redissonClient;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected IntegrationTestHelper helper;

    protected String adminToken;


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

    /*
     * Authenticates the default administrator
     * and returns a valid JWT token.
     */
    protected String getAdminToken() throws Exception {

        if (adminToken != null) {
            return adminToken;
        }

        LoginRequestDTO loginRequest =
                new LoginRequestDTO();

        loginRequest.setEmail("admin@gmail.com");
        loginRequest.setPassword("admin123");

        String response = mockMvc.perform(

                        post("/api/auth/login")

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                loginRequest)))

                .andExpect(status().isOk())

                .andReturn()

                .getResponse()

                .getContentAsString();

        JsonNode json =
                objectMapper.readTree(response);

        adminToken =
                json.get("token").asText();

        return adminToken;
    }


}