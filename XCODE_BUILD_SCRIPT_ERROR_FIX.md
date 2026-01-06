# Fix: Command PhaseScriptExecution Failed with Nonzero Exit Code

## Common Causes in This Project

1. Kotlin Framework build script failing
2. CocoaPods script phases failing
3. Firebase Crashlytics script failing
4. Path issues

## Solution Steps

### Step 1: Check Which Script is Failing

1. In Xcode, click on the error in the Issue Navigator
2. Look for the script phase name (e.g., "Build Kotlin Framework", "[CP] Embed Pods Frameworks", etc.)
3. Click "Show" next to the error to see the full output

### Step 2: Remove the Kotlin Build Script (If You Added It)

We added a run script earlier that might be causing issues. Let's remove it:

1. Open Xcode: `open iosApp/iosApp.xcworkspace`
2. Select the `iosApp` target
3. Go to **Build Phases** tab
4. Look for "Build Kotlin Framework" run script phase
5. **Delete it** (click the X or right-click → Delete)

### Step 3: Build Framework Manually

Instead of using a build script, build the framework manually before building in Xcode:

```bash
# For simulator (most common for development)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# For device
./gradlew :composeApp:linkDebugFrameworkIosArm64
```

### Step 4: Check CocoaPods Installation

If the error is related to CocoaPods:

```bash
cd iosApp
pod deintegrate
pod install
cd ..
```

### Step 5: Fix Firebase Crashlytics Script (If Needed)

If the error mentions Firebase Crashlytics:

1. In Xcode, go to **Build Phases**
2. Find "[CP] Run Script" or similar Firebase-related script
3. Temporarily disable it by unchecking the checkbox
4. Try building again

### Step 6: Check File Paths

Make sure the framework exists:

```bash
ls -la composeApp/build/bin/iosSimulatorArm64/debugFramework/
```

You should see `ComposeApp.framework`

### Step 7: Clean Everything

```bash
# Clean Gradle
./gradlew clean

# Clean Xcode
rm -rf ~/Library/Developer/Xcode/DerivedData/*

# In Xcode: Product → Clean Build Folder (⇧⌘K)
```

## Recommended Build Setup (Without Run Script)

### Option A: Manual Framework Build (Simplest)

1. **Remove all custom run scripts** from Build Phases
2. **Build framework manually** before Xcode build:
   ```bash
   ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
   ```
3. **Build in Xcode** (⌘R)

### Option B: Proper Run Script Configuration

If you need a run script, use this correct version:

1. In Xcode, **Build Phases** → **+** → **New Run Script Phase**
2. Name it: "Build Kotlin Framework"
3. **Move it BEFORE "Compile Sources"**
4. Add this script:

```bash
set -e

cd "$SRCROOT/.."

# Determine the configuration
if [ "$CONFIGURATION" = "Debug" ]; then
    TASK_SUFFIX="Debug"
else
    TASK_SUFFIX="Release"
fi

# Determine the architecture
if [ "$PLATFORM_NAME" = "iphonesimulator" ]; then
    ARCH="IosSimulatorArm64"
else
    ARCH="IosArm64"
fi

# Build the framework
./gradlew :composeApp:link${TASK_SUFFIX}Framework${ARCH}
```

5. Add input files:
   - `$(SRCROOT)/../composeApp/src/commonMain/**/*`
   - `$(SRCROOT)/../composeApp/src/iosMain/**/*`

6. Add output files:
   - `$(SRCROOT)/../composeApp/build/bin/iosSimulatorArm64/debugFramework/ComposeApp.framework`

## Check Specific Error Messages

### Error: "gradlew: Permission denied"

```bash
chmod +x gradlew
```

### Error: "No such file or directory"

Check that paths are correct:
```bash
cd iosApp
pwd  # Should show .../wheelOfPasiva/iosApp
cd ..
pwd  # Should show .../wheelOfPasiva
```

### Error: "Task 'embedAndSignAppleFrameworkForXcode' not found"

This task doesn't exist. Use:
```bash
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### Error: CocoaPods related

```bash
cd iosApp
pod repo update
pod install
cd ..
```

## My Recommendation

For this project, I recommend **Option A** (manual build):

1. **Remove any custom Kotlin build scripts** from Xcode Build Phases
2. **Build framework manually** when needed:
   ```bash
   ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
   ```
3. **Then build in Xcode**

This approach is simpler and less error-prone during development.

## Verify Setup

After fixing, verify the setup:

```bash
# 1. Clean everything
./gradlew clean
rm -rf iosApp/Pods iosApp/Podfile.lock

# 2. Install pods
cd iosApp
pod install
cd ..

# 3. Build framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# 4. Check framework exists
ls -la composeApp/build/bin/iosSimulatorArm64/debugFramework/

# 5. Open Xcode
open iosApp/iosApp.xcworkspace

# 6. Clean Build Folder in Xcode (⇧⌘K)

# 7. Build and Run (⌘R)
```

## Still Getting Error?

If you still see the error:

1. **Copy the exact error message** from Xcode's Report Navigator
2. Look for the specific script name in the error
3. Check what the script is trying to do
4. Share the full error output for more specific help

The error format is usually:
```
PhaseScriptExecution [Script Name] /path/to/script.sh
    Command /bin/sh failed with exit code X
```

The exit code and script output will tell us exactly what's wrong.
