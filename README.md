# Bitespeed Identity Reconciliation Service

A Spring Boot microservice that implements identity reconciliation for e-commerce platforms. This service links different contact information (email and phone number) to identify the same customer across multiple purchases.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Testing](#testing)
- [Deployment](#deployment)
- [Examples](#examples)

## Overview

The Identity Reconciliation Service helps e-commerce platforms like FluxKart.com identify and link customer identities across multiple purchases. When customers use different email addresses or phone numbers for different orders, this service maintains the relationship between these identities.

## Features

- **Identity Linking**: Automatically links contacts based on shared email or phone number
- **Primary/Secondary Hierarchy**: Maintains oldest contact as primary, others as secondary
- **Contact Consolidation**: Returns consolidated view of all linked contact information
- **Conflict Resolution**: Handles cases where multiple primary contacts need to be merged
- **RESTful API**: Simple HTTP POST endpoint for identity resolution

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Data JPA**
- **H2 Database**
- **Maven**

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd identity-reconciliation
```

2. Build the project:
```bash
mvn clean compile
```

3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `https://bitespeed-identity-service-wtr6.onrender.com`


## API Documentation

### POST /identify

Identifies and consolidates contact information.

#### Request

```json
{
  "email": "string (optional)",
  "phoneNumber": "string (optional)"
}
```

**Note**: At least one of `email` or `phoneNumber` must be provided.

#### Response

```json
{
  "contact": {
    "primaryContatctId": number,
    "emails": ["string"],
    "phoneNumbers": ["string"],
    "secondaryContactIds": [number]
  }
}
```

#### Status Codes

- `200 OK`: Successfully identified and consolidated contact
- `400 Bad Request`: Invalid request (missing both email and phone)
- `500 Internal Server Error`: Server error

### GET /health

Health check endpoint.

#### Response

```
Identity Reconciliation Service is running
```

## Database Schema

### Contact Table

| Field | Type | Description |
|-------|------|-------------|
| id | BIGINT (Primary Key) | Unique identifier |
| phone_number | VARCHAR | Phone number |
| email | VARCHAR | Email address |
| linked_id | BIGINT | ID of linked primary contact |
| link_precedence | ENUM | 'PRIMARY' or 'SECONDARY' |
| created_at | TIMESTAMP | Creation timestamp |
| updated_at | TIMESTAMP | Last update timestamp |
| deleted_at | TIMESTAMP | Soft delete timestamp |


### Examples

#### Scenario 1: New Customer
**Request:**
```json
{
  "email": "doc@hillvalley.edu",
  "phoneNumber": "555-0199"
}
```

**Result:** Creates new primary contact

#### Scenario 2: Existing Customer, New Information
**Existing:** Email: doc@hillvalley.edu, Phone: 555-0199
**Request:**
```json
{
  "email": "emmett@hillvalley.edu", 
  "phoneNumber": "555-0199"
}
```

**Result:** Creates secondary contact linked to existing primary

#### Scenario 3: Linking Two Primary Contacts
**Existing:** 
- Contact 1: Email: doc@hillvalley.edu (Primary)
- Contact 2: Phone: 555-0199 (Primary)

**Request:**
```json
{
  "email": "doc@hillvalley.edu",
  "phoneNumber": "555-0199"
}
```

**Result:** Older contact remains primary, newer becomes secondary

## Testing

### Manual Testing with curl

1. **Create first contact:**
```bash
curl -X POST http://localhost:8080/identify \
  -H "Content-Type: application/json" \
  -d '{"email": "lorraine@hillvalley.edu", "phoneNumber": "123456"}'
```

2. **Create linked contact:**
```bash
curl -X POST http://localhost:8080/identify \
  -H "Content-Type: application/json" \
  -d '{"email": "mcfly@hillvalley.edu", "phoneNumber": "123456"}'
```

3. **Test consolidation:**
```bash
curl -X POST http://localhost:8080/identify \
  -H "Content-Type: application/json" \
  -d '{"email": "lorraine@hillvalley.edu"}'
```



## Deployment

## 🚀 Live Demo

**POST** [`/identify`](https://identity-reconciliation.onrender.com/identify)

> Replace the base URL with your actual Render deployment URL once hosted.

### ✅ Sample Request:

```json
{
  "email": "lorraine@hillvalley.edu",
  "phoneNumber": "123456"
}

## Production Configuration

For production deployment, update `application.properties`:

```properties
# Production Database (PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/identity_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Disable H2 console
spring.h2.console.enabled=false

# Production logging
logging.level.com.bitespeed.identity=INFO
logging.level.org.springframework.web=WARN
```

## Environment Variables

Set these environment variables for production:

- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password  
- `DB_URL`: Database connection URL

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## License

This project is licensed under the MIT License.

## Contact

For questions or support, please contact the development team.

---

**Live Endpoint**: `http://localhost:8080/identify` (when running locally)

**Health Check**: `http://localhost:8080/health` 
