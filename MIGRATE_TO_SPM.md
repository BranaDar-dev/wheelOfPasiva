# Migrate from CocoaPods to Swift Package Manager (SPM)

This guide will help you migrate Firebase from CocoaPods to Swift Package Manager, which integrates better with Kotlin Multiplatform projects.

## Step 1: Remove CocoaPods

### 1.1 Deintegrate CocoaPods

```bash
cd iosApp
pod deintegrate
cd ..
```

### 1.2 Remove CocoaPods Files

```bash
rm -rf iosApp/Pods
rm -f iosApp/Podfile
rm -f iosApp/Podfile.lock
rm -rf iosApp/*.xcworkspace
```

### 1.3 Clean Derived Data

```bash
rm -rf ~/Library/Developer/Xcode/DerivedData/*
```

## Step 2: Add Firebase via Swift Package Manager

### 2.1 Open Xcode Project (NOT Workspace)

Now that we've removed CocoaPods, open the `.xcodeproj` file:

```bash
open iosApp/iosApp.xcodeproj
```

### 2.2 Add Firebase Package

1. In Xcode, select the **iosApp** project (blue icon at the top)
2. Select the **iosApp** project in the PROJECT section (not the target)
3. Go to the **Package Dependencies** tab
4. Click the **+** button

5. In the search field, enter:
   ```
   https://github.com/firebase/firebase-ios-sdk
   ```

6. Click **Add Package**

7. In the package version selection:
   - Choose **"Up to Next Major Version"**
   - Enter: `11.0.0` (or latest stable version)
   - Click **Add Package**

8. Select the following Firebase products:
   - ✅ **FirebaseAnalytics**
   - ✅ **FirebaseCrashlytics**
   - ✅ **FirebaseFirestore**

9. Make sure the target is set to **iosApp**
10. Click **Add Package**

## Step 3: Link the Kotlin Framework

### 3.1 Add Framework Search Path

1. Select the **iosApp** target (under TARGETS)
2. Go to **Build Settings** tab
3. Search for "Framework Search Paths"
4. Add (double-click to add):
   ```
   $(SRCROOT)/../composeApp/build/bin/iosSimulatorArm64/debugFramework
   $(SRCROOT)/../composeApp/build/bin/iosArm64/debugFramework
   ```

### 3.2 Link the Framework

1. Select the **iosApp** target
2. Go to **General** tab
3. Under **Frameworks, Libraries, and Embedded Content**, click **+**
4. Click **Add Other** → **Add Files...**
5. Navigate to: `composeApp/build/bin/iosSimulatorArm64/debugFramework/ComposeApp.framework`
6. Click **Open**
7. Set it to **"Do Not Embed"** (IMPORTANT!)

## Step 4: Verify FirebaseWrapper.swift

Make sure `FirebaseWrapper.swift` is in your Xcode project:

1. In Project Navigator, look for `FirebaseWrapper.swift` in the `iosApp` folder
2. If it's not there:
   - Right-click on `iosApp` folder
   - Select **Add Files to "iosApp"...**
   - Navigate to `iosApp/iosApp/FirebaseWrapper.swift`
   - Make sure "Copy items if needed" is checked
   - Make sure "iosApp" target is selected
   - Click **Add**

## Step 5: Verify GoogleService-Info.plist

1. Make sure `GoogleService-Info.plist` is in your Xcode project
2. It should be visible in the Project Navigator under `iosApp`
3. If not, add it:
   - Right-click on `iosApp` folder
   - Select **Add Files to "iosApp"...**
   - Select `GoogleService-Info.plist`
   - Make sure "Copy items if needed" is checked
   - Make sure "iosApp" target is selected
   - Click **Add**

## Step 6: Add Crashlytics Build Phase

Firebase Crashlytics requires a run script:

1. Select the **iosApp** target
2. Go to **Build Phases** tab
3. Click **+** → **New Run Script Phase**
4. Name it: "Firebase Crashlytics"
5. Move it to be the **LAST** build phase
6. Add this script:
   ```bash
   "${BUILD_DIR%Build/*}SourcePackages/checkouts/firebase-ios-sdk/Crashlytics/run"
   ```
7. Add Input Files (click + under Input Files):
   ```
   ${DWARF_DSYM_FOLDER_PATH}/${DWARF_DSYM_FILE_NAME}/Contents/Resources/DWARF/${TARGET_NAME}
   $(SRCROOT)/$(BUILT_PRODUCTS_DIR)/$(INFOPLIST_PATH)
   ```

## Step 7: Build Framework and Test

### 7.1 Build Kotlin Framework

```bash
# Clean first
./gradlew clean

# Build for simulator
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Verify it exists
ls -la composeApp/build/bin/iosSimulatorArm64/debugFramework/
```

### 7.2 Build in Xcode

1. Make sure you're opening `.xcodeproj` (NOT .xcworkspace - that doesn't exist anymore)
   ```bash
   open iosApp/iosApp.xcodeproj
   ```

2. Select iPhone simulator (e.g., iPhone 15)

3. Clean Build Folder: **Product → Clean Build Folder** (⇧⌘K)

4. Build and Run: **Product → Run** (⌘R)

## Step 8: Update Build Configuration

### 8.1 For Release Builds

When building for release or real devices:

```bash
# For device
./gradlew :composeApp:linkReleaseFrameworkIosArm64

# Update Framework Search Path in Xcode for Release configuration
```

### 8.2 Add Build Script (Optional)

If you want to auto-build the Kotlin framework, add this run script:

1. **Build Phases** → **+** → **New Run Script Phase**
2. Name: "Build Kotlin Framework"
3. **Move it BEFORE "Compile Sources"**
4. Script:
   ```bash
   cd "$SRCROOT/.."
   
   if [ "$PLATFORM_NAME" = "iphonesimulator" ]; then
       ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
   else
       ./gradlew :composeApp:linkDebugFrameworkIosArm64
   fi
   ```

## Comparison: CocoaPods vs SPM

| Feature | CocoaPods | SPM |
|---------|-----------|-----|
| Integration | Requires workspace | Built into Xcode |
| KMP Compatibility | Can conflict | No conflicts |
| Setup Complexity | High | Low |
| Build Speed | Slower | Faster |
| Maintenance | External tool | Native |

## Troubleshooting

### Error: "Framework not found ComposeApp"

**Solution:**
```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### Error: "No such module 'FirebaseAnalytics'"

**Solution:**
1. Go to **File → Packages → Reset Package Caches**
2. Go to **File → Packages → Resolve Package Versions**
3. Clean and rebuild

### Error: Framework linking issues

**Solution:**
Make sure in **General → Frameworks, Libraries, and Embedded Content**:
- `ComposeApp.framework` is set to **"Do Not Embed"**
- Firebase frameworks are automatically managed by SPM

### Build works in simulator but not on device

**Solution:**
Build the correct framework:
```bash
# For device
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

## Benefits of SPM for This Project

✅ **No CocoaPods conflicts** with Kotlin Multiplatform
✅ **Simpler setup** - no Podfile, no workspace complexity
✅ **Better Xcode integration** - native package management
✅ **Faster builds** - no pod install step needed
✅ **Easier CI/CD** - fewer steps in build pipeline

## Verification Checklist

- [ ] CocoaPods files removed (Pods, Podfile, Podfile.lock)
- [ ] Opening `.xcodeproj` (not .xcworkspace)
- [ ] Firebase packages added via SPM
- [ ] Framework search paths configured
- [ ] ComposeApp.framework linked (Do Not Embed)
- [ ] FirebaseWrapper.swift in project
- [ ] GoogleService-Info.plist in project
- [ ] Crashlytics run script added
- [ ] Kotlin framework builds successfully
- [ ] iOS app builds and runs
- [ ] Firebase features work (Analytics, Crashlytics)

## Next Steps After Migration

1. Test all Firebase features:
   - Analytics events logging
   - Crashlytics error reporting
   - Firestore data operations

2. Update CI/CD pipelines (if any) to remove pod install steps

3. Document the new build process for your team

4. Commit changes to version control

## Files to Commit

```bash
git add iosApp/iosApp.xcodeproj/
git add iosApp/iosApp/
git rm -r iosApp/Pods
git rm iosApp/Podfile
git rm iosApp/Podfile.lock
git rm -r iosApp/*.xcworkspace
git commit -m "Migrate from CocoaPods to Swift Package Manager"
```

## Files to .gitignore

Make sure these are in your `.gitignore`:
```
iosApp/Pods/
iosApp/Podfile.lock
iosApp/*.xcworkspace/
iosApp/xcuserdata/
```

And remove these (no longer needed):
```
# These are now handled by SPM, which is built into Xcode
```
