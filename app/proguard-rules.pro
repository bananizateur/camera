# CameraX
-keep class androidx.camera.** { *; }
-keep interface androidx.camera.** { *; }

# Compose
-keep class androidx.compose.** { *; }

# Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Material 3
-keep class androidx.compose.material3.** { *; }

# Keep all Camera classes
-keep class tech.jptr.camera.camera.** { *; }

# Keep all UI classes
-keep class tech.jptr.camera.ui.** { *; }

# Keep all util classes
-keep class tech.jptr.camera.util.** { *; }

# Keep Application class
-keep class tech.jptr.camera.CameraApp { *; }

# Keep Activity
-keep class tech.jptr.camera.MainActivity { *; }

# Keep ViewModel
-keep class tech.jptr.camera.CameraViewModel { *; }

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable { *; }

# Keep Enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Suppress warnings for missing classes
-dontwarn androidx.camera.**
-dontwarn androidx.compose.**
-dontwarn kotlinx.coroutines.**