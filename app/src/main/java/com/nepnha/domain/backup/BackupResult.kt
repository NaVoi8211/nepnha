package com.nepnha.domain.backup

/**
 * Kết quả đọc một file sao lưu.
 *
 * Sealed thay vì `BackupFile?`: một file hỏng cần mang theo **danh sách lý do** để nói
 * cho người dùng biết file của họ sai ở đâu, chứ không phải im lặng trả `null`.
 */
sealed interface BackupResult {
    data class Valid(val file: BackupFile) : BackupResult
    data class Invalid(val errors: List<BackupError>) : BackupResult
}

/**
 * Một vấn đề cụ thể trong file.
 *
 * Mỗi lỗi mang theo **vị trí** (`members[2].fullName`) vì "file không hợp lệ" là câu
 * vô dụng với người đang cầm một file 200 dòng.
 */
sealed interface BackupError {
    data object EmptyFile : BackupError
    data object NotJson : BackupError
    data object MissingFormatVersion : BackupError
    data class UnsupportedFormatVersion(val found: Int, val supported: Int) : BackupError
    data class MissingField(val where: String) : BackupError
    data class WrongType(val where: String, val expected: String) : BackupError
    data class OutOfRange(val where: String, val value: Int, val min: Int, val max: Int) : BackupError
    data class BadEnum(val where: String, val value: String?, val allowed: List<String>) : BackupError
    data class BadDate(val where: String, val value: String) : BackupError
    data class TooLong(val where: String, val max: Int) : BackupError
    data class DuplicateRef(val ref: Int) : BackupError
    data class DanglingReference(val where: String, val ref: Int) : BackupError
    data object ChecksumMismatch : BackupError
}
