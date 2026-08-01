# Keep main activity and all application classes
-keep class com.example.MainActivity { *; }
-keep class com.example.** { *; }
-keepclassmembers class com.example.** { *; }

# Keep Jetpack Compose classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep Lifecycle and ViewModel
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }

# Keep Navigation
-keep class androidx.navigation.** { *; }
-keepclassmembers class androidx.navigation.** { *; }

# Keep Room
-keep class androidx.room.** { *; }
-keepclassmembers class androidx.room.** { *; }

# Keep Moshi and Kotlin Reflection
-keepclassmembers class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}
-keep class * implements java.io.Serializable { *; }

