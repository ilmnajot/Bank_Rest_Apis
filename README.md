# Bank Cards Management System 💳

## Overview
This project is a **Bank Card Management System** built with Spring Boot.  
It provides functionality for **managing users, bank cards, and transactions**.  
The system implements **role-based access control (RBAC)** using Spring Security and JWT authentication.

---

## Features

### 🔑 Authentication & Authorization
- User registration and login
- Only admin registers the user and provides them with a password (`default role is `user``)
- JWT-based authentication
- Role-based access control (`ADMIN`, `USER`)
- Secure password hashing with BCrypt

### 👤 User Management
- Create and update employee accounts (admin)
- Change user credentials
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
- `POST /auths/login` – Login with username & password
- `POST /auths/register-user` – Register new user (admin only)
- `PUT /auths/change-password` – Change password
- `PUT /autha/change-credentials/{id}` – Change username/email
- `Put /auths/update-user-by-admin` - Change users' data by admin only
- `Get /get-user/{userId}` - getting a single user

### Card Management
- `POST /api/v1/cards/add-card` – Create a new card
- `GET /api/v1/cards/get/{id}` – Get card details
- `PUT /api/v1/cards/update-card/{cardId}` – Update card
- `DELETE /api/v1/cards/delete-card/{cardId}` – Delete card (soft delete)
- `GET /api/v1/cards/get-own-cards` – Get user’s own cards
- `PATCH /api/v1/cards/change-card-status/{cardId}` – Change card status
- `POST /api/v1/cards/fill-card` – Fill card with money
- `POST /api/v1/cards/transfer-money` – Transfer money between own cards
- `GET /api/v1/cards/get-balance` – Check balance
- `GET /api/v1/cards/get-card-details/{cardId}` – Get the user’s own card's details like real numbers
- `GET /api/v1/cards/get-all-cards` - get all cards

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
