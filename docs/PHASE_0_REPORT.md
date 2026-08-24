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

---

# Validate thực tế trên Samsung Galaxy A32

Chạy ngày 2026-08-24, sau khi Phase 0 baseline được commit.

## Thiết bị & bản build

| | |
|---|---|
| Device | **SM-A325F** (Samsung Galaxy A32) |
| Android | **13** / API **33** |
| ABI | **arm64-v8a** |
| Kết nối | ADB wireless |
| APK | `app/build/outputs/apk/debug/app-debug.apk` (13.16 MB, debug, chưa minify) |

## Kết quả

| Hạng mục | Kết quả |
|---|---|
| Install | **Success** (`adb install -r`, 24.8s) |
| Launch | **Success** — `am start -W -n com.nepnha/.MainActivity` → `Status: ok` |
| MainActivity | `com.nepnha/.MainActivity` |
| Cold start | `LaunchState: COLD`, `TotalTime: 1905ms`, `WaitTime: 1912ms` |
| Startup failure | **Không** |
| FATAL EXCEPTION | **0** |
| AndroidRuntime error | **0** |
| ANR | **0** |
| Permission issue | **Không** |
| INTERNET permission | **Không có** |
| Kiểm tra quyền thực tế (`dumpsys package com.nepnha`) | Chỉ có `com.nepnha.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` do AGP sinh ra (cục bộ, không liên quan mạng) |

Ảnh chụp màn hình xác nhận UI render đúng: chữ "NẾP NHÀ" và dòng trạng thái Phase 0,
dấu tiếng Việt hiển thị chuẩn, edge-to-edge hoạt động, status bar / navigation bar
bình thường.

## Ghi chú baseline hiệu năng

- Lần khởi động đầu tiên có `Choreographer: Skipped 69 frames`.
- **Chưa coi đây là performance bug**: debug build, không minify, còn `ui-tooling`,
  JIT chưa ấm và chưa có baseline profile.
- Sẽ benchmark lại nghiêm túc ở **Phase 10** bằng release build.
- `ProfileInstaller` chạy bình thường và hoàn toàn cục bộ, không phát sinh mạng.

## Ghi chú sự cố "màn hình đen"

Ảnh chụp màn hình đầu tiên đen hoàn toàn.

- Nguyên nhân: `dumpsys power` cho `mWakefulness=Dozing` — màn hình thiết bị đang tắt.
- App thực tế **vẫn đang chạy đúng**: `ResumedActivity` và `mFocusedApp` đều trỏ tới
  `com.nepnha/.MainActivity`.
- Xử lý: `input keyevent KEYCODE_WAKEUP` rồi chụp lại → UI hiển thị đúng.
- **Không có code defect.** Không phải sửa gì.
