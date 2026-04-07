# MyCampus Backend

MyCampus backend is a Spring Boot API with:
- `JWT`-secured authentication and role-aware access control
- `PostgreSQL` persistence via Spring Data JPA
- Layered backend design under `com.example.minorapp`

## Current Scope

- Real-time DB-backed authentication for login/register flows
- Forgot-password identity verification (`email + rollNumber`)
- Password change persisted in PostgreSQL
- Seed data for quick student/professor login testing
- Seeded classes and enrollments (`CSE-A`, `IT`, `DSA`) with 10 students each
- Task creation/submission APIs used by both student and professor app flows
- Professor submission checklist API (used with client-side auto-refresh polling)
- Extendable endpoints for courses, attendance, and library

## Tech Stack

- Java, Spring Boot, Spring Web
- Spring Security + JWT (filter chain + token utility)
- Spring Data JPA + Hibernate
- PostgreSQL
- Maven wrapper (`mvnw`, `mvnw.cmd`)

## Project Structure

```text
backend/
|-- src/main/java/com/example/minorapp/
|   |-- MyCampusApplication.java
|   |-- config/
|   |-- controller/
|   |-- service/
|   |-- repository/
|   |-- model/
|   |-- dto/
|   \-- util/
|-- src/main/resources/
|   \-- application.properties
|-- pom.xml
|-- mvnw
\-- mvnw.cmd
```

## Backend Auth APIs (Implemented)

- `POST /auth/login`
- `POST /auth/register`
- `POST /auth/forgot-password/verify`
- `POST /auth/change-password`

Response contract follows `status`, `message`, and `data` (token/user payload on success).

### Login Request Example

```json
{
  "email": "student@abc.com",
  "password": "Stu@12"
}
```

### Register Request Example

```json
{
  "email": "newuser@abc.com",
  "password": "New@1234",
  "rollNumber": "STU23101",
  "role": "STUDENT",
  "name": "Optional Name"
}
```

`name` is optional; backend falls back to `rollNumber` if not provided.

## Other APIs

- Implemented task/submission endpoints:
  - `POST /tasks`, `GET /tasks/course/{id}`
  - `GET /tasks/my`, `GET /tasks/professor/my`
  - `POST /submissions`, `GET /submissions/student/{id}`
  - `GET /submissions/task/{id}/checklist`
- Scaffolded/extendable endpoints:
  - `POST /courses`, `GET /courses`
  - `POST /attendance/mark`, `GET /attendance/student/{id}`
  - `GET /library/books`

## Test Credentials (Seeded)

- `student@abc.com` / `Stu@12`
- `proff@abc.com` / `Proff@12`
- Additional seeded students are auto-created from class rosters using:
  - Email pattern: `<name_or_nameN>_@abc.com`
  - Password pattern: `<Name_or_NameN>_A@12`
  - Example: `harsh_@abc.com` / `Harsh_A@12`

## End-to-End Test Flow

- Authenticate as professor (`proff@abc.com`) using `POST /auth/login`.
- Create a class task with `POST /tasks`.
- Authenticate as a student (for example `harsh_@abc.com`) and submit work using `POST /submissions`.
- Verify professor checklist updates through `GET /submissions/task/{id}/checklist` (Android client polls every `5s`).

## Prerequisites

- JDK 17+
- PostgreSQL running locally (or reachable remote instance)
- Database named `mycampus`

## Backend Setup and Run

1. Configure DB/JWT values (optional via environment variables):
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - `JWT_ACCESS_SECRET`
   - `JWT_REFRESH_SECRET`
2. Start backend:

```powershell
Set-Location "D:\MinorApp\backend"
.\mvnw.cmd spring-boot:run
```

3. Build backend JAR (optional):

```powershell
Set-Location "D:\MinorApp\backend"
.\mvnw.cmd -DskipTests package
```

On macOS/Linux:

```bash
cd /path/to/MinorApp/backend
./mvnw spring-boot:run
```

## Android Setup and Run

Use the root project guide in `README.md` for Android setup and run steps.

Current Android auth base URL is configured in `app/build.gradle.kts` as `http://10.0.2.2:8081/`.
If backend runs on default `8080`, either:
- update `AUTH_BASE_URL` in Android, or
- set `SERVER_PORT=8081` when starting backend.

## Notes

- Backend package namespace is `com.example.minorapp`.
- Root onboarding/documentation is in `README.md`.
- This backend is MVP-focused and ready for iterative expansion of role rules and domain validations.
