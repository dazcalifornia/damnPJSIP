# Selen VOIP Alarm Box

## Endpoints
Log Server - https://droid-log.selen.click/docs
Log API - https://droid-log.selen.click/device-list

Crash report Server - https://droid.selen.click/login
Update API - https://droid-update.selen.click/docs

## Features
- VoIP call management using PJSIP library
- Network change detection and handling
- Auto restart on crash or system reboot
- GPIO control for hardware interactions (i.e., button press and LED status)
- Foreground and background services for continuous operation
- Broadcast receivers for system events (boot completion, airplane mode, restart, boot completion)
- Integration with Selen Watchdog app for configuration (extension number, destination number)
- Shared preferences for storing configuration data ```com.synapes.android_voip_watchdog.provider/prefs```

## Requirements
- Android Studio Koala | 2024.1.1 Patch 1
- Kotlin
- Java
- Gradle
- Selen Watchdog app
- OpenVPN sets to `always on` mode. 

## Permissions
The application requires the following permissions:
- `INTERNET`
- `POST_NOTIFICATIONS`
- `RECORD_AUDIO`
- `READ_PHONE_STATE`
- `ACCESS_WIFI_STATE`
- `ACCESS_NETWORK_STATE`
- `MODIFY_AUDIO_SETTINGS`
- `WAKE_LOCK`
- `USE_SIP`
- `CALL_PHONE`
- `RECEIVE_BOOT_COMPLETED`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_MICROPHONE`
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `FOREGROUND_SERVICE_PHONE_CALL`
- `WRITE_EXTERNAL_STORAGE`
- `READ_EXTERNAL_STORAGE`
- `MANAGE_EXTERNAL_STORAGE`

## Configuration
The application reads configuration data from the Selen Watchdog app's shared preferences.:
- **Extension Number**: The self-extension number used for VoIP calls.
- **Destination Number**: The destination number for outgoing calls.

## GPIO Control
- **Button Press Detection**: The application can detect button presses using GPIO pins.
- **LED Status Control**: The application can control the status of LEDs using GPIO pins.

### GPIO Pins
- **Button GPIO 110**: `/sys/class/gpio/gpio1021/value`
- **Button GPIO 94**: `/sys/class/gpio/gpio1005/value`
- **LED GPIO**: `/sys/class/gpio/gpio1009/value`
- **LED2 GPIO**: `/sys/class/gpio/gpio1003/value`
- **LED3 GPIO**: `/sys/class/gpio/gpio999/value`

## Code Structure
- `MainActivity.kt`: Main activity handling UI and user interactions.
- `MyApp.kt`: Core application logic for managing VoIP calls.
- `SelenForegroundService.kt`: Foreground service for continuous operation.
- `SelenBackgroundService.kt`: Background service for additional tasks.
- `SelenBroadcastReceiver.kt`: Broadcast receiver for system events.
- `RestartBroadcastReceiver.kt`: Broadcast receiver to restart the app.
- `PreferencesManager.kt`: Utility for managing shared preferences.
- `Utils.kt`: Utility functions for hardware interactions and network checks.
- `MyApplication.kt`: Initializes the application and preferences.
- `AndroidManifest.xml`: Declares permissions, services, and receivers.
