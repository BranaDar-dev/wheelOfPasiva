# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Sync after any gradle file changes (REQUIRED)
./gradlew --refresh-dependencies

# Build Android APK
./gradlew :composeApp:assembleDebug

# Full build (all platforms)
./gradlew :composeApp:build

# Run tests
./gradlew :composeApp:test

# Run a single test class
./gradlew :composeApp:test --tests "com.bramish.wheelofpasiva.SomeTestClass"

# Clean build
./gradlew clean :composeApp:build
```

For iOS, open `iosApp/iosApp.xcodeproj` in Xcode and run from there.

## Architecture

This is a Kotlin Multiplatform project using **MVVM + Clean Architecture**. Code is shared between Android and iOS via Compose Multiplatform.

### Layer Structure

```
composeApp/src/commonMain/kotlin/com/bramish/wheelofpasiva/
├── presentation/     # ViewModels, UI state, Compose screens
├── domain/          # Use cases, models, repository interfaces
├── data/            # Repository implementations, DTOs, data sources
├── di/              # Manual dependency injection (AppContainer)
└── firebase/        # Firebase manager (expect/actual pattern)
```

**Dependency Rule**: Presentation → Domain ← Data (domain has no dependencies on other layers)

### Platform-Specific Code

Uses Kotlin's `expect/actual` pattern for platform implementations:
- `commonMain/` - Shared code with `expect` declarations
- `androidMain/` - Android `actual` implementations
- `iosMain/` - iOS `actual` implementations

Key platform-specific files:
- `FirestoreDataSource` - Firebase Firestore operations
- `FirebaseManager` - Analytics, crashlytics initialization
- `QrCodeScanner` - Camera/barcode scanning

### Dependency Injection

Manual DI via `AppContainer` class - no external DI framework. ViewModels are provided via factory methods.

### Navigation

Custom state-based navigation in `presentation/navigation/SimpleNavigation.kt` (avoids navigation-compose iOS linkage issues).

## Key Patterns

- **MVVM**: ViewModels expose `StateFlow<UiState>`, views observe and dispatch actions
- **Use Cases**: Single-purpose business operations with `operator fun invoke()`
- **Repository Pattern**: Abstract interfaces in domain, implementations in data layer
- **Sealed Classes**: For UI state (Loading/Success/Error) and navigation events

## Gradle Sync Rule

**ALWAYS run `./gradlew --refresh-dependencies` after modifying:**
- Any `build.gradle.kts`
- `settings.gradle.kts`
- `gradle.properties`
- `gradle/libs.versions.toml`

## Code Standards

- Use `val` over `var` (immutability)
- Use coroutines/Flow for async operations
- Keep Firebase code in Data layer only
- Domain layer must remain platform-agnostic
- Use sealed classes for state representation
