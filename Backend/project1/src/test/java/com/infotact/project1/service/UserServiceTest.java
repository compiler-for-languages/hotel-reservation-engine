package com.infotact.project1.service;

import com.infotact.project1.dto.request.UserPatchRequestDTO;
import com.infotact.project1.dto.request.UserRequestDTO;
import com.infotact.project1.dto.response.UserResponseDTO;
import com.infotact.project1.enums.AccountStatus;
import com.infotact.project1.enums.Gender;
import com.infotact.project1.enums.Role;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.UserRepository;
import com.infotact.project1.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/*
 * Unit tests for UserService.
 *
 * Covers:
 * - User creation
 * - User retrieval
 * - User update
 * - User deletion
 * - Customer and receptionist filtering
 *
 * External dependencies are mocked using Mockito.
 */

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    /*
     * Verifies that a new user is created
     * successfully when email and phone
     * number are unique.
     */
    @Test
    void createUser_ShouldCreateUserSuccessfully() {

        UserRequestDTO request = new UserRequestDTO();

        request.setFirstName("Shrikanth");
        request.setLastName("Sanagoudar");
        request.setGender(Gender.MALE);
        request.setEmail("shrikanth@gmail.com");
        request.setPhone("8861150224");
        request.setPassword("password123");
        request.setRole(Role.RECEPTIONIST);

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        when(userRepository.findByPhone(request.getPhone()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encodedPassword");

        User savedUser = new User();

        savedUser.setUserId(1L);
        savedUser.setFirstName("Shrikanth");
        savedUser.setLastName("Sanagoudar");
        savedUser.setGender(Gender.MALE);
        savedUser.setEmail("shrikanth@gmail.com");
        savedUser.setPhone("8861150224");
        savedUser.setRole(Role.RECEPTIONIST);
        savedUser.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponseDTO response =
                userService.createUser(request);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("Shrikanth", response.getFirstName());
        assertEquals("shrikanth@gmail.com", response.getEmail());
        assertEquals(Role.RECEPTIONIST, response.getRole());
        assertEquals(AccountStatus.ACTIVE,
                response.getAccountStatus());

        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository).findByPhone(request.getPhone());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
    }

    /*
     * Verifies that user creation fails
     * when the email address already exists.
     */
    @Test
    void createUser_ShouldThrowException_WhenEmailAlreadyExists() {

        UserRequestDTO request = new UserRequestDTO();

        request.setEmail("shrikanth@gmail.com");
        request.setPhone("8861150224");

        User existingUser = new User();
        existingUser.setEmail(request.getEmail());

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(existingUser));

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> userService.createUser(request));

        assertEquals(
                "Email already registered: " + request.getEmail(),
                exception.getMessage());

        verify(userRepository).findByEmail(request.getEmail());

        verify(userRepository, never())
                .findByPhone(any());

        verify(passwordEncoder, never())
                .encode(any());

        verify(userRepository, never())
                .save(any());
    }

    /*
     * Verifies that user creation fails
     * when the phone number already exists.
     */
    @Test
    void createUser_ShouldThrowException_WhenPhoneAlreadyExists() {

        UserRequestDTO request = new UserRequestDTO();

        request.setEmail("shrikanth@gmail.com");
        request.setPhone("8861150224");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        User existingUser = new User();
        existingUser.setPhone(request.getPhone());

        when(userRepository.findByPhone(request.getPhone()))
                .thenReturn(Optional.of(existingUser));

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> userService.createUser(request));

        assertEquals(
                "Phone number already registered: "
                        + request.getPhone(),
                exception.getMessage());

        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository).findByPhone(request.getPhone());

        verify(passwordEncoder, never())
                .encode(any());

        verify(userRepository, never())
                .save(any());
    }

    /*
     * Verifies that all users are
     * retrieved successfully.
     */
    @Test
    void getAllUsers_ShouldReturnAllUsers() {

        User user1 = new User();
        user1.setUserId(1L);
        user1.setFirstName("Shrikanth");
        user1.setRole(Role.CUSTOMER);
        user1.setAccountStatus(AccountStatus.ACTIVE);

        User user2 = new User();
        user2.setUserId(2L);
        user2.setFirstName("Rahul");
        user2.setRole(Role.RECEPTIONIST);
        user2.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        List<UserResponseDTO> users =
                userService.getAllUsers();

        assertEquals(2, users.size());

        verify(userRepository).findAll();
    }

    /*
     * Verifies that a user is retrieved
     * successfully using a valid user id.
     */
    @Test
    void getUserById_ShouldReturnUser() {

        User user = new User();

        user.setUserId(1L);
        user.setFirstName("Shrikanth");
        user.setLastName("Sanagoudar");
        user.setEmail("shrikanth@gmail.com");
        user.setPhone("8861150224");
        user.setRole(Role.CUSTOMER);
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponseDTO response =
                userService.getUserById(1L);

        assertNotNull(response);

        assertEquals(1L, response.getUserId());
        assertEquals("Shrikanth",
                response.getFirstName());

        verify(userRepository).findById(1L);
    }

    /*
     * Verifies that retrieving a
     * non-existing user throws
     * an exception.
     */
    @Test
    void getUserById_ShouldThrowException_WhenUserDoesNotExist() {

        when(userRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> userService.getUserById(100L));

        assertEquals(
                "User not found with id: 100",
                exception.getMessage());

        verify(userRepository).findById(100L);
    }

    /*
     * Verifies that a user is retrieved
     * successfully using email.
     */
    @Test
    void getUserByEmail_ShouldReturnUser() {

        User user = new User();

        user.setUserId(1L);
        user.setFirstName("Shrikanth");
        user.setLastName("Sanagoudar");
        user.setEmail("shrikanth@gmail.com");
        user.setPhone("8861150224");
        user.setRole(Role.CUSTOMER);
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByEmail(
                "shrikanth@gmail.com"))
                .thenReturn(Optional.of(user));

        UserResponseDTO response =
                userService.getUserByEmail(
                        "shrikanth@gmail.com");

        assertNotNull(response);

        assertEquals(1L, response.getUserId());
        assertEquals(
                "Shrikanth",
                response.getFirstName());

        verify(userRepository)
                .findByEmail("shrikanth@gmail.com");
    }

    /*
     * Verifies that retrieving a user
     * using a non-existing email
     * throws an exception.
     */
    @Test
    void getUserByEmail_ShouldThrowException_WhenEmailDoesNotExist() {

        when(userRepository.findByEmail(
                "unknown@gmail.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> userService.getUserByEmail(
                                "unknown@gmail.com"));

        assertEquals(
                "User not found with email: unknown@gmail.com",
                exception.getMessage());

        verify(userRepository)
                .findByEmail("unknown@gmail.com");
    }

    /*
     * Verifies that user details
     * are updated successfully.
     */
    @Test
    void updateUser_ShouldUpdateUserSuccessfully() {

        User user = new User();

        user.setUserId(1L);
        user.setFirstName("Old");
        user.setLastName("Name");
        user.setPhone("9999999999");
        user.setAccountStatus(AccountStatus.ACTIVE);

        UserPatchRequestDTO request =
                new UserPatchRequestDTO();

        request.setFirstName("Shrikanth");
        request.setLastName("Sanagoudar");
        request.setPhone("8861150224");
        request.setAccountStatus(
                AccountStatus.INACTIVE);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userRepository.findByPhone(
                request.getPhone()))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        UserResponseDTO response =
                userService.updateUser(
                        1L,
                        request);

        assertEquals(
                "Shrikanth",
                response.getFirstName());

        assertEquals(
                "Sanagoudar",
                response.getLastName());

        assertEquals(
                "8861150224",
                response.getPhone());

        assertEquals(
                AccountStatus.INACTIVE,
                response.getAccountStatus());

        verify(userRepository).findById(1L);
        verify(userRepository)
                .findByPhone(request.getPhone());
        verify(userRepository)
                .save(any(User.class));
    }

    /*
     * Verifies that updating a user
     * with an already registered phone
     * number throws an exception.
     */
    @Test
    void updateUser_ShouldThrowException_WhenPhoneAlreadyExists() {

        User currentUser = new User();

        currentUser.setUserId(1L);
        currentUser.setPhone("9999999999");

        User existingUser = new User();

        existingUser.setUserId(2L);
        existingUser.setPhone("8861150224");

        UserPatchRequestDTO request =
                new UserPatchRequestDTO();

        request.setPhone("8861150224");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(currentUser));

        when(userRepository.findByPhone(
                request.getPhone()))
                .thenReturn(Optional.of(existingUser));

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> userService.updateUser(
                                1L,
                                request));

        assertEquals(
                "Phone number already registered: 8861150224",
                exception.getMessage());

        verify(userRepository).findById(1L);
        verify(userRepository)
                .findByPhone(request.getPhone());

        verify(userRepository, never())
                .save(any());
    }

    /*
     * Verifies that updating
     * a non-existing user
     * throws an exception.
     */
    @Test
    void updateUser_ShouldThrowException_WhenUserDoesNotExist() {

        UserPatchRequestDTO request =
                new UserPatchRequestDTO();

        when(userRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> userService.updateUser(
                                100L,
                                request));

        assertEquals(
                "User not found with id: 100",
                exception.getMessage());

        verify(userRepository)
                .findById(100L);
    }

    /*
     * Verifies that a user
     * is deleted successfully.
     */
    @Test
    void deleteUser_ShouldDeleteUserSuccessfully() {

        User user = new User();

        user.setUserId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    /*
     * Verifies that deleting a
     * non-existing user throws
     * an exception.
     */
    @Test
    void deleteUser_ShouldThrowException_WhenUserDoesNotExist() {

        when(userRepository.findById(100L))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> userService.deleteUser(100L));

        assertEquals(
                "User not found with id: 100",
                exception.getMessage());

        verify(userRepository)
                .findById(100L);
    }

    /*
     * Verifies that only customers
     * are returned successfully.
     */
    @Test
    void getAllCustomers_ShouldReturnOnlyCustomers() {

        User customer1 = new User();
        customer1.setUserId(1L);
        customer1.setFirstName("John");
        customer1.setRole(Role.CUSTOMER);
        customer1.setAccountStatus(AccountStatus.ACTIVE);

        User customer2 = new User();
        customer2.setUserId(2L);
        customer2.setFirstName("David");
        customer2.setRole(Role.CUSTOMER);
        customer2.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByRole(Role.CUSTOMER))
                .thenReturn(List.of(customer1, customer2));

        List<UserResponseDTO> customers =
                userService.getAllCustomers();

        assertNotNull(customers);
        assertEquals(2, customers.size());

        assertEquals(Role.CUSTOMER,
                customers.get(0).getRole());

        assertEquals(Role.CUSTOMER,
                customers.get(1).getRole());

        verify(userRepository)
                .findByRole(Role.CUSTOMER);
    }

    /*
     * Verifies that only receptionists
     * are returned successfully.
     */
    @Test
    void getAllReceptionists_ShouldReturnOnlyReceptionists() {

        User receptionist1 = new User();
        receptionist1.setUserId(1L);
        receptionist1.setFirstName("Shrikanth");
        receptionist1.setRole(Role.RECEPTIONIST);
        receptionist1.setAccountStatus(AccountStatus.ACTIVE);

        User receptionist2 = new User();
        receptionist2.setUserId(2L);
        receptionist2.setFirstName("Rahul");
        receptionist2.setRole(Role.RECEPTIONIST);
        receptionist2.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByRole(Role.RECEPTIONIST))
                .thenReturn(List.of(receptionist1, receptionist2));

        List<UserResponseDTO> receptionists =
                userService.getAllReceptionists();

        assertNotNull(receptionists);
        assertEquals(2, receptionists.size());

        assertEquals(Role.RECEPTIONIST,
                receptionists.get(0).getRole());

        assertEquals(Role.RECEPTIONIST,
                receptionists.get(1).getRole());

        verify(userRepository)
                .findByRole(Role.RECEPTIONIST);
    }

}

