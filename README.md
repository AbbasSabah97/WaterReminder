# Hydra Water Reminder

Android hydration reminder app built with Kotlin, Jetpack Compose, and Material 3.

## Version 1.0.4
- Migrated the project to Android Gradle Plugin 9 built-in Kotlin.
- Removed the legacy `org.jetbrains.kotlin.android` plugin from all Gradle build scripts.
- Added a CI guard that fails early if the legacy Kotlin Android plugin is accidentally reintroduced.
- GitHub Actions builds a debug APK on `ubuntu-24.04`.
- Uses JDK 17 and Gradle 9.4.1. AGP 9.2.0 requires Gradle 9.4.1.

## GitHub Actions
Run **Actions → Build Hydra APK → Run workflow**.
The APK is uploaded as the `Hydra-debug-apk` artifact.

## Android compatibility
- Min SDK: 24
- Target SDK: 35
- Compile SDK: 35

Android 11 (API 30) is supported.

## v1.0.5 build compatibility fix
This version intentionally keeps `compileSdk = 35` and pins AndroidX/Compose dependencies to versions compatible with that SDK level:
- Compose BOM `2025.02.00`
- Core KTX `1.15.0`
- Activity Compose `1.10.0`
- Lifecycle Runtime KTX `2.8.7`

This avoids the AAR metadata failure caused by newer 2026 libraries requiring compileSdk 36/37 while preserving `minSdk = 24` and Android 11 support.
