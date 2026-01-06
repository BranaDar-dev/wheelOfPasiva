# Fix CocoaPods embedAndSign Conflict

## Problem
`Your project currently has CocoaPods dependencies that conflict with the embedAndSign workflow.`

This error occurs when there's a conflict between CocoaPods framework embedding and Kotlin Multiplatform framework configuration.

## Solutions

### Solution 1: Clean Build and Reinstall Pods (Try This First)

1. **Clean Gradle build:**
   ```bash
   ./gradlew clean
   ```

2. **Remove CocoaPods artifacts:**
   ```bash
   cd iosApp
   rm -rf Pods
   rm -rf Podfile.lock
   rm -rf .build
   cd ..
   ```

3. **Reinstall pods:**
   ```bash
   ./gradlew :composeApp:podInstall
   ```

4. **Clean Xcode build:**
   - Open `iosApp/iosApp.xcworkspace` (not .xcodeproj)
   - In Xcode: **Product → Clean Build Folder** (⇧⌘K)
   - Delete DerivedData: **Xcode → Preferences → Locations → DerivedData** → Click arrow and delete the folder

5. **Rebuild:**
   ```bash
   cd iosApp
   xcodebuild -workspace iosApp.xcworkspace -scheme iosApp -configuration Debug
   ```

### Solution 2: Fix Xcode Build Phases

1. Open `iosApp/iosApp.xcworkspace` in Xcode
2. Select the `iosApp` target
3. Go to **Build Phases** tab
4. Look for "[CP] Embed Pods Frameworks" phase
5. Make sure `ComposeApp.framework` is NOT listed in the "Embed Frameworks" phase
6. The framework should only be in the "Link Binary With Libraries" phase

### Solution 3: Update Podfile

Edit `iosApp/Podfile` to ensure proper configuration:

```ruby
platform :ios, '15.0'

target 'iosApp' do
  use_frameworks!
  
  # Pods will be added by Kotlin CocoaPods plugin
end

post_install do |installer|
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '15.0'
      config.build_settings['BUILD_LIBRARY_FOR_DISTRIBUTION'] = 'YES'
    end
  end
end
```

### Solution 4: Check Framework Settings in Xcode

1. Select the `iosApp` project in Xcode
2. Select the `iosApp` target
3. Go to **General** tab
4. Under **Frameworks, Libraries, and Embedded Content**:
   - Make sure `ComposeApp.framework` is set to "Do Not Embed"
   - Firebase frameworks should be "Embed & Sign"

### Solution 5: Modify build.gradle.kts (Already Done)

The framework configuration has been updated to avoid duplication. The change removed the duplicate `isStatic = true` declaration in the CocoaPods block.

### Solution 6: Alternative - Use SPM Instead of CocoaPods (Advanced)

If CocoaPods continues to cause issues, consider using Swift Package Manager for Firebase instead. However, this requires more setup and may not be fully supported by all Firebase features.

## Verification Steps

After applying the fix:

1. **Sync Gradle:**
   ```bash
   ./gradlew --refresh-dependencies
   ```

2. **Build from terminal:**
   ```bash
   ./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
   ```

3. **Build in Xcode:**
   - Open `iosApp/iosApp.xcworkspace`
   - Select iPhone simulator
   - Press ⌘R to build and run

## Common Issues

### Issue: "Framework not found ComposeApp"
**Solution:** 
```bash
./gradlew :composeApp:podInstall
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

### Issue: "Multiple commands produce framework"
**Solution:** Check Build Phases and remove duplicate "Embed Pods Frameworks" steps

### Issue: Pod install fails
**Solution:** 
```bash
cd iosApp
pod deintegrate
pod install
cd ..
```

## Best Practices

1. Always open `.xcworkspace`, never `.xcodeproj` when using CocoaPods
2. Clean build folders when changing CocoaPods configuration
3. Run `./gradlew :composeApp:podInstall` after any changes to build.gradle.kts
4. Keep CocoaPods updated: `sudo gem install cocoapods`

## Still Having Issues?

If the problem persists:

1. Check the exact error message in Xcode
2. Verify all Firebase pods are properly installed: `cd iosApp && pod list`
3. Make sure you have the latest Xcode Command Line Tools: `xcode-select --install`
4. Check for conflicting framework versions in Podfile.lock
