# Room
-keep class androidx.room.** { *; }
-keep class com.example.lairsheet.data.** { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# kotlinx.serialization
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class ** { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.Serializable <fields>;
}
