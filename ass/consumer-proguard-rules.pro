# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepclassmembers class * { public <init>(android.content.Context); }

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

#retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

#okhttp
-dontwarn okio.**
-dontwarn okhttp3.internal.platform.*

#picasso
-dontwarn com.squareup.picasso.OkHttpDownloader

# moshi
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# api classes
-keep class cz.ackee.ass.api.AssRequest { *; }

#needed for moshi code generation
-dontwarn org.jetbrains.annotations.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class cz.ackee.example.** { <init>(...); <fields>;}
-keep @android.support.annotation.Keep class * {*;}