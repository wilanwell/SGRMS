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
# Firestore fills these classes by reflection (DocumentSnapshot.toObject),
# matching document fields to property names. Keep their names and the
# no-argument constructors, or R8 renames them and reads come back empty.
-keep class com.wilanwell.sgrms.data.model.** { *; }
-keepclassmembers class com.wilanwell.sgrms.data.model.** { <init>(); }

# Needed for generic types and annotations such as @IgnoreExtraProperties.
-keepattributes Signature,*Annotation*,InnerClasses,EnclosingMethod
