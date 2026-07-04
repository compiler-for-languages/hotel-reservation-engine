package com.infotact.project1.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infotact.project1.dto.request.LoginRequestDTO;
import com.infotact.project1.dto.request.RegisterRequestDTO;
import com.infotact.project1.enums.Gender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * Integration tests for authentication APIs.
 *
 * Verifies complete request flow:
 *
 * HTTP Request
 *      ↓
 * Controller
 *      ↓
 * Service
 *      ↓
 * Repository
 *      ↓
 * H2 Database
 */

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    /*
     * Verifies that a new customer
     * can register successfully.
     */
    @Test
    void registerCustomer_ShouldReturnCreatedUser() throws Exception {

        RegisterRequestDTO request =
                new RegisterRequestDTO();

        request.setFirstName("Shrikanth");
        request.setLastName("Sanagoudar");
        request.setGender(Gender.MALE);
        request.setEmail("shrikanth@gmail.com");
        request.setPhone("8861150224");
        request.setPassword("password123");

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName")
                        .value("Shrikanth"))
                .andExpect(jsonPath("$.email")
                        .value("shrikanth@gmail.com"))
                .andExpect(jsonPath("$.role")
                        .value("CUSTOMER"))
                .andExpect(jsonPath("$.accountStatus")
                        .value("ACTIVE"));
    }

    /*
     * Verifies that registration fails
     * when the email is already registered.
     */
    @Test
    void registerCustomer_ShouldReturnBadRequest_WhenEmailAlreadyExists() throws Exception {

        RegisterRequestDTO request = new RegisterRequestDTO();

        request.setFirstName("Shrikanth");
        request.setLastName("Sanagoudar");
        request.setGender(Gender.MALE);
        request.setEmail("shrikanth@gmail.com");
        request.setPhone("8861150224");
        request.setPassword("password123");

        // First registration
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Duplicate registration
        RegisterRequestDTO duplicate = new RegisterRequestDTO();

        duplicate.setFirstName("John");
        duplicate.setLastName("Doe");
        duplicate.setGender(Gender.MALE);
        duplicate.setEmail("shrikanth@gmail.com");
        duplicate.setPhone("9999999999");
        duplicate.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }

    /*
     * Verifies that registration fails
     * when the phone number is already registered.
     */
    @Test
    void registerCustomer_ShouldReturnBadRequest_WhenPhoneAlreadyExists() throws Exception {

        RegisterRequestDTO request = new RegisterRequestDTO();

        request.setFirstName("Shrikanth");
        request.setLastName("Sanagoudar");
        request.setGender(Gender.MALE);
        request.setEmail("shrikanth@gmail.com");
        request.setPhone("8861150224");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        RegisterRequestDTO duplicate = new RegisterRequestDTO();

        duplicate.setFirstName("John");
        duplicate.setLastName("Doe");
        duplicate.setGender(Gender.MALE);
        duplicate.setEmail("john@gmail.com");
        duplicate.setPhone("8861150224");
        duplicate.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isBadRequest());
    }

    /*
     * Verifies that a registered customer
     * can login successfully and receive a JWT.
     */
    @Test
    void login_ShouldReturnJwtToken() throws Exception {

        RegisterRequestDTO register = new RegisterRequestDTO();

        register.setFirstName("Shrikanth");
        register.setLastName("Sanagoudar");
        register.setGender(Gender.MALE);
        register.setEmail("shrikanth@gmail.com");
        register.setPhone("8861150224");
        register.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        LoginRequestDTO login = new LoginRequestDTO();

        login.setEmail("shrikanth@gmail.com");
        login.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    /*
     * Verifies that login fails
     * when an incorrect password is provided.
     */
    @Test
    void login_ShouldReturnUnauthorized_WhenPasswordIsIncorrect() throws Exception {

        RegisterRequestDTO register = new RegisterRequestDTO();

        register.setFirstName("Shrikanth");
        register.setLastName("Sanagoudar");
        register.setGender(Gender.MALE);
        register.setEmail("shrikanth@gmail.com");
        register.setPhone("8861150224");
        register.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        LoginRequestDTO login = new LoginRequestDTO();

        login.setEmail("shrikanth@gmail.com");
        login.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    /*
     * Verifies that login fails
     * when the user does not exist.
     */
    @Test
    void login_ShouldReturnUnauthorized_WhenUserDoesNotExist() throws Exception {

        LoginRequestDTO login = new LoginRequestDTO();

        login.setEmail("nouser@gmail.com");
        login.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}