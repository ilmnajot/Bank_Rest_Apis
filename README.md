# Bank Cards Management System 💳

## Overview
This project is a **Bank Card Management System** built with Spring Boot.  
It provides functionality for **managing users, bank cards, and transactions**.  
The system implements **role-based access control (RBAC)** using Spring Security and JWT authentication.

---

## Features

### 🔑 Authentication & Authorization
- User registration and login
- JWT-based authentication
- Role-based access control (`ADMIN`, `USER`, etc.)
- Secure password hashing with BCrypt

### 👤 User Management
- Create and update employee accounts (admin)
- Change user credentials (username, password)
- View user details
- User status management (`ACTIVE`, `BLOCKED`, etc.)

### 💳 Card Management
- Create new cards with encrypted card numbers
- Masked card number display
- View card details (with decrypted number)
- Update or delete cards (soft delete)
- Change card status (`ACTIVE`, `BLOCKED`, `EXPIRED`)
- Automatic card expiration check (scheduled job)

### 💰 Transactions
- Fill a card with balance (deposit)
- Transfer money between user cards
- Transaction history with type (`DEPOSIT`, `TRANSFER`)
- Balance checking

### 📊 Card Operations
- Retrieve all cards with filtering & pagination
- View only personal cards (authenticated user)
- Card validation (block/expire checks before transactions)

---

## Technology Stack
- **Java 17+**
- **Spring Boot 3**
- **Spring Security + JWT**
- **Spring Data JPA**
- **PostgreSQL** (or MySQL)
- **Liquibase** for DB migrations
- **Docker / Docker Compose**
- **Swagger (OpenAPI)** for API documentation
- **JUnit + MockMvc** for testing

---

## API Endpoints (Examples)

### Authentication
- `POST /api/auth/login` – Login with username & password
- `POST /api/auth/register` – Register new user (admin only)
- `PUT /api/auth/change-password/{id}` – Change password
- `PUT /api/auth/change-credentials/{id}` – Change username/email

### Card Management
- `POST /api/cards` – Create a new card
- `GET /api/cards/{id}` – Get card details
- `PUT /api/cards/{id}` – Update card
- `DELETE /api/cards/{id}` – Delete card (soft delete)
- `GET /api/cards/my` – Get user’s own cards
- `PATCH /api/cards/{id}/status` – Change card status
- `POST /api/cards/fill` – Fill card with money
- `POST /api/cards/transfer` – Transfer money between cards
- `GET /api/cards/{id}/balance` – Check balance

---

## Database
- `users` – stores user accounts (with roles & status)
- `cards` – stores encrypted card numbers, balance, status
- `transactions` – stores transaction history

---

## Security
- Password encryption with **BCrypt**
- JWT token-based authentication
- Role-based endpoint access
- Validation for card operations (blocked, expired)

---

## Running the Project

### Prerequisites
- JDK 17+
- Maven
- Docker & Docker Compose
- PostgreSQL

### Steps
1. Clone the repository:
```bash
git clone <repository-link>
