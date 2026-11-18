# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers and hide original filenames in Crashlytics stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all project application classes, activities, viewmodels, models, and states
-keep class com.example.** { *; }
-keepclassmembers class com.example.** { *; }

# Kotlin Metadata & Moshi JSON models
-keep class kotlin.Metadata { *; }
-keep class com.example.data.model.** { *; }
-keep class * extends com.squareup.moshi.JsonAdapter { *; }
-keep class com.squareup.moshi.** { *; }
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Unity Ads SDK
-keep class com.unity3d.** { *; }
-dontwarn com.unity3d.**

# OkHttp optional JVM/alternative TLS providers (not used on Android)
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**


