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

# ---------------------------------------------------------------------------
# TÊN HẰNG ENUM ĐƯỢC LƯU XUỐNG DATABASE — KHÔNG ĐƯỢC ĐỔI TÊN.
#
# `MemorialRepository` và `MemberRepository` ghi `enum.name` thành chuỗi trong Room
# (`leapMonthPolicy`, `missingDayPolicy`, `gender`, `lunarBirthSource`) rồi đọc lại
# bằng cách so sánh `it.name`.
#
# Không có rule này, R8 đổi `LEAP_MONTH_ONLY` thành `g` và `SKIP` thành `f`. Trong
# cùng một bản build thì vẫn khớp, nên **test debug không bao giờ bắt được**. Nhưng
# bản build sau có thể gán chữ cái khác ⇒ dữ liệu người dùng ghi bởi bản trước không
# đọc được nữa và policy âm thầm quay về mặc định. Người dùng chọn "chỉ tháng nhuận"
# rồi cập nhật app là mất lựa chọn mà không có thông báo nào.
#
# Phát hiện ở Gate 4 Phase 6 bằng cách đọc mapping.txt. `tools/check_release_mapping.py`
# canh để nó không tái phát.
# ---------------------------------------------------------------------------
-keepclassmembers enum com.nepnha.** {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
