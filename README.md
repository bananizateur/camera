# Camera

A minimal Android camera app built with Jetpack Compose and CameraX.

## Features

- Live camera preview with pinch-to-zoom and tap-to-focus
- Photo capture and save to gallery (MediaStore)
- Front/back camera switching
- Flash mode cycling (off, auto, on)
- Exposure compensation
- Animated focus indicator

## Requirements

- **JDK 17**
- **Android Studio Ladybug** or later (AGP 9.3.1)
- **Android SDK 36** (compileSdk / targetSdk)
- **Min SDK 26** (Android 8.0)

## Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

Or open the project in Android Studio and run directly.

## Permissions

The app requests `CAMERA` and `READ_MEDIA_IMAGES` at runtime on launch. Denying camera permission will prevent the preview from loading.

## Project Structure

```
app/src/main/java/tech/jptr/camera/
├── CameraApp.kt              # Application class
├── MainActivity.kt           # Single activity
├── camera/
│   ├── CameraController.kt   # ProcessCameraProvider wrapper
│   ├── CameraState.kt        # ViewModel + state data class
│   ├── PhotoSaver.kt         # MediaStore JPEG save
│   └── UseCases.kt           # CameraX use case factories
├── ui/
│   ├── CameraScreen.kt       # Main composable orchestrator
│   ├── ControlsOverlay.kt    # Shutter, switch, flash buttons
│   ├── FocusIndicator.kt     # Animated focus ring
│   ├── Viewfinder.kt         # CameraXViewfinder wrapper
│   └── theme/                # Material 3 theme + colors
└── util/
    ├── Extensions.kt         # Custom IconButton / ShutterButton
    └── ImageUtils.kt         # YUV-to-Bitmap, rotation, scaling
```

## Tech Stack

- Kotlin 2.3.0
- Jetpack Compose with Material 3
- CameraX 1.6.1
- KSP (annotation processing)
- Coroutines
