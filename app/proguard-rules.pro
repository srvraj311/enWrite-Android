# Firestore maps documents onto our note classes by field name; keep them intact when shrinking.
-keepclassmembers class com.srvraj311.data.** { *; }
-keepattributes Signature, *Annotation*

# CommonMark loads its extensions reflectively.
-keep class org.commonmark.** { *; }
-dontwarn org.commonmark.**
