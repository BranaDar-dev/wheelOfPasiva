# Remove CocoaPods Script References

## Error: `diff: /Podfile.lock: No such file or directory`

This error occurs when Xcode still has CocoaPods-related build phases even though we've removed CocoaPods.

## Solution: Remove CocoaPods Build Phases in Xcode

### Step 1: Open Xcode Project

```bash
open iosApp/iosApp.xcodeproj
```

### Step 2: Remove CocoaPods Build Phases

1. Select the **iosApp** target
2. Go to **Build Phases** tab
3. Look for these CocoaPods-related phases and **DELETE them**:
   - `[CP] Check Pods Manifest.lock`
   - `[CP] Embed Pods Frameworks`
   - `[CP] Copy Pods Resources`
   - Any phase with `[CP]` prefix

To delete:
- Click on the phase name
- Press **Delete** key, or
- Right-click → **Delete**

### Step 3: Clean Build Settings

1. Still in the **iosApp** target, go to **Build Settings** tab
2. Search for "pods" or "cocoapods"
3. Remove any CocoaPods-related settings:
   - Framework Search Paths with "Pods" references
   - Header Search Paths with "Pods" references
   - Other Linker Flags with CocoaPods references

### Step 4: Check Project File Directly (Advanced)

If the error persists, you can check the project file:

```bash
cd iosApp
grep -r "Podfile" iosApp.xcodeproj/
grep -r "\\[CP\\]" iosApp.xcodeproj/
```

If you see any results, we need to manually clean the project file.

### Step 5: Clean and Rebuild

```bash
# Clean derived data
rm -rf ~/Library/Developer/Xcode/DerivedData/*

# In Xcode: Product → Clean Build Folder (⇧⌘K)
```

## Common CocoaPods Build Phases to Remove

### [CP] Check Pods Manifest.lock
```bash
diff "${PODS_PODFILE_DIR_PATH}/Podfile.lock" ...
```
**Action:** DELETE this phase

### [CP] Embed Pods Frameworks
```bash
"${PODS_ROOT}/Target Support Files/Pods-iosApp/Pods-iosApp-frameworks.sh"
```
**Action:** DELETE this phase

### [CP] Copy Pods Resources
```bash
"${PODS_ROOT}/Target Support Files/Pods-iosApp/Pods-iosApp-resources.sh"
```
**Action:** DELETE this phase

## After Removing All CocoaPods References

You should have these build phases (and no [CP] phases):

1. **Target Dependencies**
2. **Compile Sources** (automatic)
3. **Link Binary With Libraries** (automatic)
4. **Copy Bundle Resources** (if any)
5. **Firebase Crashlytics** (if you added it) - KEEP this

Make sure there are NO phases with `[CP]` prefix.

## Verification

After removing CocoaPods phases:

1. Clean build: **Product → Clean Build Folder** (⇧⌘K)
2. Try building: ⌘B
3. You should NOT see the Podfile.lock error anymore

## If You Still See Errors

The project file might have lingering CocoaPods references. Let me know and I can help clean the project file directly.
