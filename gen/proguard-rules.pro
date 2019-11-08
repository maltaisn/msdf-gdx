-dontobfuscate

# JCommander
-keep class com.beust.jcommander.** { *; }

# pngtastic
-dontwarn com.googlecode.pngtastic.ant.PngOptimizerTask

# libGDX
-dontwarn org.lwjgl.**
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.jnigen.**

# Kotlin coroutines
-dontwarn kotlinx.coroutines.flow.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Enums
-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Entry points
-keep public class com.maltaisn.msdfgdx.gen.MainKt {
    public static void main(java.lang.String[]);
}
-keepclassmembers class com.maltaisn.msdfgdx.gen.Parameters { *; }
