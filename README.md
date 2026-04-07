# MyCampus (Minor Project)

MyCampus is a full-stack academic app with:
- `Android` client built using Jetpack Compose (`app/`)
- `Spring Boot` backend API with JWT security (`backend/`)
- `PostgreSQL` persistence for authentication and user data

## Current Scope

- Role-based app flow for `STUDENT` and `PROFESSOR`
- Splash -> Login -> role-specific dashboard/navigation
- Backend-only authentication (`/auth/login`, `/auth/register`) with JWT tokens
- Forgot-password identity verification + password update in DB
- Session handling on Android with `Remember for 30 days` (`now - loginTime <= 30d`)
- Seeded class data (`CSE-A`, `IT`, `DSA`) and seeded student credentials for testing
- Student task submission flow and professor checklist flow wired to backend APIs
- Professor-side checklist auto-refresh polling enabled (`5s`, configurable)

## Tech Stack

- Kotlin, Jetpack Compose, Navigation, MVVM (Android)
- Java, Spring Boot, Spring Security, JWT, Spring Data JPA (Backend)
- PostgreSQL
- Gradle wrapper (`gradlew`, `gradlew.bat`) and Maven wrapper (`mvnw`, `mvnw.cmd`)

## Project Structure

```text
MinorApp/
|-- app/                      # Android app
|-- backend/                  # Spring Boot backend
|   |-- src/main/java/com/example/minorapp/
|   |-- src/main/resources/
|   |-- pom.xml
|   |-- mvnw
|   \-- mvnw.cmd
|-- gradle/
|-- build.gradle.kts
|-- settings.gradle.kts
\-- README.md
```

## Backend Auth APIs (Implemented)

- `POST /auth/login`
- `POST /auth/register`
- `POST /auth/forgot-password/verify`
- `POST /auth/change-password`

Response contract follows `status`, `message`, and `data` (token/user payload on success).

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

- `student@abc.com` / `Stu@12` for student login
- `proff@abc.com` / `Proff@12` for proffessor login
- Additional seeded students are auto-created from class rosters using:
  - Email pattern: `<name_or_nameN>_@abc.com`
  - Password pattern: `<Name_or_NameN>_A@12`
  - Example: `harsh_@abc.com` / `Harsh_A@12`

## End-to-End Test Flow

- Login as professor (`proff@abc.com`) in Android app.
- From professor task flow, create a new task (`POST /tasks`) for a class.
- Login as student (for example `harsh_@abc.com`), open tasks, and submit PDF (`POST /submissions`).
- Re-open professor checklist for that task; auto-refresh (`5s`) should reflect updated submission status via `GET /submissions/task/{id}/checklist`.

## Prerequisites

- Android Studio (latest stable recommended)
- JDK 17+ (project has been built successfully with newer JDK as well)
- Android SDK configured
- PostgreSQL running locally (or reachable remote instance)

## Backend Setup and Run

1. Create a PostgreSQL database named `mycampus`.
2. Configure DB/JWT values (optional via environment variables):
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - `JWT_ACCESS_SECRET`
   - `JWT_REFRESH_SECRET`
3. Start backend from `backend/`:

```powershell
Set-Location "D:\MinorApp\backend"
.\mvnw.cmd spring-boot:run
```

4. Build backend JAR (optional):

```powershell
Set-Location "D:\MinorApp\backend"
.\mvnw.cmd -DskipTests package
```

## Android Setup and Run

1. Ensure backend is running.
2. Point Android auth base URL to your backend host.
   - Current project config in `app/build.gradle.kts`: `AUTH_BASE_URL = "http://10.0.2.2:8081/"`
   - For Android emulator, use `10.0.2.2` to reach host machine localhost.
   - Backend default port in `backend/src/main/resources/application.properties` is `8080` unless overridden by `SERVER_PORT`.
3. Build/run app:

```powershell
Set-Location "D:\MinorApp"
.\gradlew.bat assembleDebug
```

Or open `D:\MinorApp` in Android Studio and run the `app` configuration.

## Notes

- Backend package namespace is `com.example.minorapp`.
- Professor checklist polling interval constant is `app/src/main/java/com/example/minorapp/domain/constants/AppTimingConstants.kt`.
- Root doc is onboarding-focused; backend API details are in `backend/README.md`.
- This repository is maintained as an academic minor project MVP and is intended for iterative expansion.
