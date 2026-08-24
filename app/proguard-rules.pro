# Nếp Nhà — release rules
# kotlinx-serialization: giữ serializer sinh bởi compiler plugin.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.nepnha.** {
    *** Companion;
}
-keepclasseswithmembers class com.nepnha.** {
    kotlinx.serialization.KSerializer serializer(...);
}
