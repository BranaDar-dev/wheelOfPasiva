# Firebase Setup Guide

This guide will help you set up Firebase (Analytics, Crashlytics, and Firestore) for both Android and iOS platforms.

## Prerequisites

1. A Firebase project created at [Firebase Console](https://console.firebase.google.com/)
2. Android Studio or IntelliJ IDEA
3. Xcode (for iOS setup)
4. CocoaPods installed (`sudo gem install cocoapods`)
5. **Kotlin 2.3.0** (Required - Do not upgrade)

## Important Notes

### Kotlin Version
**This project uses Kotlin 2.3.0. Do not upgrade to newer versions without testing thoroughly.**

The project is configured to use Kotlin 2.3.0 in `gradle/libs.versions.toml`:
```toml
kotlin = "2.3.0"
```

If you encounter issues with Kotlin versions:
1. Check `gradle/libs.versions.toml` and ensure `kotlin = "2.3.0"`
2. Sync Gradle: **File → Sync Project with Gradle Files**
3. Clean and rebuild: `./gradlew clean build`

### Gradle Sync

**After making any changes to build files or adding configuration files, always sync Gradle:**

- **In Android Studio/IntelliJ IDEA:** Click "Sync Now" or go to **File → Sync Project with Gradle Files**
- **From Terminal:** Run `./gradlew --refresh-dependencies`

This ensures that all dependencies and plugins are properly loaded and configured.

## Step 1: Firebase Project Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Enable the following services:
   - **Analytics** (enabled by default)
   - **Crashlytics**
   - **Firestore Database**

## Step 2: Android Setup

### 2.1 Download google-services.json

1. In Firebase Console, click on the Android icon (or go to Project Settings)
2. Register your Android app:
   - Package name: `com.bramish.wheelofpasiva`
   - App nickname: `Wheel of Pasiva Android`
   - Debug signing certificate SHA-1 (optional for now)
3. Download `google-services.json`
4. Place it in: `composeApp/src/androidMain/`

**Important:** The file structure should be:
```
composeApp/
  └── src/
      └── androidMain/
          └── google-services.json
```

### 2.2 Sync Gradle

After adding `google-services.json`, you need to sync Gradle to apply the changes:

**In Android Studio/IntelliJ IDEA:**
- Click the "Sync Now" banner that appears, or
- Go to **File → Sync Project with Gradle Files**, or
- Click the Gradle elephant icon in the toolbar

**From Terminal:**
```bash
./gradlew --refresh-dependencies
```

### 2.3 Verify Android Configuration

The following dependencies are already configured in `composeApp/build.gradle.kts`:
- Firebase BOM (Bill of Materials) for version management
- Firebase Analytics
- Firebase Crashlytics
- Firebase Firestore

Firebase is automatically initialized on Android when the app starts (via `google-services.json`).

## Step 3: iOS Setup

### 3.1 Download GoogleService-Info.plist

1. In Firebase Console, click on the iOS icon (or go to Project Settings)
2. Register your iOS app:
   - Bundle ID: Check your Xcode project (usually `com.bramish.wheelofpasiva` or similar)
   - App nickname: `Wheel of Pasiva iOS`
3. Download `GoogleService-Info.plist`
4. Add it to your Xcode project:
   - Open `iosApp/iosApp.xcodeproj` in Xcode
   - Right-click on the `iosApp` folder in the project navigator
   - Select "Add Files to iosApp..."
   - Select `GoogleService-Info.plist`
   - Make sure "Copy items if needed" is checked
   - Make sure "iosApp" target is selected
   - Click "Add"

### 3.2 Install CocoaPods Dependencies

The Firebase pods are configured in `composeApp/build.gradle.kts` via the CocoaPods plugin. To install them:

1. **First, sync Gradle** to ensure the CocoaPods configuration is loaded:
   
   **In Android Studio/IntelliJ IDEA:**
   - Click the "Sync Now" banner that appears, or
   - Go to **File → Sync Project with Gradle Files**

   **From Terminal:**
   ```bash
   ./gradlew --refresh-dependencies
   ```

2. Open Terminal
3. Navigate to the project root:
   ```bash
   cd /Users/brana/Projects/wheelOfPasiva
   ```
4. Run the Gradle task to sync CocoaPods:
   ```bash
   ./gradlew :composeApp:podInstall
   ```
   
   Or if you prefer to use CocoaPods directly:
   ```bash
   cd iosApp
   pod install
   cd ..
   ```

5. **Sync Gradle again** after pod installation:
   ```bash
   ./gradlew --refresh-dependencies
   ```

### 3.3 Open Xcode Workspace (Not Project)

After installing pods, always open the `.xcworkspace` file, not the `.xcodeproj`:

```bash
open iosApp/iosApp.xcworkspace
```

**Note:** If the workspace doesn't exist yet, it will be created after running `pod install`.

## Step 4: Verify Installation

### Android

1. **Sync Gradle** if you haven't already:
   - In IDE: **File → Sync Project with Gradle Files**
   - Or from terminal: `./gradlew --refresh-dependencies`

2. Build the Android app:
   ```bash
   ./gradlew :composeApp:assembleDebug
   ```

3. Run the app and check Logcat for Firebase initialization messages.

### iOS

1. Open `iosApp/iosApp.xcworkspace` in Xcode
2. Select a simulator or device
3. Build and run (⌘R)

## Step 5: Using Firebase in Your Code

### Common Interface

The project includes a multiplatform `FirebaseManager` that works on both platforms:

```kotlin
import com.bramish.wheelofpasiva.firebase.FirebaseManager

// Initialize (already done in MainActivity/MainViewController)
val firebaseManager = FirebaseManager()
firebaseManager.initialize()

// Log an analytics event
firebaseManager.logEvent("button_clicked", mapOf(
    "button_name" to "play_button",
    "screen" to "home"
))

// Log a non-fatal exception
try {
    // your code
} catch (e: Exception) {
    firebaseManager.logException(e)
}

// Set user ID
firebaseManager.setUserId("user123")

// Set custom key for crashlytics
firebaseManager.setCustomKey("app_version", "1.0.0")
```

### Using Firestore

For Firestore, you'll need to use platform-specific implementations or a multiplatform library. Here's an example structure:

#### Common Interface (expect/actual pattern)

```kotlin
// commonMain/kotlin/com/bramish/wheelofpasiva/firebase/FirestoreManager.kt
expect class FirestoreManager {
    suspend fun saveData(collection: String, documentId: String, data: Map<String, Any>)
    suspend fun getData(collection: String, documentId: String): Map<String, Any>?
}
```

#### Android Implementation

```kotlin
// androidMain/kotlin/com/bramish/wheelofpasiva/firebase/FirestoreManager.android.kt
import com.google.firebase.firestore.FirebaseFirestore

actual class FirestoreManager {
    private val db = FirebaseFirestore.getInstance()
    
    actual suspend fun saveData(collection: String, documentId: String, data: Map<String, Any>) {
        db.collection(collection)
            .document(documentId)
            .set(data)
            .await()
    }
    
    actual suspend fun getData(collection: String, documentId: String): Map<String, Any>? {
        return db.collection(collection)
            .document(documentId)
            .get()
            .await()
            .data
    }
}
```

#### iOS Implementation

```kotlin
// iosMain/kotlin/com/bramish/wheelofpasiva/firebase/FirestoreManager.ios.kt
import cocoapods.FirebaseFirestore.FIRFirestore

actual class FirestoreManager {
    private val db = FIRFirestore.firestore()
    
    actual suspend fun saveData(collection: String, documentId: String, data: Map<String, Any>) {
        // Convert Map to NSDictionary and save
        val dict = NSMutableDictionary()
        data.forEach { (key, value) ->
            dict[key] = value.toString()
        }
        db.collectionWithPath(collection)
            .documentWithPath(documentId)
            .setData(dict as Map<Any?, *>)
    }
    
    actual suspend fun getData(collection: String, documentId: String): Map<String, Any>? {
        // Implementation using FIRFirestore
        // Note: This is simplified - you'll need to handle async properly
        return null
    }
}
```

## Step 6: Testing Firebase Integration

### Test Analytics

1. Run the app on a device or emulator
2. Perform some actions (button clicks, screen views, etc.)
3. Go to Firebase Console → Analytics → Events
4. Wait a few minutes for events to appear (can take up to 24 hours for some events)

### Test Crashlytics

1. Add a test crash button in your app:
   ```kotlin
   Button(onClick = { 
       throw RuntimeException("Test crash")
   }) {
       Text("Test Crash")
   }
   ```
2. Click the button to crash the app
3. Restart the app (Crashlytics sends reports on next launch)
4. Go to Firebase Console → Crashlytics
5. You should see the crash report within a few minutes

### Test Firestore

1. Create a test document in Firestore:
   ```kotlin
   val firestoreManager = FirestoreManager()
   firestoreManager.saveData(
       collection = "test",
       documentId = "test123",
       data = mapOf("message" to "Hello Firebase!")
   )
   ```
2. Check Firebase Console → Firestore Database
3. You should see the document created

## Troubleshooting

### Android Issues

**Problem:** `google-services.json` not found
- **Solution:** Make sure the file is in `composeApp/src/androidMain/google-services.json`

**Problem:** Firebase not initializing
- **Solution:** Check that `google-services` plugin is applied in `build.gradle.kts`

**Problem:** Build errors related to Firebase
- **Solution:** 
  1. Sync Gradle files:
     - In IDE: **File → Sync Project with Gradle Files**
     - Or from terminal: `./gradlew --refresh-dependencies`
  2. Clean and rebuild: `./gradlew clean build`

**Problem:** Dependencies not found after adding Firebase
- **Solution:** 
  1. Sync Gradle: **File → Sync Project with Gradle Files** or `./gradlew --refresh-dependencies`
  2. Invalidate caches: **File → Invalidate Caches... → Invalidate and Restart**
  3. Rebuild the project

### iOS Issues

**Problem:** CocoaPods not found
- **Solution:** Install CocoaPods: `sudo gem install cocoapods`

**Problem:** `GoogleService-Info.plist` not found
- **Solution:** Make sure the file is added to the Xcode project and included in the target

**Problem:** Build errors in Xcode
- **Solution:** 
  1. Clean build folder: Product → Clean Build Folder (⇧⌘K)
  2. Delete DerivedData
  3. Run `pod install` again
  4. Reopen the workspace

**Problem:** Firebase pods not linking
- **Solution:** Make sure you're opening `.xcworkspace`, not `.xcodeproj`

### Common Issues

**Problem:** Events not showing in Analytics
- **Solution:** 
  - Analytics can take 24 hours to show some events
  - Make sure you're testing on a real device (not just emulator)
  - Check that Firebase is properly initialized

**Problem:** Crashlytics not reporting crashes
- **Solution:** 
  - Crashes are sent on the next app launch
  - Make sure you're using a release build or have debug symbols enabled
  - Check that Crashlytics is properly initialized

## Additional Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Android Setup](https://firebase.google.com/docs/android/setup)
- [Firebase iOS Setup](https://firebase.google.com/docs/ios/setup)
- [Kotlin Multiplatform with CocoaPods](https://kotlinlang.org/docs/native-cocoapods.html)

## Version Requirements

### Kotlin Version
- **Required:** Kotlin 2.3.0
- **Location:** `gradle/libs.versions.toml`
- **Do not upgrade** without thorough testing

### Firebase Versions (Current)
- Firebase BOM: 33.7.0
- Firebase Crashlytics Plugin: 3.0.2
- Google Services Plugin: 4.4.2

## Next Steps

1. Set up Firestore security rules
2. Configure Crashlytics symbol uploads for better crash reports
3. Set up custom events and user properties in Analytics
4. Implement offline persistence for Firestore
5. Add Firebase Remote Config for feature flags
