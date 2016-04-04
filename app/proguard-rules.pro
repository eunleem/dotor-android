# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\jeind\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

-optimizations !code/allocation/variable

-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-keep enum android.support.v7.** { *; }

-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep enum android.support.v4.** { *; }

-keep class com.google.** { *; }
-keep interface com.google.** { *; }
-keep enum com.google.** { *; }

-dontwarn com.google.appengine.api.urlfetch.**
-keep class com.google.appengine.api.datastore.Text { *; }


# For using GSON @Expose annotation
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

-keep interface com.squareup.okhttp.** { *; }
-keep class com.squareup.okhttp.** { *; }
-keep class retrofit.** { *; }

-dontwarn com.squareup.okhttp.**
-dontwarn retrofit.**
-dontwarn okio.**
-dontwarn retrofit2.Platform$Java8
-dontwarn rx.**
-keepclasseswithmembers class * {
    @retrofit.http.* <methods>;
}


# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }



-dontwarn javax.**
-dontwarn java.lang.management.**
-dontwarn javax.xml.stream.events.**

-keep class javax.** { *; }
-keep class java.lang.management.** { *; }

