# Phase 0 — báo cáo môi trường (đo ngày 2026-08-24)

## Máy dev
MacBook Pro 2019, Intel i5-8257U (4 core), RAM 8GB, macOS 15.7.7, còn 42GB đĩa.

## Toolchain
| Thành phần | Trạng thái |
|---|---|
| JDK trên PATH | ❌ không có → dùng JBR của Android Studio |
| JBR | OpenJDK **25.0.2** tại `/Applications/Android Studio.app/Contents/jbr/Contents/Home` |
| Android Studio | 2026.1 (AI-261.26222.65) |
| Gradle | 9.7.0 (dist đã cache; wrapper đã tạo trong project) |
| Android SDK | `~/Library/Android/sdk` |
| Platforms đã cài | `android-35`, `android-37.0` (**không có API 36**) |
| Build-tools | 36.0.0 |
| platform-tools / adb | 37.0.1 |
| cmdline-tools | ❌ **thiếu** (`sdkmanager` CLI không dùng được) |
| Thiết bị | `adb devices` rỗng — **Samsung A32 chưa kết nối** |

## Bất ngờ gặp phải khi build
1. **AGP 9 có Kotlin built-in.** Apply `org.jetbrains.kotlin.android` làm build fail:
   *"The 'org.jetbrains.kotlin.android' plugin is no longer required for Kotlin
   support since AGP 9.0"*. Đã bỏ plugin đó; `kotlin { compilerOptions { … } }` vẫn
   dùng được bình thường. Plugin `kotlin.plugin.compose` và
   `kotlin.plugin.serialization` vẫn phải apply như cũ.
2. Cần `local.properties` với `sdk.dir` vì `ANDROID_HOME` không được set.

## Baseline hiệu năng (để so sánh về sau)
- Build **incremental** `:app:assembleDebug`: ~48s
- `:app:testDebugUnitTest` lần đầu (khởi động Kotlin daemon + biên dịch test): ~4m30s
- APK debug: ~13MB (chưa minify, có `ui-tooling`)

## Kiểm chứng ràng buộc
- `aapt2 dump permissions app-debug.apk` → **không có quyền nào ngoài**
  `com.nepnha.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (do AGP tự chèn, cục bộ).
  **Không có INTERNET.**
- Quét `debugRuntimeClasspath`: **0** kết quả cho okhttp / retrofit / volley / ktor /
  firebase / play-services / crashlytics / grpc.
- Unit test: **3/3 pass**.
