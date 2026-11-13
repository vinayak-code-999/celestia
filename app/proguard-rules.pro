# ProGuard / R8 rules for Celestia Release Build

# Kotlinx Serialization
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Celestia Core Data & Models
-keep class ai.moon.celestia.core.model.** { *; }
-keep class ai.moon.celestia.core.database.** { *; }
