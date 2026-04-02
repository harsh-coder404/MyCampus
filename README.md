# MyCampus (Minor Project)

MyCampus is a full-stack academic app prototype with:
- **Android app (Jetpack Compose)** in this repository
- Planned **Spring Boot backend** (to be created in `/backend` when backend implementation starts)

## Current Project Status

This repo currently contains the Android application module (`app`) with role-based flows for student/professor screens under active development.

## Tech Stack

- Kotlin
- Jetpack Compose
- Android Navigation
- MVVM architecture (in-progress across screens)
- Gradle Kotlin DSL

## Project Structure

```
MinorApp/
├── app/                # Android app source
├── gradle/             # Gradle wrapper/config
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Prerequisites

- Android Studio (latest stable recommended)
- JDK 17 (or version required by your Android Gradle Plugin)
- Android SDK configured

## Run the App

1. Open `D:\MinorApp` in Android Studio.
2. Let Gradle sync finish.
3. Run the `app` configuration on an emulator/device.

Or from terminal:

```powershell
cd D:\MinorApp
.\gradlew.bat assembleDebug
```

## Notes

- Backend APIs are being scaffolded separately and integrated screen-by-screen.
- Session/auth behavior currently uses local session handling where backend endpoints are not yet wired.

## License

This is an academic minor project repository.

