package com.infotact.project1.repository;

import com.infotact.project1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * Repository responsible for User database operations.
 *
 * Provides CRUD functionality and custom queries related to:
 * - User registration
 * - User management
 * - Authentication
 * - Duplicate account validation
 *
 * Each user represents a customer or administrator
 * of the hotel reservation system.
 */
@Repository

/*
 * JpaRepository provides:
 *
 * save()
 * findById()
 * findAll()
 * delete()
 * deleteById()
 * existsById()
 *
 * Entity Type : User
 * Primary Key : Long (userId)
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /*
     * Retrieves a user using their email address.
     *
     * Used for:
     * - Login authentication
     * - JWT authentication
     * - Duplicate email validation during registration
     * - User lookup operations
     *
     * Business Rule:
     * Every user must have a unique email address.
     *
     * Spring automatically generates:
     *
     * SELECT *
     * FROM users
     * WHERE email = ?;
     */
    Optional<User> findByEmail(String email);//and also a custom method used to search users by email

    /*
     * Retrieves a user using their phone number.
     *
     * Used for:
     * - Duplicate phone validation
     * - User lookup operations
     *
     * Business Rule:
     * Every user must have a unique phone number.
     *
     * Spring automatically generates:
     *
     * SELECT *
     * FROM users
     * WHERE phone = ?;
     */
    Optional<User> findByPhone(String phone);
}
