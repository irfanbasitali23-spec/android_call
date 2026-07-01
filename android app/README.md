# eSIBiL Call — Android SIP audio & video calling app

A Kotlin Android app that registers a user by phone number, receives a
server-assigned SIP id, and places **audio and video calls** over a SIP /
eSIBiL calling server using the **Linphone SDK**.

## Flow

1. **Register** (`RegisterActivity`) — the user enters their phone number. The
   app POSTs it to your provisioning backend, which validates it and returns
   the SIP id + password the server assigned. Those credentials are stored and
   used to register against the SIP server.
2. **Dialer** (`DialerActivity`) — enter a SIP id / number and start an
   **Audio** or **Video** call. Incoming calls open the call screen automatically.
3. **In call** (`CallActivity`) — full-screen remote video with a local preview,
   plus mute, speaker, camera-switch, video on/off and hang-up controls.

## Configure your server

All server settings live in one file: **`app/src/main/java/com/esibil/call/Config.kt`**

| Constant           | Meaning                                                        |
|--------------------|----------------------------------------------------------------|
| `SIP_DOMAIN`       | SIP realm used in addresses `sip:<id>@<domain>`                |
| `SIP_PROXY`        | SIP registrar/proxy, e.g. `sip:sip.example.com:5060` or an IP   |
| `SIP_TRANSPORT`    | `Udp`, `Tcp`, or `Tls` (use `Tls` in production)               |
| `PROVISIONING_URL` | Your HTTP endpoint that maps a phone number → SIP credentials  |

### Provisioning API contract

`POST {PROVISIONING_URL}` with `{"phone": "+923001234567"}` should return:

```json
{ "sipId": "923001234567", "password": "s3cr3t", "domain": "sip.example.com" }
```

> **DEV mode:** while `PROVISIONING_URL` still points at the placeholder host
> (`CHANGE_ME.example.com`), the app skips the network call and derives a temporary
> SIP id from the phone number, so you can test calling before the backend is
> live. Point it at a real URL to enable true phone-number validation.

## Build & run

This project has no committed Gradle wrapper JAR. Either:

- **Android Studio** (recommended): *File → Open* this folder. Studio downloads
  the wrapper and Linphone SDK automatically, then *Run*.
- **Command line:** run `gradle wrapper` once (needs Gradle 8.x installed) to
  generate `gradlew`, then `./gradlew assembleDebug`.

The Linphone SDK is pulled from `https://download.linphone.org/maven_repository`
(configured in `settings.gradle.kts`).

## Requirements

- Android Studio Koala+ / AGP 8.5, JDK 17
- minSdk 23, targetSdk 34
- A reachable SIP/Asterisk/eSIBiL server and valid credentials

## Project layout

```
app/src/main/java/com/esibil/call/
├── Config.kt                 ← EDIT: server & backend settings
├── CallApplication.kt        ← boots the Linphone Core
├── core/LinphoneManager.kt   ← SIP register + audio/video call engine
├── data/Prefs.kt             ← stored SIP credentials
├── network/RegistrationApi.kt← phone → SIP-id provisioning
└── ui/
    ├── RegisterActivity.kt   ← phone-number sign-up
    ├── DialerActivity.kt     ← dial audio/video
    └── CallActivity.kt       ← in-call screen
```

## Notes

- `usesCleartextTraffic` is enabled for UDP/plain SIP during development. Use TLS
  and remove cleartext for production.
- The app requests `RECORD_AUDIO`, `CAMERA`, and (Android 13+) `POST_NOTIFICATIONS`
  at runtime on first launch.
