# Hotel Reservation Engine

A full-stack Hotel Reservation System built using Spring Boot, PostgreSQL, Redis, and React. The application provides a complete hotel booking workflow, including secure authentication, room reservation, payment processing, guest management, reception operations, and automatic booking hold expiration using Redis.

---

## Tech Stack

### Backend
- Java 17
- Spring Boot 3.5.15
- Spring Data JPA
- Spring Security (JWT Authentication)
- Spring Validation
- PostgreSQL
- Redis
- Redisson (Distributed Locking)
- Maven

### Frontend
- React
- TypeScript
- Tailwind CSS
- Axios

### DevOps & Tools
- Docker
- Docker Compose
- Swagger / OpenAPI
- JUnit 5
- Mockito
- GitHub Actions (CI)
- Postman

---

# Features

## Authentication & Authorization

- User Registration
- Secure Login
- JWT Authentication
- BCrypt Password Encryption
- Role-based Authorization
- Customer, Receptionist and Admin Roles

---

## User Management

- Create User
- Update User
- Delete User
- View User Details
- Profile Management

---

## Room Type Management

- Create Room Type
- Update Room Type
- Delete Room Type
- Room Capacity Validation
- Price Management

---

## Room Management

- Create Room
- Update Room
- Delete Room
- Room Status Management
- Maintenance Support

---

## Availability Module

- Search Available Rooms
- Room Availability Validation
- Booking Hold Consideration
- Prevent Double Booking

---

## Reservation Module

- Create Reservation
- View Reservation
- Update Reservation
- Delete Reservation
- Reservation Status Management
- Guest Count Validation
- Room Capacity Validation
- Automatic Payment Creation

Reservation Statuses:

- Pending
- Confirmed
- Cancelled
- Checked In
- Checked Out
- Expired

---

## Booking Hold Module (Redis)

- Temporary Booking Hold
- Redis-based Hold Storage
- Automatic TTL Expiration
- Event-driven Hold Expiration
- Redis Key Expiration Listener
- Automatic Reservation Expiration
- Hold Release after Successful Payment
- Hold Release after Failed Payment
- Distributed Locking using Redisson

---

## Payment Module

- Payment Creation
- Payment Status Tracking
- Payment Validation
- Reservation Confirmation after Payment
- Refund Processing
- Reservation Cancellation after Refund

---

## Guest Module

- Guest Registration
- Guest CRUD Operations
- Guest Count Validation
- Guest Verification

---

## Reception Module

- Reception Dashboard
- Today's Arrivals
- Current Guests
- Today's Departures
- Room Assignment
- Guest Check-In
- Guest Check-Out

---

## Exception Handling

- Centralized Exception Handling
- Business Exception Support
- Validation Error Handling
- Authentication Error Handling
- Data Integrity Exception Handling
- Consistent API Error Responses

---

## Security

- JWT Token Authentication
- Password Encryption
- Protected Endpoints
- Role-based Access Control

---

## API Documentation

- Swagger UI
- OpenAPI Documentation
- Exportable API Documentation (PDF)

---

## Testing

- Unit Testing with JUnit 5
- Mockito-based Service Tests
- Repository Mocking
- Controller Testing
- GitHub Actions Continuous Integration

---

# Architecture

- Layered Architecture

```
Controller
      │
      ▼
Service Layer
      │
      ▼
Repository Layer
      │
      ▼
PostgreSQL
```

Redis is used independently for:

- Booking Holds
- Key Expiration Events
- Distributed Locking

---

# Project Highlights

- Layered Architecture
- DTO-based Request/Response Models
- Clean Exception Handling
- JWT Authentication
- Redis-based Booking Hold Management
- Distributed Locking
- Event-driven Reservation Expiration
- Automatic Payment Workflow
- Swagger API Documentation
- Docker Support
- GitHub Actions CI Pipeline
- Comprehensive Unit Testing

---

# Database

- PostgreSQL for persistent data
- Redis for temporary booking holds and distributed locking

---

# Continuous Integration

GitHub Actions automatically:

- Builds the project
- Executes JUnit Tests
- Verifies every push

---

# API Documentation

Swagger UI is available after running the application.

```
http://localhost:8080/swagger-ui/index.html
```

---

# Running the Project

### Clone Repository

```bash
git clone <repository-url>
```

### Backend

```bash
cd Backend/project1
./mvnw spring-boot:run
```

### Frontend

```bash
cd Frontend
npm install
npm run dev
```

---

# Contributors

- Shrikanth S Sanagoudar
- Spandana V

---

# License

This project was developed as part of the Infotact Internship program
