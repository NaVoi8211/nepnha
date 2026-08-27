package com.nepnha.domain.backup

import java.time.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Đọc/ghi file sao lưu. Kotlin thuần — không Android, nên test được trên JVM.
 *
 * Cố ý **không** dùng `@Serializable` sinh tự động: định dạng trên đĩa của người dùng
 * phải do hợp đồng quyết định, không do hình dạng lớp Kotlin quyết định. Đổi tên một
 * thuộc tính trong mã **không được** làm hỏng file đã sao lưu từ trước.
 *
 * Mọi lỗi được **gom lại và trả cùng lúc**: người dùng cần thấy toàn bộ vấn đề của file
 * chứ không phải sửa từng vòng.
 */
object BackupCodec {

    private val json = Json { prettyPrint = true; prettyPrintIndent = "  " }
    private val strict = Json

    // ---------------------------------------------------------------- ghi

    fun encode(data: BackupData, exportedAt: String, appVersionName: String?): String {
        val root = buildJsonObject {
            put("formatVersion", JsonPrimitive(BackupFormat.SUPPORTED_VERSION))
            put("exportedAt", JsonPrimitive(exportedAt))
            put("appVersionName", JsonPrimitive(appVersionName))
            put("checksum", JsonPrimitive(BackupChecksum.of(data)))
            put(
                "data",
                buildJsonObject {
                    put("familyName", JsonPrimitive(data.familyName))
                    put("primaryMemberRef", JsonPrimitive(data.primaryMemberRef))
                    put("members", buildJsonArray { data.members.forEach { add(encodeMember(it)) } })
                    put("memorials", buildJsonArray { data.memorials.forEach { add(encodeMemorial(it)) } })
                },
            )
        }
        return json.encodeToString(JsonObject.serializer(), root)
    }

    private fun encodeMember(m: BackupMember) = buildJsonObject {
        put("ref", JsonPrimitive(m.ref))
        put("fullName", JsonPrimitive(m.fullName))
        put("gender", JsonPrimitive(BackupFormat.wire(m.gender)))
        put("solarBirthDate", JsonPrimitive(m.solarBirthDate))
        put(
            "lunarBirthDate",
            m.lunarBirthDate?.let {
                buildJsonObject {
                    put("day", JsonPrimitive(it.day))
                    put("month", JsonPrimitive(it.month))
                    put("year", JsonPrimitive(it.year))
                    put("leapMonth", JsonPrimitive(it.leapMonth))
                }
            } ?: kotlinx.serialization.json.JsonNull,
        )
        put("role", JsonPrimitive(m.role))
        put("note", JsonPrimitive(m.note))
    }

    private fun encodeMemorial(x: BackupMemorial) = buildJsonObject {
        put("name", JsonPrimitive(x.name))
        put("memberRef", JsonPrimitive(x.memberRef))
        put("lunarDay", JsonPrimitive(x.lunarDay))
        put("lunarMonth", JsonPrimitive(x.lunarMonth))
        put("leapMonthPolicy", JsonPrimitive(BackupFormat.wire(x.leapMonthPolicy)))
        put("missingDayPolicy", JsonPrimitive(BackupFormat.wire(x.missingDayPolicy)))
        put("note", JsonPrimitive(x.note))
    }

    // ---------------------------------------------------------------- đọc

    fun decode(text: String): BackupResult {
        if (text.isBlank()) return BackupResult.Invalid(listOf(BackupError.EmptyFile))

        val root = runCatching { strict.parseToJsonElement(text).jsonObject }.getOrNull()
            ?: return BackupResult.Invalid(listOf(BackupError.NotJson))

        val version = root["formatVersion"]?.jsonPrimitiveOrNull()?.intOrNull
            ?: return BackupResult.Invalid(listOf(BackupError.MissingFormatVersion))
        if (version > BackupFormat.SUPPORTED_VERSION) {
            return BackupResult.Invalid(
                listOf(BackupError.UnsupportedFormatVersion(version, BackupFormat.SUPPORTED_VERSION)),
            )
        }

        val body = root["data"]?.let { runCatching { it.jsonObject }.getOrNull() }
            ?: return BackupResult.Invalid(listOf(BackupError.MissingField("data")))

        val errors = mutableListOf<BackupError>()
        val members = decodeMembers(body, errors)
        val memorials = decodeMemorials(body, members.map { it.ref }.toSet(), errors)

        val primaryRaw = body["primaryMemberRef"]
        val primaryRef = primaryRaw?.jsonPrimitiveOrNull()?.intOrNull
        if (primaryRaw != null && primaryRaw !is kotlinx.serialization.json.JsonNull && primaryRef == null) {
            // Tín chủ ghi sai kiểu mà lặng lẽ bỏ qua thì người dùng khôi phục xong
            // mất tín chủ và không biết vì sao.
            errors += BackupError.WrongType("primaryMemberRef", "number")
        }
        if (primaryRef != null && members.none { it.ref == primaryRef }) {
            errors += BackupError.DanglingReference("primaryMemberRef", primaryRef)
        }

        if (errors.isNotEmpty()) return BackupResult.Invalid(errors)

        val data = BackupData(
            familyName = body["familyName"]?.jsonPrimitiveOrNull()?.contentOrNull,
            primaryMemberRef = primaryRef,
            members = members,
            memorials = memorials,
        )

        // Checksum kiểm SAU khi cấu trúc đã hợp lệ — báo "file hỏng" cho một file thiếu
        // trường thì vô nghĩa với người dùng.
        val declared = root["checksum"]?.jsonPrimitiveOrNull()?.contentOrNull
        if (declared != null && declared != BackupChecksum.of(data)) {
            return BackupResult.Invalid(listOf(BackupError.ChecksumMismatch))
        }

        return BackupResult.Valid(
            BackupFile(
                formatVersion = version,
                exportedAt = root["exportedAt"]?.jsonPrimitiveOrNull()?.contentOrNull.orEmpty(),
                appVersionName = root["appVersionName"]?.jsonPrimitiveOrNull()?.contentOrNull,
                data = data,
            ),
        )
    }

    private fun decodeMembers(body: JsonObject, errors: MutableList<BackupError>): List<BackupMember> {
        val array = body["members"]?.let { runCatching { it.jsonArray }.getOrNull() }
            ?: JsonArray(emptyList())
        val seen = mutableSetOf<Int>()
        val out = mutableListOf<BackupMember>()
        array.forEachIndexed { index, element ->
            val o = runCatching { element.jsonObject }.getOrNull()
            if (o == null) {
                errors += BackupError.WrongType("members[$index]", "object")
                return@forEachIndexed
            }
            val where = "members[$index]"
            val ref = o["ref"]?.jsonPrimitiveOrNull()?.intOrNull
            if (ref == null) {
                errors += BackupError.MissingField("$where.ref"); return@forEachIndexed
            }
            if (!seen.add(ref)) errors += BackupError.DuplicateRef(ref)

            val name = o["fullName"]?.jsonPrimitiveOrNull()?.contentOrNull?.trim()
            if (name.isNullOrEmpty()) {
                errors += BackupError.MissingField("$where.fullName"); return@forEachIndexed
            }
            if (name.length > BackupFormat.MAX_NAME_LENGTH) {
                errors += BackupError.TooLong("$where.fullName", BackupFormat.MAX_NAME_LENGTH)
            }

            val genderWire = o["gender"]?.jsonPrimitiveOrNull()?.contentOrNull
            val gender = genderWire?.let { BackupFormat.gender(it) }
            if (gender == null) {
                errors += BackupError.BadEnum("$where.gender", genderWire, BackupFormat.genderValues)
                return@forEachIndexed
            }

            val solar = o["solarBirthDate"]?.jsonPrimitiveOrNull()?.contentOrNull
            if (solar != null && runCatching { LocalDate.parse(solar) }.isFailure) {
                errors += BackupError.BadDate("$where.solarBirthDate", solar)
            }

            val lunar = decodeLunarBirth(o, where, errors)
            out += BackupMember(
                ref = ref,
                fullName = name,
                gender = gender,
                solarBirthDate = solar,
                lunarBirthDate = lunar,
                role = o["role"]?.jsonPrimitiveOrNull()?.contentOrNull?.checkLength("$where.role", errors),
                note = o["note"]?.jsonPrimitiveOrNull()?.contentOrNull?.checkLength("$where.note", errors),
            )
        }
        return out
    }

    private fun decodeLunarBirth(
        o: JsonObject,
        where: String,
        errors: MutableList<BackupError>,
    ): BackupLunarBirth? {
        val raw = o["lunarBirthDate"]
        if (raw == null || raw is kotlinx.serialization.json.JsonNull) return null
        // Có mặt nhưng sai kiểu thì phải BÁO, không được lặng lẽ bỏ đi: ngày sinh âm
        // biến mất không một lời nào cũng là mất dữ liệu.
        val l = runCatching { raw.jsonObject }.getOrNull()
        if (l == null) {
            errors += BackupError.WrongType("$where.lunarBirthDate", "object")
            return null
        }
        val day = l["day"]?.jsonPrimitiveOrNull()?.intOrNull
        val month = l["month"]?.jsonPrimitiveOrNull()?.intOrNull
        val year = l["year"]?.jsonPrimitiveOrNull()?.intOrNull
        if (day == null || month == null || year == null) {
            errors += BackupError.MissingField("$where.lunarBirthDate")
            return null
        }
        if (day !in 1..30) errors += BackupError.OutOfRange("$where.lunarBirthDate.day", day, 1, 30)
        if (month !in 1..12) errors += BackupError.OutOfRange("$where.lunarBirthDate.month", month, 1, 12)
        if (year !in BackupFormat.LUNAR_YEAR_RANGE) {
            errors += BackupError.OutOfRange(
                "$where.lunarBirthDate.year", year,
                BackupFormat.LUNAR_YEAR_RANGE.first, BackupFormat.LUNAR_YEAR_RANGE.last,
            )
        }
        return BackupLunarBirth(day, month, year, decodeLeapMonth(l, where, errors))
    }

    private fun decodeMemorials(
        body: JsonObject,
        knownRefs: Set<Int>,
        errors: MutableList<BackupError>,
    ): List<BackupMemorial> {
        val array = body["memorials"]?.let { runCatching { it.jsonArray }.getOrNull() }
            ?: JsonArray(emptyList())
        val out = mutableListOf<BackupMemorial>()
        array.forEachIndexed { index, element ->
            val o = runCatching { element.jsonObject }.getOrNull()
            if (o == null) {
                errors += BackupError.WrongType("memorials[$index]", "object")
                return@forEachIndexed
            }
            val where = "memorials[$index]"
            val name = o["name"]?.jsonPrimitiveOrNull()?.contentOrNull?.trim()
            if (name.isNullOrEmpty()) {
                errors += BackupError.MissingField("$where.name"); return@forEachIndexed
            }
            if (name.length > BackupFormat.MAX_NAME_LENGTH) {
                errors += BackupError.TooLong("$where.name", BackupFormat.MAX_NAME_LENGTH)
            }
            val day = o["lunarDay"]?.jsonPrimitiveOrNull()?.intOrNull
            val month = o["lunarMonth"]?.jsonPrimitiveOrNull()?.intOrNull
            if (day == null || month == null) {
                errors += BackupError.MissingField("$where.lunarDay/lunarMonth"); return@forEachIndexed
            }
            if (day !in 1..30) errors += BackupError.OutOfRange("$where.lunarDay", day, 1, 30)
            if (month !in 1..12) errors += BackupError.OutOfRange("$where.lunarMonth", month, 1, 12)

            val leapWire = o["leapMonthPolicy"]?.jsonPrimitiveOrNull()?.contentOrNull
            val leap = leapWire?.let { BackupFormat.leapPolicy(it) }
            if (leap == null) {
                errors += BackupError.BadEnum("$where.leapMonthPolicy", leapWire, BackupFormat.leapPolicyValues)
                return@forEachIndexed
            }
            val missingWire = o["missingDayPolicy"]?.jsonPrimitiveOrNull()?.contentOrNull
            val missing = missingWire?.let { BackupFormat.missingPolicy(it) }
            if (missing == null) {
                errors += BackupError.BadEnum(
                    "$where.missingDayPolicy", missingWire, BackupFormat.missingPolicyValues,
                )
                return@forEachIndexed
            }
            val memberRef = o["memberRef"]?.jsonPrimitiveOrNull()?.intOrNull
            if (memberRef != null && memberRef !in knownRefs) {
                errors += BackupError.DanglingReference("$where.memberRef", memberRef)
            }
            out += BackupMemorial(
                name = name,
                memberRef = memberRef,
                lunarDay = day,
                lunarMonth = month,
                leapMonthPolicy = leap,
                missingDayPolicy = missing,
                note = o["note"]?.jsonPrimitiveOrNull()?.contentOrNull?.checkLength("$where.note", errors),
            )
        }
        return out
    }

    /**
     * `leapMonth` phải là boolean JSON thật.
     *
     * Bản đầu so sánh chuỗi `== "true"`, nên `1`, `"yes"` hay `"True"` đều âm thầm
     * thành `false` — người sinh tháng nhuận khôi phục xong thành sinh tháng thường
     * mà không có lời cảnh báo nào. Giống hệt lỗi "policy lạ về mặc định" mà hợp đồng
     * đã cấm, chỉ khác chỗ xảy ra.
     */
    private fun decodeLeapMonth(l: JsonObject, where: String, errors: MutableList<BackupError>): Boolean {
        val raw = l["leapMonth"]
        if (raw == null || raw is kotlinx.serialization.json.JsonNull) return false
        val value = raw.jsonPrimitiveOrNull()
            ?.takeIf { !it.isString }
            ?.content
            ?.toBooleanStrictOrNull()
        if (value == null) {
            errors += BackupError.WrongType("$where.lunarBirthDate.leapMonth", "boolean")
            return false
        }
        return value
    }

    private fun String.checkLength(where: String, errors: MutableList<BackupError>): String {
        if (length > BackupFormat.MAX_TEXT_LENGTH) {
            errors += BackupError.TooLong(where, BackupFormat.MAX_TEXT_LENGTH)
        }
        return this
    }

    /** `null` thay vì ném, cho cả trường hợp phần tử là object/array chứ không phải giá trị. */
    private fun kotlinx.serialization.json.JsonElement.jsonPrimitiveOrNull(): JsonPrimitive? =
        runCatching { jsonPrimitive }.getOrNull()?.takeIf { it !is kotlinx.serialization.json.JsonNull }
}
