package com.infotact.project1.integration;

import com.infotact.project1.dto.request.UserRequestDTO;
import com.infotact.project1.enums.AccountStatus;
import com.infotact.project1.enums.Gender;
import com.infotact.project1.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * Integration tests for Admin User APIs.
 *
 * Verifies complete administrator
 * user management workflow.
 */
class AdminUserIntegrationTest
        extends AbstractIntegrationTest {

    /*
     * Verifies that an administrator
     * can create a receptionist.
     */
    @Test
    void createReceptionist_ShouldCreateReceptionist()
            throws Exception {

        String adminToken =
                getAdminToken();

        UserRequestDTO request =
                new UserRequestDTO();

        request.setFirstName(
                "Reception");

        request.setLastName(
                "User");

        request.setGender(
                Gender.MALE);

        request.setEmail(
                "reception"
                        + System.nanoTime()
                        + "@gmail.com");

        request.setPhone(
                String.valueOf(System.nanoTime())
                        .substring(0, 10));

        request.setPassword(
                "password123");

        request.setRole(
                Role.RECEPTIONIST);

        mockMvc.perform(

                        post("/api/admin/users/receptionist")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.firstName")
                        .value("Reception"))

                .andExpect(jsonPath("$.role")
                        .value("RECEPTIONIST"))

                .andExpect(jsonPath("$.accountStatus")
                        .value("ACTIVE"));
    }

    /*
     * Verifies that administrator
     * can retrieve all users.
     */
    @Test
    void getAllUsers_ShouldReturnUsers()
            throws Exception {

        String adminToken =
                getAdminToken();

        UserRequestDTO request =
                new UserRequestDTO();

        request.setFirstName(
                "Reception");

        request.setLastName(
                "User");

        request.setGender(
                Gender.MALE);

        request.setEmail(
                "reception"
                        + System.nanoTime()
                        + "@gmail.com");

        request.setPhone(
                String.valueOf(System.nanoTime())
                        .substring(0, 10));

        request.setPassword(
                "password123");

        request.setRole(
                Role.RECEPTIONIST);

        mockMvc.perform(

                        post("/api/admin/users/receptionist")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))

                .andExpect(status().isOk());

        mockMvc.perform(

                        get("/api/admin/users/getall")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$")
                        .isArray())

                .andExpect(jsonPath("$.length()")
                        .value(org.hamcrest.Matchers.greaterThan(0)));
    }

    /*
     * Verifies that administrator
     * can retrieve a user by id.
     */
    @Test
    void getUserById_ShouldReturnUser()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long userId =
                helper.createCustomer();

        mockMvc.perform(

                        get("/api/admin/users/get/{userId}",
                                userId)

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.userId")
                        .value(userId))

                .andExpect(jsonPath("$.role")
                        .value("CUSTOMER"));
    }

    /*
     * Verifies that administrator
     * can retrieve a user by email.
     */
    @Test
    void getUserByEmail_ShouldReturnUser()
            throws Exception {

        String adminToken =
                getAdminToken();

        String email =
                "customer"
                        + System.nanoTime()
                        + "@gmail.com";

        String phone =
                String.valueOf(System.nanoTime())
                        .substring(0, 10);

        mockMvc.perform(

                        post("/api/auth/register")

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content("""
                                        {
                                            "firstName":"Test",
                                            "lastName":"Customer",
                                            "gender":"MALE",
                                            "email":"%s",
                                            "phone":"%s",
                                            "password":"password123"
                                        }
                                        """.formatted(email, phone)))

                .andExpect(status().isOk());

        mockMvc.perform(

                        get("/api/admin/users/get")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .param(
                                        "email",
                                        email))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.email")
                        .value(email))

                .andExpect(jsonPath("$.role")
                        .value("CUSTOMER"));
    }

    /*
     * Verifies that administrator
     * can partially update a user.
     */
    @Test
    void updateUser_ShouldUpdateUser()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long userId =
                helper.createCustomer();

        mockMvc.perform(

                        patch("/api/admin/users/update/{userId}",
                                userId)

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content("""
                                        {
                                            "firstName":"Updated",
                                            "accountStatus":"INACTIVE"
                                        }
                                        """))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$.firstName")
                        .value("Updated"))

                .andExpect(jsonPath("$.accountStatus")
                        .value("INACTIVE"));
    }

    /*
     * Verifies that administrator
     * can delete a user.
     */
    @Test
    void deleteUser_ShouldDeleteUser()
            throws Exception {

        String adminToken =
                getAdminToken();

        Long userId =
                helper.createCustomer();

        mockMvc.perform(

                        delete("/api/admin/users/delete/{userId}",
                                userId)

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken))

                .andExpect(status().isOk())

                .andExpect(content()
                        .string("User deleted successfully"));
    }

    /*
     * Verifies that administrator
     * can retrieve all customers.
     */
    @Test
    void getAllCustomers_ShouldReturnCustomers()
            throws Exception {

        String adminToken =
                getAdminToken();

        helper.createCustomer();

        mockMvc.perform(

                        get("/api/admin/users/customers")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$")
                        .isArray())

                .andExpect(jsonPath("$[0].role")
                        .value("CUSTOMER"));
    }

    /*
     * Verifies that administrator
     * can retrieve all receptionists.
     */
    @Test
    void getAllReceptionists_ShouldReturnReceptionists()
            throws Exception {

        String adminToken =
                getAdminToken();

        UserRequestDTO request =
                new UserRequestDTO();

        request.setFirstName(
                "Reception");

        request.setLastName(
                "Staff");

        request.setGender(
                Gender.MALE);

        request.setEmail(
                "reception"
                        + System.nanoTime()
                        + "@gmail.com");

        request.setPhone(
                String.valueOf(System.nanoTime())
                        .substring(0, 10));

        request.setPassword(
                "password123");

        request.setRole(
                Role.RECEPTIONIST);

        mockMvc.perform(

                        post("/api/admin/users/receptionist")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken)

                                .contentType(
                                        MediaType.APPLICATION_JSON)

                                .content(
                                        objectMapper.writeValueAsString(
                                                request)))

                .andExpect(status().isOk());

        mockMvc.perform(

                        get("/api/admin/users/receptionists")

                                .header(
                                        "Authorization",
                                        "Bearer " + adminToken))

                .andExpect(status().isOk())

                .andExpect(jsonPath("$")
                        .isArray())

                .andExpect(jsonPath("$[0].role")
                        .value("RECEPTIONIST"));
    }
}

