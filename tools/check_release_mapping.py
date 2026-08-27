#!/usr/bin/env python3
"""
Kiểm tra R8 KHÔNG đổi tên những hằng enum mà app đã lưu xuống database.

Vì sao cần: `MemorialRepository`/`MemberRepository`/`BackupRepository` ghi `enum.name`
thành chuỗi trong Room rồi đọc lại bằng so sánh tên. R8 đổi tên hằng thì trong cùng một
bản build vẫn khớp — **test trên bản debug không bao giờ bắt được** — nhưng bản build
sau có thể gán chữ cái khác và dữ liệu người dùng thành không đọc được.

Ba lớp kiểm, vì mỗi lớp bắt một kiểu hỏng khác nhau:

  1. RULE      — rule giữ tên trong `proguard-rules.pro` còn nguyên.
                 Xoá nó là mọi thứ dưới đây hỏng cùng lúc.
  2. SITES     — tập các chỗ `enum.name` chảy vào Room đúng bằng tập đã ghi nhận.
                 Thêm một chỗ mới mà quên khai báo enum thì fail ở đây.
  3. MAPPING   — mọi enum dưới `com.nepnha` không bị đổi tên trong bản release, và
                 riêng những enum đã lưu xuống database thì bắt buộc phải có mặt.

Chạy sau mỗi lần build release:
    ./gradlew :app:assembleRelease && python3 tools/check_release_mapping.py
"""
import os
import re
import sys

MAPPING = "app/build/outputs/mapping/release/mapping.txt"
SOURCE_ROOT = "app/src/main/java"
PROGUARD = "app/proguard-rules.pro"

REQUIRED_KEEP_RULE = "-keepclassmembers enum com.nepnha.** {"

# Enum mà giá trị `.name` của nó ĐANG NẰM trong database của người dùng.
# Với nhóm này, "không có trong mapping" cũng là lỗi: nghĩa là R8 đã xoá mất lớp mà
# code đọc dữ liệu cũ vẫn cần.
PERSISTED_ENUMS = {
    "com.nepnha.domain.event.LeapMonthPolicy",
    "com.nepnha.domain.event.MissingDayPolicy",
    "com.nepnha.domain.model.Gender",
    "com.nepnha.domain.model.LunarBirthDate$Source",
}

# Mọi chỗ `.name` của một enum được ghi xuống Room, ghi nhận thành ảnh chụp.
# Danh sách này KHÔNG phải trang trí: nếu tập thực tế lệch đi thì có người vừa thêm
# (hoặc bỏ) một đường lưu enum, và `PERSISTED_ENUMS` ở trên phải được xem lại.
PERSISTENCE_SITES = {
    ('data/repository/BackupRepository.kt',
     'com.nepnha.domain.event.LeapMonthPolicy.entries.firstOrNull { it.name == stored }'),
    ('data/repository/BackupRepository.kt',
     'com.nepnha.domain.event.MissingDayPolicy.entries.firstOrNull { it.name == stored }'),
    ('data/repository/BackupRepository.kt',
     'gender = m.gender.name'),
    ('data/repository/BackupRepository.kt',
     'leapMonthPolicy = x.leapMonthPolicy.name'),
    ('data/repository/BackupRepository.kt',
     'lunarBirthSource = m.lunarBirthDate?.let { LunarBirthDate.Source.USER_PROVIDED.name }'),
    ('data/repository/BackupRepository.kt',
     'missingDayPolicy = x.missingDayPolicy.name'),
    ('data/repository/MemberRepository.kt',
     'gender = gender.name'),
    ('data/repository/MemberRepository.kt',
     'lunarBirthSource = lunarBirthDate?.source?.name'),
    ('data/repository/MemorialRepository.kt',
     'leapMonthPolicy = LeapMonthPolicy.entries.firstOrNull { it.name == leapMonthPolicy }'),
    ('data/repository/MemorialRepository.kt',
     'leapMonthPolicy = rule.leapMonthPolicy.name'),
    ('data/repository/MemorialRepository.kt',
     'missingDayPolicy = MissingDayPolicy.entries.firstOrNull { it.name == missingDayPolicy }'),
    ('data/repository/MemorialRepository.kt',
     'missingDayPolicy = rule.missingDayPolicy.name'),
    ('domain/model/FamilyModels.kt',
     'entries.firstOrNull { it.name == raw } ?: UNSPECIFIED'),
}

# Cột Room ĐANG chứa `enum.name`. Đây là danh sách cần bảo vệ.
ENUM_COLUMNS = {"gender", "lunarBirthSource", "leapMonthPolicy", "missingDayPolicy"}

# Cột Room chứa văn bản người dùng nhập, tình cờ cũng gán từ một `.name` nào đó
# (`Memorial.name`, `Family.name`). Không liên quan tới R8.
TEXT_COLUMNS = {"name", "fullName", "familyName", "role", "note"}


def strip_comments(text: str) -> str:
    """
    Bỏ comment, giữ nguyên độ dài từng dòng.

    Bắt buộc phải làm trước khi phân tích: phần thân enum được tách khỏi phần thân lớp
    bằng dấu `;`, mà doc comment tiếng Việt thì đầy dấu chấm phẩy — "giữ đúng nguyên
    tắc; hệ quả là…" cắt mất hằng cuối của `LeapMonthPolicy` và cả hai hằng của
    `MissingDayPolicy`, rồi script vẫn báo xanh. Đúng loại lỗi mà một công cụ kiểm tra
    an toàn không được phép mắc.
    """
    out, i, n = [], 0, len(text)
    while i < n:
        if text.startswith("/*", i):
            end = text.find("*/", i + 2)
            end = n if end == -1 else end + 2
            out.append("".join(c if c == "\n" else " " for c in text[i:end]))
            i = end
        elif text.startswith("//", i):
            end = text.find("\n", i)
            end = n if end == -1 else end
            out.append(" " * (end - i))
            i = end
        else:
            out.append(text[i])
            i += 1
    return "".join(out)


def kotlin_files():
    for dirpath, _, filenames in os.walk(SOURCE_ROOT):
        for filename in sorted(filenames):
            if filename.endswith(".kt"):
                yield os.path.join(dirpath, filename)


def find_persistence_sites() -> tuple:
    """
    Tìm mọi chỗ gán `<cột> = <gì đó>.name` trong tầng repository, rồi phân loại theo
    **tên cột** chứ không đoán xem vế phải có phải enum hay không.

    Đoán theo vế phải là ngõ cụt: `name = x.name` (tên ngày giỗ) và
    `gender = m.gender.name` (hằng enum) trông giống hệt nhau khi không có kiểu.
    Tên cột thì rõ ràng, và cột mới xuất hiện sẽ buộc người sửa phải phân loại.

    Trả về `(chỗ lưu enum, chỗ chưa phân loại)`.
    """
    enum_sites, unknown = set(), set()
    assign = re.compile(r"(\w+)\s*=\s*(.*\.name\b.*)$")
    # Đường ĐỌC ngược lại cũng phải được ghi nhận: `entries.firstOrNull { it.name == raw }`
    # nằm ở tầng domain chứ không ở repository, và nó hỏng theo đúng cùng một cách.
    read_back = re.compile(r"\bit\.name\s*==")
    for path in kotlin_files():
        rel = path.split("java/com/nepnha/", 1)[1]
        for raw in strip_comments(open(path, encoding="utf-8").read()).splitlines():
            stripped = raw.strip().rstrip(",")
            if read_back.search(stripped):
                enum_sites.add((rel, stripped))
        if "/data/repository/" not in path:
            continue
        for raw in strip_comments(open(path, encoding="utf-8").read()).splitlines():
            stripped = raw.strip().rstrip(",")
            m = assign.match(stripped)
            if not m:
                continue
            column = m.group(1)
            if column in TEXT_COLUMNS:
                continue
            if column in ENUM_COLUMNS:
                enum_sites.add((rel, stripped))
            else:
                unknown.add((rel, f"cột '{column}': {stripped}"))
    return enum_sites, unknown


def discover_enums() -> dict:
    """
    Tìm MỌI enum khai báo dưới `com.nepnha`, kèm tên đầy đủ đúng kiểu R8 (`Ngoai$Trong`).

    Bản đầu của script giữ một danh sách viết tay 9 hằng. Danh sách viết tay chỉ bảo vệ
    được những gì người viết còn nhớ; quét từ nguồn thì enum mới tự động được kiểm.
    """
    enums = {}
    for path in kotlin_files():
        text = strip_comments(open(path, encoding="utf-8").read())
        package = re.search(r"^package\s+([\w.]+)", text, re.M)
        if not package:
            continue
        enums.update(scan_declarations(text, package.group(1)))
    return enums


def scan_declarations(text: str, package: str) -> dict:
    """
    Duyệt theo độ sâu ngoặc để biết một enum lồng trong những lớp nào.

    Không dùng regex "tìm lớp bao ngoài gần nhất": `LunarError.InvalidLunarDate.Reason`
    nằm sâu hai tầng và cách đó cho ra `LunarError$Reason` — một tên không tồn tại,
    khiến script báo động giả rồi bị bỏ qua.
    """
    decl = re.compile(r"\b(?:enum class|class|object|interface)\s+(\w+)")
    enums, stack, depth = {}, [], 0
    i, pending = 0, None
    while i < len(text):
        ch = text[i]
        if ch == "{":
            depth += 1
            stack.append(pending)
            pending = None
        elif ch == "}":
            depth -= 1
            if stack:
                stack.pop()
        else:
            m = decl.match(text, i)
            if m:
                pending = (m.group(1), text.startswith("enum class", i))
                i = m.end()
                if pending[1]:
                    names = [n for n, _ in filter(None, stack)] + [pending[0]]
                    body_start = text.find("{", i)
                    body = read_block(text, body_start)
                    head = body.split(";", 1)[0]
                    constants = sorted(set(
                        # Dấu phân cách phải nằm trong lookahead: nếu nuốt luôn dấu
                        # phẩy thì hai hằng viết chung một dòng
                        # (`{ DAY_OUT_OF_RANGE, MONTH_OUT_OF_RANGE }`) chỉ nhận được cái
                        # đầu — thiếu trong im lặng, đúng thứ script này phải chống.
                        re.findall(r"(?:^|,)\s*([A-Z][A-Z0-9_]*)\s*(?=[(,\n}]|$)", head, re.M)
                    ))
                    if constants:
                        enums[f"{package}.{'$'.join(names)}"] = constants
                continue
        i += 1
    return enums


def read_block(text: str, start: int) -> str:
    """Trả về nội dung giữa cặp ngoặc nhọn bắt đầu tại `start`."""
    if start < 0:
        return ""
    depth, i = 0, start
    while i < len(text):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                return text[start + 1:i]
        i += 1
    return text[start + 1:]


def check_keep_rule() -> list:
    rules = open(PROGUARD, encoding="utf-8").read()
    if REQUIRED_KEEP_RULE in rules:
        return []
    return [
        f"{PROGUARD}: THIẾU rule giữ tên hằng enum ({REQUIRED_KEEP_RULE!r}) — "
        "xoá nó đi là mọi enum lưu xuống database bị R8 đổi tên trong im lặng"
    ]


def check_sites() -> list:
    actual, unknown = find_persistence_sites()
    problems = []
    for site in sorted(unknown):
        problems.append(
            f"CỘT CHƯA PHÂN LOẠI nhận giá trị `.name`: {site[0]} :: {site[1]}\n"
            "     ⇒ xếp vào ENUM_COLUMNS (nếu là hằng enum) hoặc TEXT_COLUMNS "
            "(nếu là văn bản người dùng)")
    for site in sorted(actual - PERSISTENCE_SITES):
        problems.append(
            f"CHỖ LƯU ENUM MỚI chưa được ghi nhận: {site[0]} :: {site[1]}\n"
            "     ⇒ thêm vào PERSISTENCE_SITES và kiểm xem enum đó đã có trong "
            "PERSISTED_ENUMS chưa"
        )
    for site in sorted(PERSISTENCE_SITES - actual):
        problems.append(
            f"chỗ lưu enum đã biến mất: {site[0]} :: {site[1]}\n"
            "     ⇒ nếu là cố ý thì bỏ khỏi PERSISTENCE_SITES"
        )
    return problems


def check_mapping(text: str, enums: dict) -> tuple:
    problems, renamed_ok, absent = [], 0, []
    for cls, constants in sorted(enums.items()):
        block = re.search(
            rf"^{re.escape(cls)} -> .*?$((?:\n(?:[ \t#].*)?)*)", text, re.M)
        if not block:
            # Enum không lưu xuống database mà vắng mặt = R8 đã xoá vì không dùng tới.
            # Đó là kết quả mong muốn của minify, không phải lỗi.
            if cls in PERSISTED_ENUMS:
                problems.append(
                    f"{cls}: KHÔNG có trong mapping dù dữ liệu người dùng đang lưu "
                    "tên hằng của nó")
            else:
                absent.append(cls)
            continue
        body = block.group(1)
        for const in constants:
            m = re.search(rf"{re.escape(cls)} {const} -> (\S+)", body)
            if not m:
                if cls in PERSISTED_ENUMS:
                    problems.append(f"{cls}.{const}: không có trong mapping")
            elif m.group(1) != const:
                problems.append(
                    f"{cls}.{const}: BỊ ĐỔI TÊN thành '{m.group(1)}' "
                    "⇒ dữ liệu đã lưu sẽ không đọc được ở bản build sau")
            else:
                renamed_ok += 1
    return problems, renamed_ok, absent


def main() -> int:
    if not os.path.exists(MAPPING):
        print(f"⛔ không thấy {MAPPING} — hãy chạy ./gradlew :app:assembleRelease trước")
        return 2

    enums = discover_enums()
    if not enums:
        print("⛔ không quét thấy enum nào dưới com.nepnha — script hỏng chứ không phải code sạch")
        return 2
    missing = PERSISTED_ENUMS - set(enums)
    if missing:
        print(f"⛔ PERSISTED_ENUMS nhắc tới enum không còn tồn tại: {sorted(missing)}")
        return 2

    text = open(MAPPING, encoding="utf-8").read()
    problems = check_keep_rule() + check_sites()
    mapping_problems, kept, absent = check_mapping(text, enums)
    problems += mapping_problems

    if problems:
        print("⛔ MAPPING KHÔNG AN TOÀN:")
        for p in problems:
            print("   -", p)
        return 1

    print(f"✓ rule giữ tên enum còn nguyên trong {PROGUARD}")
    print(f"✓ {len(PERSISTENCE_SITES)} chỗ ghi enum.name xuống Room đúng như đã ghi nhận")
    print(f"✓ {len(enums)} enum dưới com.nepnha được quét, {kept} hằng giữ nguyên tên")
    for cls in sorted(PERSISTED_ENUMS):
        print(f"   · [database] {cls}: {', '.join(enums[cls])}")
    if absent:
        print(f"   ({len(absent)} enum không lưu xuống database đã được R8 loại bỏ — bình thường)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
