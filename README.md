# Construction Workforce HRMS Backend

## Overview

This project is a backend service for managing a construction workforce, designed for site managers, HR teams, and payroll operators. It provides functionality for managing workers, sites, attendance tracking, overtime calculations, and payroll-related data.

The system is built using Spring Boot, PostgreSQL (Supabase), Redis caching, and JPA/Hibernate, following RESTful API principles and layered architecture.

---

## Tech Stack

* Java 21
* Spring Boot 3
* Spring Data JPA
* PostgreSQL (Supabase)
* Redis
* Spring Cache
* Spring Security (CORS configuration)
* Maven
* Lombok

---

## Features

### Worker Management

* Create, update, retrieve, and manage workers
* Store designation, phone number, wage rate, and active status

### Site Management

* Create and manage construction sites
* Track worker attendance against sites

### Attendance Tracking

* Clock-in and clock-out records
* Daily attendance tracking
* Total hours worked calculation
* Attendance flagging support

### Overtime Management

* Overtime hour tracking
* Overtime rate storage
* Settlement status tracking
* Payroll amount calculation support

### Performance

* Redis-based caching for frequently accessed data
* Pagination support for large datasets

---

# Database Design

## Core Entities

### Workers

Stores worker master data including:

* Name
* Designation
* Phone
* Daily wage rate
* Active status

### Sites

Stores construction site information including:

* Site name
* Location
* Active status

### Attendance Logs

Represents a worker's attendance for a particular day and site.

Relationships:

* Many Attendance Logs → One Worker
* Many Attendance Logs → One Site

### Overtime Entries

Stores overtime generated from attendance records.

Relationships:

* Many Overtime Entries → One Worker
* Many Overtime Entries → One Attendance Log

---

# Setup Instructions

## Prerequisites

Install:

* Java 21+
* Maven 3.9+
* Redis
* PostgreSQL (or Supabase account)

---

## Clone Repository

```bash
git clone <repository-url>
cd <repository-name>
```

## Configure Environment Variables

Create the following environment variables:

```text
DB_URL=
DB_USERNAME=
DB_PASSWORD=

REDIS_HOST=
REDIS_PORT=
REDIS_PASSWORD=

CORS_ALLOWED_ORIGINS=
```

Example:

```text
DB_URL=jdbc:postgresql://localhost:5432/hrms
DB_USERNAME=postgres
DB_PASSWORD=password

REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

CORS_ALLOWED_ORIGINS=http://localhost:3000
```

---

## Supabase Setup

1. Create a Supabase project.
2. Navigate to:

Database → Connect

3. Copy the PostgreSQL connection details.

Example:

```text
Host: db.xxxxxxxxx.supabase.co
Port: 5432
Database: postgres
User: postgres
Password: <your-password>
```

4. Construct DB_URL:

```text
jdbc:postgresql://db.xxxxxxxxx.supabase.co:5432/postgres
```

5. Set the remaining credentials:

```text
DB_USERNAME=postgres
DB_PASSWORD=<your-password>
```

6. Start the application.

---

## Redis Setup

Run Redis locally:

```bash
redis-server
```

or configure Redis cloud credentials through the environment variables above.

---

## Run Application

```bash
mvn clean install
mvn spring-boot:run
```

Application starts on:

```text
http://localhost:8080
```

---

# API Design

The application follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

This separation keeps business logic independent from persistence concerns and improves maintainability.

---

# HRMS Reference

Forked Inspiration:

The design was inspired by https://github.com/amigoscode/spring-boot-fullstack-professional

The implementation itself was developed independently and adapted specifically for construction workforce management use cases.

---

# AI Tools Used

### ChatGPT

Used for:
1
* Architectural brainstorming
* API design review
* JPA relationship validation
* Edge case discussion
* Documentation assistance

All implementation, debugging, entity modeling, repository creation, service logic, and project integration were completed manually.

---

# Design Decisions

## Why Overtime References Both Worker and Attendance

Overtime records store:

* attendance_id
* worker_id

While worker information could be derived through attendance, storing worker_id directly simplifies reporting queries and payroll calculations while reducing unnecessary joins.

Trade-off:

* Slight data duplication
* Faster reporting and aggregation

---

## Why Redis Caching

Redis was added to support:

* Frequently requested reference data
* Reduced database load
* Faster API response times

For the assignment scope, caching demonstrates scalability considerations beyond basic CRUD operations.

---

## Why Soft Active Flags

Workers and sites contain an active flag instead of hard deletion.

Benefits:

* Preserves historical attendance data
* Maintains payroll auditability
* Prevents orphaned records

---

# What I Would Improve With More Time

### Authentication & Authorization

Implement:

* JWT authentication
* Role-based access control
* Site Manager / HR / Payroll permissions

### Audit Logging

Track:

* Record creation
* Updates
* Payroll modifications

### Automated Payroll Processing

Generate payroll summaries directly from:

* Attendance
* Overtime
* Wage rates

### Testing

Add:

* Unit tests
* Integration tests
* Testcontainers for PostgreSQL

### Production Readiness

* Flyway database migrations
* OpenAPI / Swagger documentation
* Centralized exception handling enhancements
* Metrics and monitoring

---

# Assumptions

* One attendance record per worker per day.
* Overtime is linked to a valid attendance record.
* Wage calculations are based on stored rates at the time of processing.
* Historical attendance records remain immutable for payroll consistency.

# Local Setup

## 1. Configure Environment Variables

### Windows (Command Prompt)

Replace the values below with your own credentials.

```cmd
setx DB_URL "jdbc:postgresql://YOUR_HOST:5432/postgres"
setx DB_USERNAME "postgres"
setx DB_PASSWORD "your-password"

setx REDIS_HOST "localhost"
setx REDIS_PORT "6379"
setx REDIS_PASSWORD ""

setx CORS_ALLOWED_ORIGINS "http://localhost:3000"
```

Close and reopen the terminal after running the commands.

Verify:

```cmd
echo %DB_URL%
echo %DB_USERNAME%
```

---

## 2. Build Project

```bash
mvn clean install
```

---

## 3. Run Application

```bash
mvn spring-boot:run
```

Application will start on:

```text
http://localhost:8080
```

---

# API Examples

Base URL:

```text
http://localhost:8080
```

---

# Attendance APIs

## Clock In

### Request

```bash
curl --location 'http://localhost:8080/api/attendance/clock-in' \
--header 'Content-Type: application/json' \
--data '{
    "workerId": 1,
    "siteId": 1
}'
```

---

## Clock Out

### Request

```bash
curl --location 'http://localhost:8080/api/attendance/clock-out' \
--header 'Content-Type: application/json' \
--data '{
    "workerId": 1
}'
```

---

## Get Active Workers

Returns workers currently clocked in and not yet clocked out.

```bash
curl --location 'http://localhost:8080/api/attendance/active'
```

---

## Get Attendance History

Returns paginated attendance history for a worker within a date range.

```bash
curl --location 'http://localhost:8080/api/attendance/log?workerId=1&from=2026-06-01&to=2026-06-30&page=0&size=20'
```

Example:

```bash
curl --location 'http://localhost:8080/api/attendance/log?workerId=1&from=2026-06-01&to=2026-06-30'
```

---

# Overtime APIs

## Get Monthly Overtime Summary

Month format:

```text
YYYY-MM
```

Example:

```text
2026-06
```

Request:

```bash
curl --location 'http://localhost:8080/api/overtime/summary/1?month=2026-06'
```

Where:

```text
1 = workerId
```

---

## Settle Monthly Overtime

Marks overtime entries for the specified worker and month as settled.

```bash
curl --location --request POST 'http://localhost:8080/api/overtime/settle/1?month=2026-06'
```

Where:

```text
1 = workerId
```

---

# Worker APIs

## Update Worker

Used to update worker information and demonstrate cache invalidation behavior.

```bash
curl --location --request PUT 'http://localhost:8080/api/workers/1' \
--header 'Content-Type: application/json' \
--data '{
    "name":"Ramesh Kumar",
    "phoneNumber":"9876543210",
    "designation":"SUPERVISOR",
    "dailyWageRate":1200,
    "active":true
}'
```

Where:

```text
1 = workerId
```

---

# Example Workflow

## Step 1 - Worker Clocks In

```bash
curl --location 'http://localhost:8080/api/attendance/clock-in' \
--header 'Content-Type: application/json' \
--data '{
    "workerId":1,
    "siteId":1
}'
```

## Step 2 - Verify Active Workers

```bash
curl --location 'http://localhost:8080/api/attendance/active'
```

## Step 3 - Worker Clocks Out

```bash
curl --location 'http://localhost:8080/api/attendance/clock-out' \
--header 'Content-Type: application/json' \
--data '{
    "workerId":1
}'
```

## Step 4 - View Attendance History

```bash
curl --location 'http://localhost:8080/api/attendance/log?workerId=1&from=2026-06-01&to=2026-06-30'
```

## Step 5 - View Overtime Summary

```bash
curl --location 'http://localhost:8080/api/overtime/summary/1?month=2026-06'
```

## Step 6 - Settle Overtime

```bash
curl --location --request POST 'http://localhost:8080/api/overtime/settle/1?month=2026-06'
```


---

# Author

Karan Singh

Java Backend Developer Assignment Submission
