# eSIBiL Call — Flutter

Cross-platform prison video call client for Punjab Prison Foundation (eSIBiL). Mirrors the native Android app flow: welcome → onboarding → prisoner registration (SIP login) → home with call history and profile.

## Prerequisites

- [Flutter SDK](https://docs.flutter.dev/get-started/install) (3.19+ recommended)
- Android Studio / Xcode for device builds
- Physical device or emulator with camera & microphone

## Setup

```bash
cd esibil_call_flutter
flutter pub get
```

## Run

**Android**

```bash
flutter run -d android
```

Native SIP uses Linphone SDK 5.3.30 via `MethodChannel` (`com.esibil.call/sip`).

**iOS**

```bash
flutter run -d ios
```

SIP MethodChannel is stubbed on iOS (returns not implemented) but the project compiles.

## Project structure

| Path | Purpose |
|------|---------|
| `lib/config/app_config.dart` | SIP server, jails API, fixed password |
| `lib/services/` | Prefs, Jails API, SIP bridge |
| `lib/screens/` | Welcome, onboarding, registration, home, calls |
| `android/.../LinphoneBridge.kt` | Linphone SIP engine (Android) |
| `ios/Runner/AppDelegate.swift` | SIP stub (iOS) |

## Configuration

Edit `lib/config/app_config.dart` for SIP defaults and jails API URL/token. Jail IP is selected at runtime from the sites API when the user picks a prison.

## Permissions

Android manifest includes network, audio, camera, notifications, and foreground service permissions matching the native Android app.

## Version

1.0.0 — Punjab Prison Foundation Video Call App
