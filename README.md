# SnapLink Pro - URL Shortener with Analytics

## Overview

SnapLink Pro is a production-ready URL shortener REST API built with Spring Boot. It allows users to convert long URLs into short, shareable links while tracking click analytics and usage statistics.

The application is designed with security, scalability, and maintainability in mind, featuring JWT authentication, Redis caching, rate limiting, analytics tracking, and comprehensive automated testing.

---

## Features

### Authentication & Security

* User registration and login
* JWT-based authentication
* Protected API endpoints
* Role-based authorization support
* Global exception handling

### URL Management

* Shorten long URLs
* Custom aliases
* Delete owned URLs
* User-specific URL management
* Expiration support

### Redirect Engine

* Fast URL redirection
* Redis-backed URL caching
* Click tracking
* Rate limiting protection

### Analytics

* Total click statistics
* Browser analytics
* Device analytics
* Click trend reporting
* Top performing URLs

### Dashboard

* Total URLs
* Active URLs
* Expired URLs
* Total clicks

---

## Tech Stack

### Backend

* Java 21
* Spring Boot 3
* Spring Security
* Spring Data JPA
* Hibernate

### Database

* PostgreSQL
* H2 (Testing)

### Caching

* Redis

### Authentication

* JWT (JSON Web Token)

### Documentation

* OpenAPI / Swagger

### Testing

* JUnit 5
* Mockito
* Spring Boot Test
* MockMvc

### Deployment

* Docker
* Docker Compose

---

## Architecture

SnapLink Pro follows a layered architecture:

Controller Layer
↓
Service Layer
↓
Repository Layer
↓
Database

Supporting Components:

* JWT Authentication
* Redis Cache
* Analytics Event Processing
* Rate Limiting
* Global Exception Handling

---

## API Endpoints

### Authentication

POST /api/auth/register

POST /api/auth/login

### URLs

POST /api/urls

GET /api/urls

DELETE /api/urls/{id}

### Redirect

GET /{shortCode}

### Analytics

GET /api/analytics/{urlId}

GET /api/analytics/top-urls

GET /api/analytics/{urlId}/browsers

GET /api/analytics/{urlId}/devices

GET /api/analytics/{urlId}/trends

### Dashboard

GET /api/dashboard/summary

---

## Running Locally

### Clone Repository

git clone <repository-url>

cd snaplink-pro

### Start Dependencies

docker compose up -d

### Run Application

mvn spring-boot:run

### Run Tests

mvn clean test

---

## Test Results

Latest Test Execution (2026-06-03)

BUILD SUCCESS

Tests Run: 44

Failures: 0

Errors: 0

Skipped: 0

Execution Time: 46.286 seconds

Completed Integration Tests:

* AuthController
* UrlController
* RedirectController
* AnalyticsController
* DashboardController

Completed Unit Tests:

* AuthService
* UrlService
* DashboardService
* RateLimitService

---

## Future Improvements

* Custom domains
* QR code generation
* User roles and administration
* Geo-location analytics
* API rate limit dashboard
* Testcontainers integration
* GitHub Actions CI/CD pipeline

---

## Author

Udoka Excellence

Software Engineer | Java & Spring Boot Developer
