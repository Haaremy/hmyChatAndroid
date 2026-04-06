# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class de.haaremy.hmychat.data.api.models.**$$serializer { *; }
-keepclassmembers class de.haaremy.hmychat.data.api.models.** {
    *** Companion;
}
-keepclasseswithmembers class de.haaremy.hmychat.data.api.models.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit
-keepattributes Signature, Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Error Prone annotations (compile-only, not present at runtime)
-dontwarn com.google.errorprone.annotations.**
