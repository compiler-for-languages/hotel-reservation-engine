package com.infotact.project1.service;

import com.infotact.project1.dto.request.LoginRequestDTO;
import com.infotact.project1.dto.response.LoginResponseDTO;
import com.infotact.project1.repository.UserRepository;
import com.infotact.project1.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import com.infotact.project1.dto.request.RegisterRequestDTO;
import com.infotact.project1.dto.response.UserResponseDTO;
import com.infotact.project1.enums.AccountStatus;
import com.infotact.project1.enums.Gender;
import com.infotact.project1.enums.Role;
import com.infotact.project1.model.User;

import java.util.Optional;

/*
 * Unit tests for AuthService.
 *
 * Covers:
 * - Customer registration
 * - User login
 * - Success scenarios
 * - Failure scenarios
 *
 * External dependencies are mocked using Mockito.
 */

@ExtendWith(MockitoExtension.class) // This tells that, for this class, enable Mockito
class AuthServiceTest {

    // Mock the dependencies, Mockito creates fake versions
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService; // for this particular service, the Mock must be injected

    /*
        This test will:
        * Verifies that a new customer is registered
        * successfully when email and phone number are unique.
     */
    @Test
    void registerCustomer_ShouldRegisterSuccessfully() {
        RegisterRequestDTO request = new RegisterRequestDTO();

        request.setFirstName("Shrikanth");
        request.setLastName("Sanagoudar");
        request.setGender(Gender.MALE);
        request.setEmail("shrikanth@gmail.com");
        request.setPhone("8861150224");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail()))
        .thenReturn(Optional.empty());

        when(userRepository.findByPhone(request.getPhone()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encodedPassword");

        User savedUser= new User();

        savedUser.setUserId(1L);
        savedUser.setFirstName("Shrikanth");
        savedUser.setLastName("Sanagoudar");
        savedUser.setGender(Gender.MALE);
        savedUser.setEmail("shrikanth@gmail.com");
        savedUser.setPhone("8861150224");
        savedUser.setRole(Role.CUSTOMER);
        savedUser.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponseDTO response = authService.registerCustomer(request);

        assertNotNull(response);
        assertEquals(1L,response.getUserId());
        assertEquals("Shrikanth", response.getFirstName());
        assertEquals("shrikanth@gmail.com",response.getEmail());
        assertEquals(Role.CUSTOMER, response.getRole());
        assertEquals(AccountStatus.ACTIVE, response.getAccountStatus());

        verify(userRepository).findByEmail(request.getEmail());
        verify(userRepository).findByPhone(request.getPhone());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
    }

    /*
        This test will :
     * Verify that registration fails when
     * the email address is already registered.
     */
    @Test
    void registerCustomer_ShouldThrowException_WhenEmailAlreadyExists() {

        RegisterRequestDTO request = new RegisterRequestDTO();

        request.setEmail("shrikanth@gmail.com");
        request.setPhone("8861150224");

        User existingUser = new User();
        existingUser.setEmail(request.getEmail());

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(existingUser));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.registerCustomer(request)
        );

        assertEquals(
                "Email already registered: " + request.getEmail(),
                exception.getMessage()
        );

        verify(userRepository).findByEmail(request.getEmail());

        verify(userRepository, never()).findByPhone(any());

        verify(userRepository, never()).save(any());

        verify(passwordEncoder, never()).encode(any());
    }

    /*
     * Verifies that registration fails when
     * the phone number is already registered.
     */

    @Test
    void registerCustomer_ShouldThrowException_WhenPhoneAlreadyExists() {

        RegisterRequestDTO request = new RegisterRequestDTO();

        request.setEmail("shrikanth@gmail.com");
        request.setPhone("8861150224");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        User existingUser = new User();
        existingUser.setPhone(request.getPhone());

        when(userRepository.findByPhone(request.getPhone()))
                .thenReturn(Optional.of(existingUser));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authService.registerCustomer(request)
        );

        assertEquals(
                "Phone number already registered: " + request.getPhone(),
                exception.getMessage()
        );

        verify(userRepository).findByEmail(request.getEmail());

        verify(userRepository).findByPhone(request.getPhone());

        verify(userRepository, never()).save(any());

        verify(passwordEncoder, never()).encode(any());
    }

    /*
     * Verifies that login succeeds when
     * valid credentials are provided.
     */

    @Test
    void login_ShouldReturnJwtToken_WhenCredentialsAreValid() {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("shrikanth@gmail.com");
        request.setPassword("password123");

        User user = new User();
        user.setEmail("shrikanth@gmail.com");
        user.setPasswordHash("encodedPassword");
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()))
                .thenReturn(true);

        when(jwtService.generateToken(user.getEmail()))
                .thenReturn("jwt-token");

        LoginResponseDTO response =
                authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());

        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(
                request.getPassword(),
                user.getPasswordHash());

        verify(jwtService).generateToken(user.getEmail());
    }

    /*
     * Verifies that login fails when
     * the email address does not exist.
     */

    @Test
    void login_ShouldThrowException_WhenEmailDoesNotExist() {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("unknown@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> authService.login(request));

        assertEquals(
                "Invalid email or password",
                exception.getMessage());

        verify(userRepository).findByEmail(request.getEmail());

        verify(passwordEncoder, never()).matches(any(), any());

        verify(jwtService, never()).generateToken(any());
    }

    /*
     * Verifies that login fails when
     * the user account is inactive.
     */

    @Test
    void login_ShouldThrowException_WhenAccountIsInactive() {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("shrikanth@gmail.com");
        request.setPassword("password123");

        User user = new User();
        user.setEmail("shrikanth@gmail.com");
        user.setAccountStatus(AccountStatus.INACTIVE);

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> authService.login(request));

        assertEquals(
                "Account is inactive",
                exception.getMessage());

        verify(passwordEncoder, never()).matches(any(), any());

        verify(jwtService, never()).generateToken(any());
    }

    /*
     * Verifies that login fails when
     * an incorrect password is provided.
     */
    
    @Test
    void login_ShouldThrowException_WhenPasswordIsIncorrect() {

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("shrikanth@gmail.com");
        request.setPassword("wrongPassword");

        User user = new User();
        user.setEmail("shrikanth@gmail.com");
        user.setPasswordHash("encodedPassword");
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()))
                .thenReturn(false);

        RuntimeException exception =
                assertThrows(RuntimeException.class,
                        () -> authService.login(request));

        assertEquals(
                "Invalid email or password",
                exception.getMessage());

        verify(jwtService, never()).generateToken(any());
    }

}