# BankingSecurity Microservices Application

BankingSecurity is a secure microservices-based banking application built with Spring Boot. This README focuses on two core services: the API Gateway and Authentication Service.

## System Architecture

The application consists of several microservices:

- **API Gateway Service** - Entry point for all client requests
- **Authentication Service** - Handles user registration, authentication, and authorization
- **Account Service** - Manages banking accounts (not covered in this README)
- **Transaction Service** - Processes banking transactions (not covered in this README)
- **Notification Service** - Manages alerts and notifications (not covered in this README)

## Tech Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Cloud Gateway
- JWT Authentication
- OAuth2 (Google)
- MySQL Database
- Maven

## Services

### 1. Gateway Service

The Gateway Service is the entry point for all client requests to the BankingSecurity microservices architecture. It routes incoming requests to the appropriate services and provides cross-cutting concerns like authentication, logging, and CORS handling.

#### Features

- Request routing based on paths
- JWT token validation
- CORS configuration
- Single point of entry for the microservices architecture

#### Configuration

The Gateway Service runs on port 8080 by default. Here's the key configuration:

**application.yml**:
```yaml
server:
  port: 8080

spring:
  application:
    name: GATEWAY-SERVICE

  jwt:
    secret: 1j5Jz2+wg1jIs6dVM4RBEdGAvopKfucrwf5h/VzNsJS/VbEsT/FO32FW+5Hd6uiwE2PQumMJTtxOvrtbi/kHyA==

cloud:
  gateway:
    routes:
      - id: auth-service
        uri: http://localhost:8081
        predicates:
          - Path=/api/auth/**
      - id: account-service
        uri: http://localhost:8082
        predicates:
          - Path=/api/accounts/**
      - id: transaction-service
        uri: http://localhost:8083
        predicates:
          - Path=/api/transactions/**
      - id: notification-service
        uri: http://localhost:8084
        predicates:
          - Path=/api/notifications/**
```

#### Routes

| Route | Target Service | Path Pattern |
|-------|---------------|--------------|
| auth-service | Authentication Service | `/api/auth/**` |
| account-service | Account Service | `/api/accounts/**` |
| transaction-service | Transaction Service | `/api/transactions/**` |
| notification-service | Notification Service | `/api/notifications/**` |

#### JWT Authentication

The Gateway Service validates JWT tokens for protected routes. The `JwtAuthenticationFilter` class handles token validation:

- Extracts JWT from the Authorization header
- Validates the token signature
- Checks if the token is expired
- Allows unauthenticated access to the auth-service endpoints
- Returns 401 Unauthorized for invalid tokens

### 2. Authentication Service

The Authentication Service manages user registration, authentication, and authorization. It provides JWT-based authentication and supports OAuth2 login with Google.

#### Features

- User registration and login
- JWT-based authentication
- Token refresh mechanism
- OAuth2 integration with Google
- Role-based authorization
- Token blacklisting for logout

#### Configuration

The Authentication Service runs on port 8081 by default. Here's the key configuration:

**application.yml**:
```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/auth_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
    username: root
    password:
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  application:
    name: AUTH-SERVICE
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: 1098580004709-ur38elak0t7i9c8gvmnunchhkpjje4dm.apps.googleusercontent.com
            client-secret: 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
            scope:
              - email
              - profile
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"

jwt:
  secret: EEQLjAtCuNXlnTiKAvGxrWOeNAQ1vDUFwYo9w2kmAzIPY8U/7bjeyUGEb4GKAgD3xZVVZtjM3zXibMWHndfU8A==
  accessToken:
    expiration: 900000  # 15 minutes
  refreshToken:
    expiration: 86400000  # 24 hours

frontend:
  url: http://localhost:3000

cors:
  allowed-origins: http://localhost:3000
```

#### Endpoints

| HTTP Method | Endpoint | Access | Description |
|-------------|----------|--------|-------------|
| GET | `/api/auth/` | Public | Check API status |
| POST | `/api/auth/register` | Public | Register a new user |
| POST | `/api/auth/login` | Public | Authenticate user and get tokens |
| POST | `/api/auth/refresh` | Public | Refresh access token |
| POST | `/api/auth/logout` | Protected | Logout user and blacklist token |
| GET | `/api/auth/profile` | Protected | Get user profile information |
| GET | `/oauth2/login/google` | Public | Initiate Google OAuth2 login flow |

#### Data Models

**User Registration Request**:
```json
{
  "fullname": "John Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "role": "USER"
}
```

**Login Request**:
```json
{
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

**Login Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "role": "USER"
}
```

**Refresh Token Request**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**User Profile Response**:
```json
{
  "email": "john.doe@example.com",
  "fullname": "John Doe",
  "role": "USER",
  "lastLogin": "2023-10-15T14:30:45.123"
}
```

#### JWT Token Structure

The JWT tokens contain the following claims:

- **Subject**: User's email
- **Role**: User's role (USER, EMPLOYEE, ADMIN)
- **Issued At**: Token creation timestamp
- **Expiration**: Token expiration timestamp

#### OAuth2 Flow

1. User initiates OAuth2 login by visiting `/oauth2/login/google`
2. User is redirected to Google for authentication
3. After successful authentication, user is redirected back to `/login/oauth2/code/google`
4. The `OAuth2SuccessHandler` processes the authentication:
   - Creates or updates the user in the database
   - Generates JWT tokens
   - Redirects to the frontend with tokens as URL parameters

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 21
- Maven
- MySQL

### Setup

1. Clone the repository
   ```
   git clone https://github.com/yourusername/bankingsecurity.git
   cd bankingsecurity
   ```

2. Configure MySQL
   ```
   # Create the database
   CREATE DATABASE auth_db;
   ```

3. Build the project
   ```
   mvn clean install
   ```

4. Start the services
   ```
   # Start Auth Service
   cd auth-service
   mvn spring-boot:run
   
   # Start Gateway Service (in a new terminal)
   cd gateway-service
   mvn spring-boot:run
   ```

## Testing the Services

### Authentication Service Testing

#### Register a New User
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullname": "John Doe",
    "email": "john.doe@example.com",
    "password": "securePassword123",
    "role": "USER"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "securePassword123"
  }'
```
This will return access and refresh tokens.

#### Get User Profile
```bash
curl -X GET http://localhost:8081/api/auth/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

#### Refresh Token
```bash
curl -X POST http://localhost:8081/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

#### Logout
```bash
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Gateway Service Testing

Test accessing a protected endpoint through the gateway:

```bash
# First, login to get a token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "securePassword123"
  }'

# Then, use the token to access a protected resource
curl -X GET http://localhost:8080/api/accounts/user \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Security Considerations

1. **JWT Secret**: Ensure you change the JWT secret key for production environments and store it securely.
2. **OAuth2 Credentials**: Update the OAuth2 client ID and client secret with your actual Google API credentials.
3. **Database Credentials**: Use strong database passwords in production and configure them via environment variables.
4. **CORS Configuration**: Update the CORS allowed origins to match your frontend domain in production.
5. **Token Blacklisting**: The current implementation stores blacklisted tokens in memory. Consider using Redis or another distributed cache for token blacklisting in a multi-instance environment.

## Future Enhancements

- Implement rate limiting in the gateway service
- Add circuit breakers for resilience
- Implement distributed tracing
- Add centralized logging
- Deploy with Docker containers and Kubernetes orchestration
