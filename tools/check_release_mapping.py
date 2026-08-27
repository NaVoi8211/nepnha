#!/usr/bin/env python3
"""
Kiểm tra R8 KHÔNG đổi tên những hằng enum được lưu xuống database.

Vì sao cần: `MemorialRepository`/`MemberRepository` ghi `enum.name` thành chuỗi trong
Room rồi đọc lại bằng so sánh tên. R8 đổi tên hằng thì trong cùng một bản build vẫn
khớp — **test trên bản debug không bao giờ bắt được** — nhưng bản build sau có thể gán
chữ cái khác và dữ liệu người dùng thành không đọc được.

Chạy sau mỗi lần build release:
    ./gradlew :app:assembleRelease && python3 tools/check_release_mapping.py
"""
import os
import re
import sys

MAPPING = "app/build/outputs/mapping/release/mapping.txt"

# (lớp enum, các hằng bắt buộc giữ nguyên tên vì đã nằm trong database)
PERSISTED = {
    "com.nepnha.domain.event.LeapMonthPolicy":
        ["COMMON_MONTH_DEFAULT", "LEAP_MONTH_PREFERRED", "LEAP_MONTH_ONLY"],
    "com.nepnha.domain.event.MissingDayPolicy":
        ["LAST_VALID_DAY_OF_MONTH", "SKIP"],
    "com.nepnha.domain.model.Gender":
        ["MALE", "FEMALE", "UNSPECIFIED"],
    "com.nepnha.domain.model.LunarBirthDate$Source":
        ["USER_PROVIDED"],
}


def main() -> int:
    if not os.path.exists(MAPPING):
        print(f"⛔ không thấy {MAPPING} — hãy chạy ./gradlew :app:assembleRelease trước")
        return 2
    text = open(MAPPING, encoding="utf-8").read()
    problems = []
    for cls, constants in PERSISTED.items():
        # Khối của một lớp gồm dòng khai báo rồi các dòng thụt đầu dòng HOẶC dòng
        # chú thích `# {...}` bắt đầu từ cột 0 — bỏ sót dạng thứ hai thì khối bị cắt
        # ngay dòng đầu và mọi hằng đều bị báo "không có trong mapping".
        block = re.search(
            rf"^{re.escape(cls)} -> .*?$((?:\n(?:[ \t#].*)?)*)", text, re.M)
        if not block:
            problems.append(f"{cls}: không tìm thấy trong mapping (bị loại bỏ hoàn toàn?)")
            continue
        body = block.group(1)
        for const in constants:
            m = re.search(rf"{re.escape(cls)} {const} -> (\S+)", body)
            if not m:
                problems.append(f"{cls}.{const}: không có trong mapping")
            elif m.group(1) != const:
                problems.append(
                    f"{cls}.{const}: BỊ ĐỔI TÊN thành '{m.group(1)}' "
                    "⇒ dữ liệu đã lưu sẽ không đọc được ở bản build sau")
    if problems:
        print("⛔ MAPPING KHÔNG AN TOÀN:")
        for p in problems:
            print("   -", p)
        return 1
    total = sum(len(v) for v in PERSISTED.values())
    print(f"✓ {total} hằng enum được lưu xuống database đều giữ nguyên tên trong bản release")
    return 0


if __name__ == "__main__":
    sys.exit(main())
