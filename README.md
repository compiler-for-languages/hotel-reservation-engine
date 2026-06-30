# Hotel Reservation Engine

A backend application for managing hotel reservations, payments, room allocation, and guest check-in/check-out workflows. The project is built using Spring Boot with PostgreSQL and Redis, following a layered architecture.

## Tech Stack

- Java 17
- Spring Boot 3.5.15
- Spring Data JPA
- Spring Security (JWT Authentication)
- PostgreSQL
- Redis
- Redisson (Distributed Locking)
- Docker
- Maven

## Features Implemented

### Authentication
- User Registration
- User Login
- JWT Token Generation
- BCrypt Password Encryption

### User Management
- CRUD Operations
- Customer, Receptionist and Admin Roles

### Room & Room Type Management
- CRUD Operations
- Room Type Capacity Validation
- Room Status Management

### Availability
- Room Availability Check
- Customer Availability Search
- Active Booking Hold Consideration

### Reservation
- Reservation Creation
- Reservation Status Management
- Capacity Validation
- Guest Count Validation
- Automatic Payment Creation

### Booking Hold
- Temporary Booking Hold using Redis
- Distributed Locking using Redisson
- Automatic Hold Release on Payment Success/Failure
- Event-driven Hold Expiry using Redis Key Expiration

### Payment
- Payment Lifecycle
- Reservation Confirmation on Successful Payment
- Refund Workflow
- Reservation Cancellation after Refund

### Guest Management
- Guest CRUD Operations
- Guest Count Validation
- Check-in Guest Verification

### Reception Module
- Reception Dashboard
- Today's Arrivals
- Current Guests
- Today's Departures
- Room Assignment
- Guest Check-in
- Guest Check-out

## Architecture

- RESTful APIs
- Layered Architecture (Controller → Service → Repository)
- DTO-based Request/Response Models
- Redis for Booking Holds
- PostgreSQL for Persistent Data
- Distributed Locking to Prevent Double Booking

