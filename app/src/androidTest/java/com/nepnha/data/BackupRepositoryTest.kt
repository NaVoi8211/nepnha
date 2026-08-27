package com.nepnha.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.data.db.NepNhaDatabase
import com.nepnha.data.prefs.SettingsRepository
import com.nepnha.data.repository.BackupRepository
import com.nepnha.data.repository.FamilyRepository
import com.nepnha.data.repository.MemberRepository
import com.nepnha.data.repository.MemorialRepository
import com.nepnha.domain.backup.BackupCodec
import com.nepnha.domain.backup.BackupData
import com.nepnha.domain.backup.BackupLunarBirth
import com.nepnha.domain.backup.BackupMember
import com.nepnha.domain.backup.BackupMemorial
import com.nepnha.domain.backup.BackupResult
import com.nepnha.domain.event.LeapMonthPolicy
import com.nepnha.domain.event.MemorialRule
import com.nepnha.domain.event.MissingDayPolicy
import com.nepnha.domain.model.FamilyMemberDraft
import com.nepnha.domain.model.Gender
import com.nepnha.domain.model.LunarBirthDate
import com.nepnha.domain.model.MemorialDraft
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Xuất/nhập đi qua **database thật**.
 *
 * Dùng file trên đĩa chứ không in-memory ở phần nào cần chứng minh việc lưu trữ, và
 * dùng `Dispatchers` thật để giao dịch Room hành xử đúng như trong app.
 */
@RunWith(AndroidJUnit4::class)
class BackupRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val files = mutableListOf<File>()

    private class Env(val db: NepNhaDatabase, val backup: BackupRepository, val settings: SettingsRepository)

    private fun newEnv(tag: String): Env {
        val dbFile = context.getDatabasePath("backup_${tag}_${System.nanoTime()}.db")
        val prefs = File(context.cacheDir, "backup_${tag}_${System.nanoTime()}.preferences_pb")
        files += dbFile; files += prefs
        val db = Room.databaseBuilder(context, NepNhaDatabase::class.java, dbFile.absolutePath)
            .addMigrations(NepNhaDatabase.MIGRATION_1_2, NepNhaDatabase.MIGRATION_2_3)
            .build()
        val store: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            produceFile = { prefs },
        )
        val settings = SettingsRepository(store)
        val family = FamilyRepository(db.familyDao())
        return Env(db, BackupRepository(db, family, settings), settings)
    }

    @After
    fun tearDown() {
        files.forEach { it.delete(); File("${it.path}-wal").delete(); File("${it.path}-shm").delete() }
    }

    /** Dựng một gia đình có đủ các dạng dữ liệu đáng quan tâm. */
    private fun seed(env: Env): Long = runBlocking {
        FamilyRepository(env.db.familyDao()).ensureDefaultFamily("Nhà họ Nguyễn")
        val familyId = env.db.familyDao().firstId()!!
        val members = MemberRepository(env.db.memberDao(), env.settings)
        val a = members.add(
            familyId,
            FamilyMemberDraft(
                fullName = "Nguyễn Văn A", gender = Gender.MALE,
                solarBirthDate = LocalDate.of(1950, 3, 14),
                lunarBirthDate = LunarBirthDate(26, 1, 1950, false),
                role = "Trưởng nam", note = null,
            ),
        )
        members.add(
            familyId,
            FamilyMemberDraft(
                fullName = "Trần Thị B", gender = Gender.FEMALE,
                solarBirthDate = null, lunarBirthDate = null, role = null, note = "ghi chú",
            ),
        )
        val memorials = MemorialRepository(env.db.memorialDao())
        memorials.add(
            familyId,
            MemorialDraft(
                "Cụ ông", 30, 7,
                MemorialRule(LeapMonthPolicy.LEAP_MONTH_ONLY, MissingDayPolicy.SKIP),
                "giỗ lớn", memberId = a,
            ),
        )
        memorials.add(
            familyId,
            MemorialDraft("Cụ tổ không liên kết", 5, 5, MemorialRule(), null),
        )
        env.settings.setPrimaryMemberId(a)
        familyId
    }

    // ------------------------------------------------------------- vòng tròn

    /**
     * DB → xuất → phân tích → nhập vào DB sạch → **tương đương về mặt nghĩa**.
     *
     * ID không cần giống (nhập luôn tạo id mới); **quan hệ** phải giống.
     *
     * Sai thì: bản sao lưu không khôi phục lại được đúng gia đình của người dùng —
     * toàn bộ tính năng vô nghĩa.
     */
    @Test
    fun vong_tron_xuat_nhap_giu_nguyen_y_nghia() {
        val source = newEnv("src")
        seed(source)
        val exported = runBlocking { source.backup.readAll() }
        val text = BackupCodec.encode(exported, "2026-08-27T10:00:00Z", "test")
        source.db.close()

        // Phân tích lại đúng như importer thật sẽ làm.
        val parsed = (BackupCodec.decode(text) as BackupResult.Valid).file.data

        val target = newEnv("dst")
        val outcome = runBlocking { target.backup.importAdditive(parsed) }
        assertEquals(2, outcome.membersAdded)
        assertEquals(2, outcome.memorialsAdded)

        val reread = runBlocking { target.backup.readAll() }
        // So sánh nghĩa: ref được đánh lại theo thứ tự nên hai bên phải khớp hoàn toàn.
        assertEquals(exported, reread)
        target.db.close()
    }

    /**
     * Quan hệ ngày giỗ → thành viên phải trỏ đúng người sau khi nhập, dù id đã đổi.
     *
     * Sai thì: sau khi khôi phục, ngày giỗ gắn nhầm sang người khác — lỗi im lặng và
     * rất khó phát hiện.
     */
    @Test
    fun quan_he_ngay_gio_toi_thanh_vien_duoc_anh_xa_dung_id_moi() {
        val source = newEnv("src2")
        seed(source)
        val exported = runBlocking { source.backup.readAll() }
        source.db.close()

        val target = newEnv("dst2")
        // Có sẵn một thành viên khác để id chắc chắn lệch so với bên nguồn.
        runBlocking {
            FamilyRepository(target.db.familyDao()).ensureDefaultFamily("Nhà cũ")
            val fid = target.db.familyDao().firstId()!!
            MemberRepository(target.db.memberDao(), target.settings).add(
                fid,
                FamilyMemberDraft("Người có sẵn", Gender.UNSPECIFIED, null, null, null, null),
            )
            target.backup.importAdditive(exported)
        }

        val members = runBlocking { target.db.memberDao().observeByFamily(1).first() }
        val memorials = runBlocking { target.db.memorialDao().observeByFamily(1).first() }
        val a = members.single { it.fullName == "Nguyễn Văn A" }
        val linked = memorials.single { it.name == "Cụ ông" }
        val unlinked = memorials.single { it.name == "Cụ tổ không liên kết" }

        assertEquals("phải trỏ tới ĐÚNG người, với id mới", a.id, linked.memberId)
        assertNotEquals("id phải khác bên nguồn để test có ý nghĩa", 1L, a.id)
        assertNull("ngày giỗ không liên kết phải vẫn không liên kết", unlinked.memberId)
        target.db.close()
    }

    /**
     * Policy không mặc định phải sống sót nguyên vẹn qua xuất/nhập.
     *
     * Sai thì: người dùng chọn "chỉ tháng nhuận" rồi khôi phục và lựa chọn về mặc định
     * mà không có thông báo nào.
     */
    @Test
    fun policy_khong_mac_dinh_song_sot_qua_xuat_nhap() {
        val source = newEnv("src3")
        seed(source)
        val exported = runBlocking { source.backup.readAll() }
        source.db.close()

        val target = newEnv("dst3")
        runBlocking { target.backup.importAdditive(exported) }
        val m = runBlocking { MemorialRepository(target.db.memorialDao()).observe(1).first() }
            .single { it.name == "Cụ ông" }
        assertEquals(LeapMonthPolicy.LEAP_MONTH_ONLY, m.rule.leapMonthPolicy)
        assertEquals(MissingDayPolicy.SKIP, m.rule.missingDayPolicy)
        assertEquals(30, m.lunarDay)
        target.db.close()
    }

    // ------------------------------------------------------------- chỉ thêm

    /**
     * Nhập vào máy đã có dữ liệu chỉ được **thêm**, không xoá, không ghi đè.
     *
     * Sai thì: người dùng khôi phục một bản cũ và mất những gì đã nhập gần đây.
     */
    @Test
    fun nhap_vao_may_da_co_du_lieu_chi_them_khong_xoa() {
        val source = newEnv("src4")
        seed(source)
        val exported = runBlocking { source.backup.readAll() }
        source.db.close()

        val target = newEnv("dst4")
        seed(target)
        val before = runBlocking { target.backup.readAll() }
        assertEquals(2, before.members.size)

        runBlocking { target.backup.importAdditive(exported) }
        val after = runBlocking { target.backup.readAll() }
        assertEquals("phải là 2 cũ + 2 mới", 4, after.members.size)
        assertEquals(4, after.memorials.size)
        // Hai người trùng tên là HAI người — không gộp.
        assertEquals(2, after.members.count { it.fullName == "Nguyễn Văn A" })
        target.db.close()
    }

    /**
     * Tín chủ chỉ được áp dụng khi máy chưa chọn ai.
     *
     * Sai thì: nhập dữ liệu âm thầm đổi người đứng khấn mà người dùng đã tự tay chọn.
     */
    @Test
    fun tin_chu_khong_bi_ghi_de_khi_may_da_chon() {
        val source = newEnv("src5")
        seed(source)
        val exported = runBlocking { source.backup.readAll() }
        assertNotNull("file phải có tín chủ để test có ý nghĩa", exported.primaryMemberRef)
        source.db.close()

        val target = newEnv("dst5")
        seed(target)
        val existing = runBlocking { target.settings.primaryMemberId.first() }
        val outcome = runBlocking { target.backup.importAdditive(exported) }
        assertEquals(false, outcome.primaryMemberApplied)
        assertEquals(existing, runBlocking { target.settings.primaryMemberId.first() })
        target.db.close()

        val fresh = newEnv("dst6")
        runBlocking { FamilyRepository(fresh.db.familyDao()).ensureDefaultFamily("Nhà mới") }
        val outcome2 = runBlocking { fresh.backup.importAdditive(exported) }
        assertEquals("máy chưa chọn ai thì phải áp dụng", true, outcome2.primaryMemberApplied)
        assertNotNull(runBlocking { fresh.settings.primaryMemberId.first() })
        fresh.db.close()
    }

    // ------------------------------------------------------------- nguyên tử

    /**
     * Giao dịch hỏng giữa chừng ⇒ **rollback toàn bộ**, database không đổi một dòng.
     *
     * Dựng lỗi bằng một `memberRef` không có trong bảng ánh xạ — thứ mà bước kiểm tra
     * đã chặn, nên ở đây là bảo hiểm tầng cuối.
     *
     * Sai thì: nhập nửa chừng để lại một gia đình lẫn lộn không cách nào dọn.
     */
    @Test
    fun giao_dich_hong_thi_rollback_toan_bo() {
        val env = newEnv("atomic")
        seed(env)
        val before = runBlocking { env.backup.readAll() }

        val broken = BackupData(
            familyName = "Nhà lỗi",
            primaryMemberRef = null,
            members = listOf(
                BackupMember(1, "Người hợp lệ", Gender.MALE, null, null, null, null),
            ),
            memorials = listOf(
                BackupMemorial(
                    "Ngày giỗ hợp lệ", 1, 1, 1,
                    LeapMonthPolicy.COMMON_MONTH_DEFAULT, MissingDayPolicy.LAST_VALID_DAY_OF_MONTH, null,
                ),
                // ref 999 không nằm trong danh sách thành viên ⇒ ném giữa giao dịch.
                BackupMemorial(
                    "Ngày giỗ hỏng", 999, 1, 1,
                    LeapMonthPolicy.COMMON_MONTH_DEFAULT, MissingDayPolicy.LAST_VALID_DAY_OF_MONTH, null,
                ),
            ),
        )

        val threw = runCatching { runBlocking { env.backup.importAdditive(broken) } }.isFailure
        assertTrue("phải ném để rollback", threw)

        val after = runBlocking { env.backup.readAll() }
        assertEquals("database phải y nguyên như trước", before, after)
        env.db.close()
    }

    /**
     * Xuất **không được** làm thay đổi database.
     *
     * Sai thì: chỉ việc sao lưu cũng có thể làm hỏng dữ liệu gốc.
     */
    @Test
    fun xuat_du_lieu_khong_lam_doi_database() {
        val env = newEnv("readonly")
        seed(env)
        val before = runBlocking { env.backup.readAll() }
        repeat(3) { runBlocking { env.backup.readAll() } }
        val after = runBlocking { env.backup.readAll() }
        assertEquals(before, after)
        env.db.close()
    }

    /**
     * File xuất ra phải **đọc lại được bằng chính importer**.
     *
     * Sai thì: app tạo ra file mà chính nó không nhập được.
     */
    @Test
    fun file_xuat_ra_luon_doc_lai_duoc() {
        val env = newEnv("selfread")
        seed(env)
        val text = BackupCodec.encode(runBlocking { env.backup.readAll() }, "2026-01-01T00:00:00Z", "t")
        assertTrue("phải phân tích lại được", BackupCodec.decode(text) is BackupResult.Valid)
        env.db.close()
    }

    // ------------------------------------------------------ Phase 7.5: máy hoàn toàn trống

    /**
     * Kịch bản thật của tính năng này: **máy mới, database rỗng hoàn toàn**.
     *
     * Đây là lần duy nhất mọi thứ trong file phải được áp dụng: tên gia đình, tín chủ,
     * quan hệ, policy, dấu tiếng Việt, ngày sinh âm. Các test khác đều nhập vào máy đã
     * có sẵn dữ liệu nên không chứng minh được nhánh này.
     *
     * Sai thì: người mất điện thoại cầm file sao lưu trong tay mà không lấy lại được gia
     * đình của mình — toàn bộ Phase 7 vô nghĩa.
     */
    @Test
    fun nhap_vao_database_hoan_toan_trong_tai_hien_du_moi_thu() {
        val source = newEnv("empty_src")
        seed(source)
        val exported = runBlocking { source.backup.readAll() }
        val text = BackupCodec.encode(exported, "2026-08-27T10:00:00Z", "test")
        source.db.close()

        val target = newEnv("empty_dst")
        // Chứng minh là thật sự rỗng trước khi nhập.
        assertNull("chưa được có gia đình nào", runBlocking { target.db.familyDao().firstId() })
        assertNull("chưa được có tín chủ", runBlocking { target.settings.primaryMemberId.first() })

        val parsed = (BackupCodec.decode(text) as BackupResult.Valid).file.data
        val outcome = runBlocking { target.backup.importAdditive(parsed) }

        assertEquals(2, outcome.membersAdded)
        assertEquals(2, outcome.memorialsAdded)
        assertTrue("máy trống thì tín chủ trong file PHẢI được áp dụng", outcome.primaryMemberApplied)

        val familyId = runBlocking { target.db.familyDao().firstId() }!!
        val family = runBlocking { FamilyRepository(target.db.familyDao()).observeFamily().first() }!!
        assertEquals("máy trống thì tên gia đình lấy theo file", "Nhà họ Nguyễn", family.name)

        val members = runBlocking { target.db.memberDao().observeByFamily(familyId).first() }
        val a = members.single { it.fullName == "Nguyễn Văn A" }
        val b = members.single { it.fullName == "Trần Thị B" }

        // Unicode tiếng Việt nguyên vẹn tới từng dấu.
        assertEquals("Nguyễn Văn A", a.fullName)
        assertEquals("Trưởng nam", a.role)
        assertEquals("ghi chú", b.note)

        // Ngày sinh dương và âm.
        assertEquals("1950-03-14", a.solarBirthDate.toString())
        assertEquals(26, a.lunarBirthDay)
        assertEquals(1, a.lunarBirthMonth)
        assertEquals(1950, a.lunarBirthYear)
        assertEquals(false, a.lunarBirthIsLeapMonth)
        assertNull("người không có ngày sinh âm phải vẫn không có", b.lunarBirthDay)

        // Quan hệ và policy.
        val memorials = runBlocking { MemorialRepository(target.db.memorialDao()).observe(familyId).first() }
        val linked = memorials.single { it.name == "Cụ ông" }
        assertEquals(a.id, linked.memberId)
        assertEquals(LeapMonthPolicy.LEAP_MONTH_ONLY, linked.rule.leapMonthPolicy)
        assertEquals(MissingDayPolicy.SKIP, linked.rule.missingDayPolicy)
        assertNull(memorials.single { it.name == "Cụ tổ không liên kết" }.memberId)

        // Tín chủ trỏ đúng người, với id mới.
        assertEquals(a.id, runBlocking { target.settings.primaryMemberId.first() })
        target.db.close()
    }

    // ------------------------------------------------------------ Phase 7.5: atomicity lớn

    /**
     * Rollback trên bộ dữ liệu **lớn**, lỗi rơi vào giữa chừng.
     *
     * Test rollback cũ chỉ có 2 bản ghi, nên không phân biệt được "giao dịch thật" với
     * "may mắn vì quá ngắn". Ở đây 60 thành viên và 120 ngày giỗ đã ghi xong thì bản ghi
     * thứ 100 mới hỏng — nếu Room không gói tất cả trong một giao dịch, database sẽ còn
     * lại đúng cái đống nửa vời đó.
     *
     * Sai thì: nhập hỏng để lại một gia đình lẫn lộn mà không có cách nào dọn ngoài xoá
     * sạch dữ liệu.
     */
    @Test
    fun rollback_toan_bo_tren_bo_du_lieu_lon() {
        val env = newEnv("atomic_big")
        seed(env)
        val before = runBlocking { env.backup.readAll() }
        val familyId = runBlocking { env.db.familyDao().firstId() }!!
        val membersBefore = runBlocking { env.db.memberDao().observeByFamily(familyId).first() }.size
        val memorialsBefore = runBlocking { env.db.memorialDao().observeByFamily(familyId).first() }.size

        val broken = BackupData(
            familyName = "Nhà lớn hỏng",
            primaryMemberRef = null,
            members = (1..60).map {
                BackupMember(it, "Thành viên $it", Gender.UNSPECIFIED, null, null, null, null)
            },
            memorials = (1..120).map {
                BackupMemorial(
                    "Giỗ $it",
                    // Bản ghi thứ 100 trỏ tới ref không tồn tại ⇒ ném khi đã ghi 99 cái trước.
                    if (it == 100) 9999 else ((it % 60) + 1),
                    (it % 30) + 1, (it % 12) + 1,
                    LeapMonthPolicy.COMMON_MONTH_DEFAULT, MissingDayPolicy.LAST_VALID_DAY_OF_MONTH, null,
                )
            },
        )

        assertTrue(
            "phải ném để rollback",
            runCatching { runBlocking { env.backup.importAdditive(broken) } }.isFailure,
        )

        val familyIdAfter = runBlocking { env.db.familyDao().firstId() }!!
        val membersAfter = runBlocking { env.db.memberDao().observeByFamily(familyIdAfter).first() }.size
        val memorialsAfter = runBlocking { env.db.memorialDao().observeByFamily(familyIdAfter).first() }.size
        assertEquals("không được sót thành viên nào của lần nhập hỏng", membersBefore, membersAfter)
        assertEquals("không được sót ngày giỗ nào của lần nhập hỏng", memorialsBefore, memorialsAfter)
        assertEquals("dữ liệu phải y nguyên từng trường", before, runBlocking { env.backup.readAll() })
        env.db.close()
    }

    // ------------------------------------------------------------- Phase 7.5: bộ dữ liệu lớn

    /**
     * Bộ dữ liệu lớn phải nhập được, đúng, và trong thời gian chấp nhận được.
     *
     * Đo trên thiết bị thật rồi in ra logcat — không tối ưu gì cho tới khi có số.
     *
     * Sai thì: gia đình đông người khôi phục xong thấy thiếu bản ghi, hoặc app đứng
     * hình đủ lâu để hệ thống báo ANR.
     */
    @Test
    fun bo_du_lieu_lon_nhap_dung_va_do_thoi_gian() {
        for ((members, memorials) in listOf(50 to 100, 200 to 500)) {
            val data = BackupData(
                familyName = "Nhà rất đông",
                primaryMemberRef = 1,
                members = (1..members).map {
                    BackupMember(
                        it, "Nguyễn Văn Số $it", if (it % 2 == 0) Gender.FEMALE else Gender.MALE,
                        "19%02d-01-01".format(it % 100),
                        BackupLunarBirth((it % 30) + 1, (it % 12) + 1, 1900 + (it % 100), it % 7 == 0),
                        "vai trò $it", "ghi chú $it",
                    )
                },
                memorials = (1..memorials).map {
                    BackupMemorial(
                        "Giỗ cụ $it", (it % members) + 1, (it % 30) + 1, (it % 12) + 1,
                        LeapMonthPolicy.entries[it % LeapMonthPolicy.entries.size],
                        MissingDayPolicy.entries[it % MissingDayPolicy.entries.size],
                        "ghi chú giỗ $it",
                    )
                },
            )

            // Đi qua đúng đường thật: mã hoá ra văn bản, phân tích lại, rồi mới nhập.
            val encodeMs = measure { BackupCodec.encode(data, "2026-01-01T00:00:00Z", "t") }
            val text = BackupCodec.encode(data, "2026-01-01T00:00:00Z", "t")
            val decodeMs = measure { BackupCodec.decode(text) }
            val parsed = (BackupCodec.decode(text) as BackupResult.Valid).file.data
            assertEquals("phân tích lại phải giống hệt", data, parsed)

            // `importAdditive` là CHỈ-THÊM: đo bằng cách lặp trên cùng một database sẽ
            // cộng dồn 7 lần dữ liệu. Mỗi lần đo phải có database sạch riêng.
            val importSamples = (1..5).map {
                val fresh = newEnv("big_${members}_${memorials}_run$it")
                // Room dựng database ở truy vấn ĐẦU TIÊN. Không chạm trước thì phép đo
                // gộp cả thời gian tạo schema vào thời gian nhập và cho ra con số sai.
                runBlocking { fresh.db.familyDao().firstId() }
                val t0 = System.nanoTime()
                runBlocking { fresh.backup.importAdditive(parsed) }
                val ms = (System.nanoTime() - t0) / 1_000_000
                fresh.db.close()
                ms
            }
            val importMs = importSamples.sorted()[2]

            // Env cuối cùng dùng để kiểm đúng/sai, nhập đúng MỘT lần.
            val env = newEnv("big_${members}_$memorials")
            runBlocking { env.backup.importAdditive(parsed) }
            val exportMs = measure { runBlocking { env.backup.readAll() } }

            val familyId = runBlocking { env.db.familyDao().firstId() }!!
            assertEquals(
                members,
                runBlocking { env.db.memberDao().observeByFamily(familyId).first() }.size,
            )
            assertEquals(
                memorials,
                runBlocking { env.db.memorialDao().observeByFamily(familyId).first() }.size,
            )
            // Khớp về NGHĨA, kể cả policy và ngày sinh âm. Thành viên giữ nguyên thứ tự
            // chèn; ngày giỗ thì không — DAO sắp lại theo `lunarMonth, lunarDay`, nên so
            // sánh theo tập hợp chứ không theo thứ tự.
            val reread = runBlocking { env.backup.readAll() }
            assertEquals(data.familyName, reread.familyName)
            assertEquals(data.primaryMemberRef, reread.primaryMemberRef)
            assertEquals(data.members, reread.members)
            assertEquals(data.memorials.toSet(), reread.memorials.toSet())
            assertEquals("không được trùng lặp hay thiếu", data.memorials.size, reread.memorials.size)

            android.util.Log.i(
                "NepNhaPerf",
                "backup $members thành viên / $memorials ngày giỗ | " +
                    "kích thước ${text.length / 1024} KB | encode ${encodeMs}ms | " +
                    "decode ${decodeMs}ms | import trung vị ${importMs}ms " +
                    "(xấu nhất ${importSamples.max()}ms) | export ${exportMs}ms",
            )
            env.db.close()
        }
    }

    /** Trung vị của 5 lần đo — một lần chạy không phải là con số. */
    private fun measure(block: () -> Unit): Long {
        repeat(2) { block() } // làm nóng
        val samples = (1..5).map {
            val t0 = System.nanoTime()
            block()
            (System.nanoTime() - t0) / 1_000_000
        }
        return samples.sorted()[2]
    }
}
