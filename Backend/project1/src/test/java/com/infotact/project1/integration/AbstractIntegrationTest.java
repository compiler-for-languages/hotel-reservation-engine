package com.infotact.project1.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infotact.project1.dto.request.LoginRequestDTO;
import com.infotact.project1.model.BookingHold;
import com.infotact.project1.repository.BookingHoldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
 * -> Replaces Redis/Redisson infrastructure with mocks so tests do not
 *    require a running Redis instance while keeping locking behaviour deterministic.
 */

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected IntegrationTestHelper helper;

    @MockBean
    protected RedissonClient redissonClient;

    @MockBean
    protected BookingHoldRepository bookingHoldRepository;

    @MockBean
    protected RedisMessageListenerContainer redisMessageListenerContainer;

    protected String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        // Setup deterministic RLock behaviour on the mocked RedissonClient
        RLock lock = Mockito.mock(RLock.class);
        Mockito.lenient().when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        Mockito.lenient().when(lock.isHeldByCurrentThread()).thenReturn(true);
        Mockito.lenient().when(redissonClient.getLock(anyString())).thenReturn(lock);

        // Setup BookingHoldRepository mock to behave like a clean in-memory repository
        Mockito.lenient().when(bookingHoldRepository.save(any(BookingHold.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.lenient().when(bookingHoldRepository.findAll())
                .thenReturn(Collections.emptyList());
        Mockito.lenient().when(bookingHoldRepository.findById(anyString()))
                .thenReturn(Optional.empty());

        bookingHoldRepository.deleteAll();
    }

    /*
     * Authenticates the default administrator
     * and returns a valid JWT token.
     */
    protected String getAdminToken() throws Exception {
        if (adminToken != null) {
            return adminToken;
        }

        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("admin@gmail.com");
        loginRequest.setPassword("admin123");

        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        adminToken = json.get("token").asText();
        return adminToken;
    }
}
