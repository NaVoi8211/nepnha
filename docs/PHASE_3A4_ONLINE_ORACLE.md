# Phase 3A.4 — Online oracle strategy

> **KHÔNG CÓ PRODUCTION CODE.** `app/` không bị đụng · không Kotlin · không Room ·
> không UI · không dependency · không network trong app.
>
> Nhãn: **FACT** · **INFERENCE** · **HYPOTHESIS** · **UNVERIFIED/UNKNOWN**.

**Kết luận ngắn: KHÔNG bỏ được yêu cầu sách Trần Tiến Bình.** Các lịch trực tuyến
nâng chất lượng đối chiếu lên đáng kể nhưng **không thay thế được oracle Tier 1**, vì
lý do đơn giản: chúng có thể đều là hậu duệ của cùng một implementation.

---

## 1–4. lichviet.app — kết luận

Chi tiết đầy đủ: [LUNAR_ONLINE_ORACLE_PROVENANCE.md §1](LUNAR_ONLINE_ORACLE_PROVENANCE.md).

**FACT.** Vận hành bởi PPCLink / Công ty CP Phát triển Lịch Việt. Không công bố thuật
toán, không công bố source, không có API công khai. Có URL theo từng ngày
`/lich-van-nien/D-M-YYYY`, **render phía server**, phủ đủ **1901–2100**.

**FACT.** `robots.txt` **Allow** tường minh cho `ClaudeBot` và `anthropic-ai`.

**FACT.** Điều khoản mục 3 tuyên bố sở hữu cả **"dữ liệu"** và cấm *"sao chép, tái
tạo, phân phối"* nếu không có **văn bản cho phép**.

**FACT — ancestry UNKNOWN, và biết chính xác vì sao:** lịch được tính **phía server**
nên logic không nằm trong bundle JS ⇒ không có cách nào xác định provenance từ phần
công khai. Không suy đoán.

**INFERENCE.** Đây **là** lịch Việt Nam (105°Đ): ca 1985 lệch đúng một tháng so với
HKO theo đúng chiều đã tài liệu hoá. **Nhưng** điều đó chỉ phân biệt *lịch VN hay lịch
TQ*, **không** phân biệt *độc lập hay là HND*.

---

## 5–6. Ba câu hỏi phải tách riêng

### 5.1 LEGAL — được lấy kết quả truy vấn để kiểm thử nội bộ không?

| Hành vi | Kết luận |
|---|---|
| Truy vấn thủ công, số lượng nhỏ, xem bằng mắt | ✅ Sử dụng bình thường một website công khai; robots.txt cho phép |
| Tải hàng loạt để dựng dataset | ⛔ **Không** — điều khoản cấm *"sao chép, tái tạo"* |
| Commit kết quả vào repository | ⛔ **Không** — repository là **phân phối** |
| Đóng gói vào APK | ⛔ **Tuyệt đối không** |

> ⚠️ **LEGAL UNCERTAINTY.** Một cặp ngày dương↔âm là *sự kiện*; sự kiện thường không
> được bảo hộ quyền tác giả. Nhưng điều khoản của họ tuyên bố sở hữu "dữ liệu". Tôi
> **không kết luận pháp lý** và chọn cách thận trọng nhất.

### 5.2 TECHNICAL — có đủ tin cậy làm test oracle không?

**Không, ở mức Tier 1.** Ancestry UNKNOWN ⇒ có thể cùng nhánh HND với hàng chục
website khác. Giá trị thật của nó là một loại xác nhận **khác**, xem §7.

### 5.3 ENGINEERING — có nên hardcode vào production không?

**Không.** Cả vì lý do pháp lý (§5.1) lẫn kỹ thuật (§5.2). Dataset của Nếp Nhà phải
sinh từ nguồn thiên văn có giấy phép rõ, không phải chép từ website.

---

## 7. Composite oracle — 5 mức xác nhận

Thay một oracle duy nhất bằng **ma trận đối chiếu có phân hạng**. Điểm mấu chốt:
**không gọi "verified" chỉ vì nhiều website đồng ý.**

| Mức | Tên | Nguồn | Chứng minh được gì |
|---|---|---|---|
| **V1** | **Independent Tier 1 confirmation** | Trần Tiến Bình · văn bản nhà nước | **Lịch có đúng lịch chính thức Việt Nam không** |
| **V2** | **Independent astronomical confirmation** | NASA · HKO · ERFA | Thiên văn đúng — **không** chứng minh quy tắc lịch VN đúng |
| **V3** | **Vietnamese-calendar implementation confirmation** | lichviet.app và các lịch VN khác | Kết quả **trùng với thứ người Việt đang thấy hằng ngày**. Không phải bằng chứng độc lập |
| **V4** | **Online black-box confirmation** | Nguồn ancestry UNKNOWN | Chỉ để **phát hiện sai lệch**, không bao giờ là bằng chứng |
| **V5** | **Unresolved** | Các nguồn mâu thuẫn hoặc không đủ | Phải điều tra, **không chọn bên** |

**INFERENCE — V3 có giá trị sản phẩm thật, dù không phải bằng chứng khoa học.** Nếu
Nếp Nhà hiện ngày âm khác với mọi lịch mà người dùng đang dùng, thì kể cả ta đúng về
thiên văn, sản phẩm vẫn **sai về mặt sử dụng**. V3 là tín hiệu cảnh báo, không phải
oracle.

**Quy tắc tối hậu:** V3/V4 đồng ý **không bao giờ** nâng lên "verified". Chỉ V1 mới
đóng được G5/G6/G7/G10.

---

## 8. Điều gì đã được chứng minh trong phase này

| # | Kết luận | Nhãn |
|---|---|---|
| 1 | lichviet.app dùng lịch Việt Nam (105°Đ), không phải lịch Trung Quốc | **INFERENCE** từ FACT ca 1985 |
| 2 | lichviet.app phủ đủ 1901–2100 | **FACT** |
| 3 | Ancestry của lichviet.app **không xác định được**, vì tính phía server | **FACT** |
| 4 | Điều khoản cấm sao chép/phân phối **dữ liệu** | **FACT** |
| 5 | amlich.app **tự công bố** dựa trên HND ⇒ nhánh D | **FACT** |
| 6 | Không có bảng lịch 1901–2100 trực tuyến nào của cơ quan nhà nước VN | **FACT** (tìm không thấy) |
| 7 | lichviet.app chỉ có **một** lịch, theo miền Bắc/chính thức (Tết Mậu Thân = 29/01/1968) | **FACT** |
| 8 | Ca 1967-07-07: lichviet.app cho Sóc = 08/07, **khác** ERFA của ta (07/07) | **FACT** |

### ⚠️ Phát hiện đáng chú ý nhất: mục 8

Một trong **5 ca fake-precision** nay có thêm dữ liệu:

| Nguồn | Ngày Sóc | Nhánh |
|---|---|---|
| NASA (17:00 UT làm tròn) | 08/07/1967 | C |
| **ERFA của ta** (16:59:41 UT) | **07/07/1967** | tính toán riêng |
| lichviet.app | 08/07/1967 | **UNKNOWN** |

**INFERENCE.** Hai trên ba nói 08/07 — nhưng **không được đếm phiếu**: NASA và
lichviet.app **có thể cùng nhánh** (NASA dựa trên Meeus; lichviet.app UNKNOWN, có thể
là HND, mà HND cũng dựa trên cùng lớp thuật toán). Đây đúng cái bẫy dự án đã gặp ba
lần.

**Giá trị thật:** đây là **manh mối cực rẻ để hỏi sách** — chỉ cần tra **một** ngày
trong sách Trần Tiến Bình là biết ai đúng. Xem §16.

---

## 9. Test matrix — nguyên tắc điền

Theo mục E, ma trận cần 19 nhóm (E1–E19). Trạng thái nguồn cho từng nhóm:

| Nhóm | Có V1? | Có V2? | Có V3? | Kết luận |
|---|---|---|---|---|
| E1 Tết 2020–2031 | 🟡 chỉ 2025, 2026 (văn bản nhà nước) | — | ✅ | Thiếu V1 cho 10 năm |
| E2 VN≠TQ (8 năm) | ❌ | ✅ HKO cho phía TQ | ✅ | **1985 đã có V1(VN)+V2(TQ)** |
| E3 Tháng nhuận | ❌ | ❌ | ✅ | ⛔ **G6 BLOCKED** |
| E4/E5 Tháng 29/30 ngày | ❌ | ❌ | ✅ | ⛔ **G7 BLOCKED** |
| E6–E9 Rằm/Trung Thu/Giao thừa | ❌ | ❌ | ✅ | ⛔ BLOCKED |
| E10 Biên 1901/2100 | ❌ | ❌ | ✅ | ⛔ **G10 BLOCKED** |
| E11 Sóc sát biên | ❌ | ✅ (mâu thuẫn) | ✅ | ⛔ **G15 UNRESOLVED** |
| E12 Trung khí | — | ✅ HKO 3 năm + ERFA | — | 🟡 |
| E13/E14 1954–1967, Bắc/Nam | ❌ | ❌ | ❌ (chỉ có một lịch) | ⛔ **G9 BLOCKED** |
| E15–E19 hai chiều, nhuận, can chi | — | — | ✅ | Thiếu V1 |

**Không tạo file test vector mới trong phase này**, vì mọi giá trị mới thu được đều
là V3/V4 và **không đủ tư cách làm expected value**. Đưa chúng vào
`LUNAR_TEST_VECTORS.md` sẽ vi phạm chính nguyên tắc dự án đã đặt.

---

## 10–11. Meeus / ERFA — trạng thái không đổi

Không có bằng chứng mới trong phase này. Kết luận của
[PHASE_3A_NEXT_GATE.md §A](PHASE_3A_NEXT_GATE.md) giữ nguyên:

- **FACT:** `moon98.c` là ephemeris Mặt Trăng **duy nhất** trong ERFA và là file
  **duy nhất** nhắc tới Meeus (quét 251 file).
- **FACT:** ERFA có phép relicense bằng văn bản của Chủ tịch IAU SOFA Board.
- **LEGAL UNCERTAINTY:** phép đó nói về quyền của SOFA, không nói về quyền của
  Willmann-Bell với thuật toán Meeus.
- **Chưa quyết:** chủ dự án chưa chọn cách hiểu policy A/B/C/D.

**Nhắc lại điều dễ bị bỏ sót:** nếu policy loại Meeus theo cách hiểu B/C thì **dữ
liệu NASA cũng bị loại** — NASA ghi rõ dựa trên Meeus. Không có phương án "bỏ moon98,
dùng NASA".

---

## 12. Historical timezone — không đổi

**G9 vẫn BLOCKED.** Toàn văn 121-CP vẫn 403. Bằng chứng mới duy nhất từ phase này là
tiêu cực: lichviet.app **chỉ có một lịch**, không mô hình hoá Bắc/Nam ⇒ không giúp gì
cho 1954–1967.

---

## 13. Gate matrix

| # | Gate | Trạng thái | Thay đổi ở 3A.4 |
|---|---|---|---|
| G1–G4, G11–G13 | | ✅ **PASS** | — |
| **G5** | Oracle độc lập | ⛔ **BLOCKED** | Không đổi — online là V3, không phải V1 |
| **G6** | Tháng nhuận | ⛔ **BLOCKED** | Không đổi |
| **G7** | 29/30 ngày | ⛔ **BLOCKED** | Không đổi |
| **G8** | VN ≠ TQ | 🟡 **PARTIAL** | **Củng cố**: lichviet.app xác nhận ca 1985 ở mức V3 |
| **G9** | Múi giờ lịch sử | ⛔ **BLOCKED** | Không đổi |
| **G10** | Biên 1901/2100 | ⛔ **BLOCKED** | Không đổi (URL chạy nhưng chỉ V3) |
| **G14** | Provenance thiên văn | 🟡 **PARTIAL** | Không đổi — chờ quyết định policy |
| **G15** | Độ chính xác | 🟡 **PARTIAL** | **Thêm dữ kiện** cho ca 1967-07-07, vẫn UNRESOLVED |
| **G16** | ΔT | ✅ **PASS** | — |

**Không gate nào được mở trong phase này.** Đúng như dự đoán: online calendars không
đủ tư cách mở gate Tier 1.

---

## 14. Blocker còn lại

| # | Blocker | Cách mở |
|---|---|---|
| **1** | **Policy Meeus** | Chủ dự án chọn cách hiểu A/B/C/D |
| **2** | **Oracle Tier 1** (G5/G6/G7/G10, 5 ca G15) | Sách Trần Tiến Bình |
| **3** | **G9** 1954–1967 | Toàn văn 121-CP · lịch in thời kỳ đó |

Không blocker nào giảm đi. Nhưng **chi phí mở blocker 2 đã giảm mạnh** — xem §16.

---

## 15. Việc cần chủ dự án quyết

1. **Policy Meeus** (A/B/C/D) — chặn nhánh Mặt Trăng.
2. **Có chấp nhận V3 làm tiêu chí sản phẩm không?** Câu hỏi mới nảy ra từ phase này:
   nếu Nếp Nhà tính đúng về thiên văn nhưng lệch với mọi lịch người Việt đang dùng,
   ta chọn *đúng* hay chọn *giống mọi người*? Đây là quyết định sản phẩm, không phải
   kỹ thuật.
3. **Sách** — vẫn là đường găng.

---

## 16. Việc rẻ nhất mà có giá trị nhất

**Không cần mua sách. Chỉ cần tra đúng vài dòng.**

Sách có ở nhiều thư viện (ví dụ Thư viện Quảng Ninh có trong danh mục). Chỉ cần mượn
tại chỗ hoặc nhờ người tra, và chép lại **rất ít** dữ liệu:

| Ưu tiên | Tra gì | Mở được gì |
|---|---|---|
| **1** | **Tháng 7/1967**: ngày dương nào là mùng 1 tháng 6 âm? | **Vừa G15 vừa G9** — nếu là 08/07 thì khớp NASA/lichviet; nếu 07/07 thì khớp ERFA. Và vì 1967 nằm trong vùng tranh chấp múi giờ, kết quả còn là manh mối cho P1/P2 |
| 2 | 4 tháng còn lại của G15: 6/1944, 5/2054, 11/2077, 10/2085 | Đóng G15 |
| 3 | Danh sách năm nhuận + tháng nhuận 1901–2100 | Đóng **G6** |
| 4 | Ngày đầu **mọi tháng âm** của ~6 năm chọn có chủ đích | Đóng **G7** (hiệu hai mốc = 29 hay 30) |
| 5 | Ngày đầu các tháng của 1901, 1902, 2099, 2100 | Đóng **G10** |
| 6 | Phần dẫn nhập: sách xử lý 1954–1967 thế nào | **G9** |

**Ưu tiên 1 là món hời nhất của cả dự án hiện nay:** một dòng tra cứu, mở được manh
mối cho hai gate đang bế tắc.
