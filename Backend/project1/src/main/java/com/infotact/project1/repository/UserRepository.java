package com.infotact.project1.repository;

import com.infotact.project1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

// JpaRepository provides built-in CRUD operations
public interface UserRepository extends JpaRepository<User, Long> {

    // Used to validate duplicate email addresses
    Optional<User> findByEmail(String email);//and also a custom method used to search users by email

    // Used to validate duplicate phone numbers
    Optional<User> findByPhone(String phone);
}
