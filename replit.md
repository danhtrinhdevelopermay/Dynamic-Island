# Dynamic Island Android

## Overview

This is an Android Kotlin project that creates a Dynamic Island-like notification overlay similar to iOS. When notifications arrive, they are displayed as a sleek, animated pill-shaped overlay at the top of the screen.

**Project Type**: Android Mobile Application  
**Language**: Kotlin  
**Build System**: Gradle with Kotlin DSL  
**Min SDK**: Android 8.0 (API 26)  
**Target SDK**: Android 14 (API 34)

## Project Architecture

### Core Components

1. **DynamicIslandService** (`service/DynamicIslandService.kt`)
   - Extends `NotificationListenerService`
   - Listens for all system notifications
   - Extracts notification data (title, content, actions, app icon)
   - Forwards notification data to OverlayService

2. **OverlayService** (`service/OverlayService.kt`)
   - Manages the overlay window (Dynamic Island UI)
   - Handles expand/collapse animations
   - Auto-hides after configurable duration
   - Runs as foreground service

3. **BootReceiver** (`service/BootReceiver.kt`)
   - Starts the service automatically after device boot

4. **MainActivity** (`ui/MainActivity.kt`)
   - Permission management UI
   - Service toggle controls
   - Test notification functionality

### Directory Structure

```
app/src/main/
├── java/com/dynamicisland/android/
│   ├── service/           # Background services
│   ├── ui/                # Activities and UI components
│   └── util/              # Helper classes and data models
├── res/
│   ├── layout/            # XML layouts
│   ├── drawable/          # Shapes and icons
│   ├── anim/              # Animation resources
│   └── values/            # Strings, colors, themes
└── AndroidManifest.xml
```

## Building the APK

### GitHub Actions (Recommended)

This project includes a GitHub Actions workflow (`.github/workflows/android-build.yml`) that automatically builds the APK:

1. Push code to GitHub repository
2. Go to Actions tab
3. Download APK from Artifacts or Releases

### Local Build

Requires Android SDK and JDK 17:

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Key Features

- iOS-like Dynamic Island appearance
- Smooth expand/collapse animations
- Notification action buttons support
- App icon and name display
- Auto-hide with configurable duration
- Boot persistence

## Permissions Required

- `SYSTEM_ALERT_WINDOW` - Display overlay on other apps
- `BIND_NOTIFICATION_LISTENER_SERVICE` - Read notifications
- `POST_NOTIFICATIONS` - Foreground service notification
- `RECEIVE_BOOT_COMPLETED` - Auto-start on boot

## Recent Changes

- Initial project setup (2024)
- Created full Android project structure
- Implemented NotificationListenerService
- Created Dynamic Island overlay UI with animations
- Added GitHub Actions workflow for APK builds
- Vietnamese language support for UI strings
- Added backdrop blur effect for Dynamic Island overlay (2025-11-28)
  - Uses FLAG_BLUR_BEHIND and blurBehindRadius for Android 12+
  - Semi-transparent background colors for blur effect
  - BlurHelper utility class for managing blur effects
  - Fallback to solid background on older Android versions

## Notes

- This is a mobile Android project - it cannot be "run" directly in Replit
- Build APK using GitHub Actions or local Android SDK
- Requires physical Android device or emulator for testing
