# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep MediaPipe and TensorFlow Lite classes
-keep class com.google.mediapipe.** { *; }
-keep class org.tensorflow.lite.** { *; }

# Keep Room entities and DAOs
-keep class com.smsexpensetracker.TransactionData { *; }
-keep class com.smsexpensetracker.TransactionDao { *; }

# Keep Gson serialized classes
-keep class com.smsexpensetracker.BankIdentifier { *; }

# Keep chart library classes
-keep class com.github.mikephil.charting.** { *; }