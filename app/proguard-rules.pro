# AegisVPN ProGuard Rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep data models
-keep class com.aegisvpn.app.data.model.** { *; }

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Keep Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
