# Wheel of Pasiva

A Kotlin Multiplatform project targeting Android and iOS with Firebase integration.

## 🚀 Tech Stack

- **Kotlin Multiplatform** (Kotlin 2.3.0)
- **Compose Multiplatform** for shared UI
- **Firebase** (Analytics, Crashlytics, Firestore)
- **Swift Package Manager** for iOS dependencies

## 📋 Prerequisites

- JDK 11 or higher
- Android Studio or IntelliJ IDEA
- Xcode 15+ (for iOS development)
- Kotlin 2.3.0 (Do not upgrade)

## 🔧 Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/wheelOfPasiva.git
cd wheelOfPasiva
```

### 2. Firebase Configuration

⚠️ **Important:** Firebase configuration files are not included in the repository for security reasons.

#### Android
1. Download `google-services.json` from [Firebase Console](https://console.firebase.google.com/)
2. Place it in: `composeApp/src/androidMain/google-services.json`

#### iOS
1. Download `GoogleService-Info.plist` from [Firebase Console](https://console.firebase.google.com/)
2. Open `iosApp/iosApp.xcodeproj` in Xcode
3. Add `GoogleService-Info.plist` to the project (make sure to check "Copy items if needed")

### 3. Sync Dependencies

```bash
./gradlew --refresh-dependencies
```

## 🏗️ Building

### Android

```bash
# Debug build
./gradlew :composeApp:assembleDebug

# Release build
./gradlew :composeApp:assembleRelease
```

### iOS

```bash
# Build Kotlin framework for simulator
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Build Kotlin framework for device
./gradlew :composeApp:linkDebugFrameworkIosArm64

# Then open in Xcode
open iosApp/iosApp.xcodeproj
```

## 📱 Running

### Android
- Use Android Studio's run configuration, or
- Run `./gradlew :composeApp:installDebug` and launch the app

### iOS
1. Build the Kotlin framework (see above)
2. Open `iosApp/iosApp.xcodeproj` in Xcode
3. Select a simulator or device
4. Press ⌘R to build and run

## 📚 Documentation

- [Architecture Guidelines](ARCHITECTURE_GUIDELINES.md) - MVVM, SOLID principles, design patterns, TDD
- [Firebase Setup](FIREBASE_SETUP.md) - Complete Firebase integration guide
- [SPM Migration](MIGRATE_TO_SPM.md) - CocoaPods to Swift Package Manager migration

## 🏛️ Architecture

The project follows Clean Architecture principles with MVVM pattern:

```
commonMain/
  └── com/bramish/wheelofpasiva/
      ├── data/          # Data layer (repositories, data sources)
      ├── domain/        # Domain layer (models, use cases)
      └── presentation/  # Presentation layer (UI, ViewModels)
```

See [ARCHITECTURE_GUIDELINES.md](ARCHITECTURE_GUIDELINES.md) for detailed information.

## 🔥 Firebase Features

- **Analytics** - Track user events and behaviors
- **Crashlytics** - Monitor app crashes and errors
- **Firestore** - Cloud database for real-time data

### Using Firebase

```kotlin
import com.bramish.wheelofpasiva.firebase.FirebaseManager

val firebaseManager = FirebaseManager()

// Log analytics event
firebaseManager.logEvent("button_clicked", mapOf("button_name" to "play"))

// Log exception
firebaseManager.logException(exception)

// Set user ID
firebaseManager.setUserId("user123")
```

## 🧪 Testing

### Run Unit Tests

```bash
./gradlew test
```

### Run Android Tests

```bash
./gradlew :composeApp:connectedAndroidTest
```

See [ARCHITECTURE_GUIDELINES.md](ARCHITECTURE_GUIDELINES.md) for TDD practices and testing guidelines.

## ⚙️ Configuration

### Kotlin Version

⚠️ **This project uses Kotlin 2.3.0. Do not upgrade without thorough testing.**

Version is configured in `gradle/libs.versions.toml`:
```toml
kotlin = "2.3.0"
```

### Firebase Versions

- Firebase BOM: 33.7.0
- Firebase Crashlytics Plugin: 3.0.2
- Google Services Plugin: 4.4.2

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

Please follow the architecture guidelines and ensure all tests pass before submitting.

## 📄 License

[Your License Here]

## 👥 Authors

[Your Name/Team]

## 🙏 Acknowledgments

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Firebase](https://firebase.google.com/)

---

**Note:** Remember to add your Firebase configuration files after cloning!
