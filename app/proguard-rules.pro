# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/EdipoSouza/Library/Android/sdk/tools/proguard/proguard-android.txt
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

## TES Legends Tracker
-keepclassmembers class com.ediposouza.teslesgendstracker.interactor.FirebaseParsers$* { *; }

## Kotlin
-dontwarn kotlin.**
-dontwarn org.jetbrains.anko.internals.AnkoInternals

## The support library contains references to newer platform versions.
-dontwarn android.support.**
-keep class android.support.** { *; }

## EventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

## OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

## Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okio.**

## Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.integration.okhttp3.OkHttpGlideModule
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

## Gson
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keep class sun.misc.Unsafe { *; }

## ThreeTenABP
-dontwarn org.junit.**
-dontwarn android.test.**
-dontwarn android.support.test.**

## MixPanel
-dontwarn com.mixpanel.**
-keep class **.R$* {
    <fields>;
}

## JSoup
-keep public class org.jsoup.** {
    public *;
}

## Ads
-keep public class com.google.android.gms.ads.** {
   public *;
}

-keep public class com.google.ads.** {
   public *;
}