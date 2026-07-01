package com.infotact.project1.config;

import com.infotact.project1.enums.AccountStatus;
import com.infotact.project1.enums.Gender;
import com.infotact.project1.enums.Role;
import com.infotact.project1.model.User;
import com.infotact.project1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/*
 * Creates a default administrator account when the application starts.
 *
 * The admin is created only if no administrator already exists.
 * This avoids manual database inserts for the initial system setup.
 */

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    // Repository used to check and create admin user
    private final UserRepository userRepository;

    // BCrypt password encoder
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // Skip creation if an admin already exists
        if (!userRepository.findByRole(Role.ADMIN).isEmpty()) {

            System.out.println(
                    "Admin account already exists.");

            return;
        }

        // Create default administrator
        User admin = new User();

        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setGender(Gender.MALE);

        admin.setEmail("admin@gmail.com");
        admin.setPhone("9999999999");

        // BCrypt hashed password
        admin.setPasswordHash(
                passwordEncoder.encode("admin123"));

        admin.setRole(Role.ADMIN);

        admin.setAccountStatus(
                AccountStatus.ACTIVE);

        userRepository.save(admin);

        System.out.println(
                "Default administrator account created successfully.");
    }
}