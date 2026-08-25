# Provenance của các lịch Việt Nam trực tuyến

> Phase 3A.4, khảo sát 2026-08-25. Không có production code.
> Nhãn: **FACT** · **INFERENCE** · **HYPOTHESIS** · **UNVERIFIED/UNKNOWN**.

---

## 1. lichviet.app

| Mục | Kết quả | Nhãn |
|---|---|---|
| **A1** Là gì | Website + app "Lịch Việt – Lịch vạn niên". Ngoài lịch còn có tử vi, phong thuỷ, thần số học, xem ngày tốt, giải mã giấc mơ | FACT |
| **A2** Ai vận hành | Phát triển bởi **PPCLink**, sở hữu bởi **Công ty Cổ phần Phát triển Lịch Việt**. `© Copyright 2022-2026 Lịch Việt` | FACT |
| **A3** Công bố thuật toán? | **Không** | FACT |
| **A4** Công bố source? | **Không** | FACT |
| **A5** Repo/API công khai? | Không tìm thấy. Không có endpoint `/api/` trong HTML | FACT |
| **A6** Điều khoản | Có: `/dieu-khoan-su-dung`, `/chinh-sach-bao-mat` | FACT |
| **A7** API công khai | **Không** | FACT |
| **A8/A9** Truy vấn dương→âm | **Có**, qua URL `/lich-van-nien/D-M-YYYY`, **render phía server**. Chiều âm→dương: không có URL trực tiếp | FACT |
| **A10** Tháng nhuận | Chưa xác định trong khảo sát này | UNKNOWN |
| **A11** Tiết khí | Chưa xác định | UNKNOWN |
| **A12** Phủ 1901–2100 | **Có** — thử 01/01/1901 và 31/12/2100 đều trả kết quả | FACT |
| **A13/A14** Dấu vết HND/thư viện | **KHÔNG XÁC ĐỊNH ĐƯỢC** — xem §1.2 | **UNKNOWN** |

### 1.1 robots.txt — cho phép rõ ràng

**FACT.** `https://lichviet.app/robots.txt` liệt kê tường minh và **Allow** cho
`GPTBot`, `OAI-SearchBot`, `ChatGPT-User`, **`ClaudeBot`**, `anthropic-ai`,
`PerplexityBot` — ngoài `User-agent: * Allow: /`.

**INFERENCE.** Việc truy cập tự động không bị robots.txt cấm. Nhưng **robots.txt
không phải điều khoản sử dụng** — xem §1.3.

### 1.2 ⚠️ Không xác định được ancestry — và lý do rất cụ thể

**FACT.** Ngày âm xuất hiện **trong HTML tĩnh** sau khi đã gỡ toàn bộ thẻ `<script>`.
Tức là lịch được tính **phía server**, không phải trong JavaScript của trình duyệt.

**INFERENCE — hệ quả quan trọng.** Không thể xác định ancestry bằng cách xem bundle
JS phía client, vì logic lịch **không nằm ở đó**. Mọi kết luận kiểu "site này dùng
amlich.js" đều **không kiểm chứng được** với lichviet.app.

⇒ **Ancestry = UNKNOWN.** Không suy đoán. Không loại trừ khả năng nó là HND.

*(Ghi chú phương pháp: chỉ tìm dấu hiệu provenance ở phần công khai của trang. Không
sao chép code, không tái tạo implementation.)*

### 1.3 Điều khoản sử dụng — nguyên văn điều quyết định

**FACT.** Mục 3 *"Quyền Sở Hữu Ứng Dụng"*:

> "tất cả các quyền sở hữu trí tuệ liên quan đến Ứng Dụng (bao gồm nhưng không giới
> hạn mã nguồn, hình ảnh, **dữ liệu**, thông tin, nội dung chứa đựng trong Ứng
> Dụng…) sẽ thuộc quyền sở hữu duy nhất bởi Lịch Việt và **không cá nhân, tổ chức
> nào được phép sao chép, tái tạo, phân phối**, hoặc hình thức khác xâm phạm tới
> quyền của chủ sở hữu **nếu không có sự đồng ý và cho phép bằng văn bản** của Lịch
> Việt."

**FACT.** Điều khoản nêu đích danh **"dữ liệu"**, không chỉ mã nguồn.

**INFERENCE.** ⇒ **Không** được tải hàng loạt kết quả rồi đóng gói. **Không** được
commit dữ liệu của họ vào repository. **Không** được đưa vào APK.

> ⚠️ **LEGAL UNCERTAINTY.** Một cặp ngày dương↔âm là *sự kiện*, và sự kiện thường
> không thuộc đối tượng bảo hộ quyền tác giả ở nhiều hệ thống pháp luật. Nhưng điều
> khoản của họ tuyên bố sở hữu "dữ liệu". Tôi **không đưa ra kết luận pháp lý** và
> chọn cách xử lý **thận trọng nhất**.

### 1.4 Hành vi quan sát được

Truy vấn thủ công một tập nhỏ có chủ đích (không scraping, không lưu thành dataset).
**Ghi lại ở đây để phân tích provenance, KHÔNG dùng làm expected value trong test.**

| Ngày dương | lichviet.app trả về | Ý nghĩa |
|---|---|---|
| 21/01/1985 | **1/1** năm Ất Sửu | = Tết Việt Nam |
| 20/02/1985 | **1/2** năm Ất Sửu | HKO nói đây là **1/1** (Tết Trung Quốc) |
| 29/01/1968 | **1/1** năm Mậu Thân | = Tết miền **Bắc** |
| 30/01/1968 | **2/1** năm Mậu Thân | **Không** mô hình hoá lịch miền Nam |
| 07/07/1967 | **30/5** | |
| 08/07/1967 | **1/6** | Ngày Sóc = 08/07 |
| 01/01/1901 | 11/11 năm Canh Tý | Biên dưới hoạt động |
| 31/12/2100 | 1/12 năm Canh Thân | Biên trên hoạt động |

**INFERENCE 1 — đây LÀ lịch Việt Nam, không phải lịch Trung Quốc.** Ca 1985 lệch
đúng một tháng so với HKO theo đúng chiều đã được tài liệu hoá ⇒ dùng kinh tuyến
105°Đ.

**INFERENCE 2 — nhưng điều đó KHÔNG chứng minh độc lập với HND.** Thuật toán HND
cũng cho đúng kết quả Việt Nam. Phép thử 1985 phân biệt được *"lịch VN hay lịch TQ"*,
**không** phân biệt được *"độc lập hay là HND"*.

**INFERENCE 3 — chỉ một lịch duy nhất.** 29/01/1968 = mùng 1 ⇒ theo lịch **chính
thức/miền Bắc**. Không có khái niệm Bắc/Nam. Không giải được G9.

**INFERENCE 4 — ca 1967-07-07 rất đáng chú ý.** lichviet.app cho ngày Sóc = **08/07**,
khớp với cách hiểu NASA (17:00 UT làm tròn ⇒ 00:00 giờ VN ngày 08/07), **khác** với
tính toán ERFA của ta (16:59:41 UT ⇒ 23:59 ngày 07/07). Đây là một dữ kiện thật,
nhưng do ancestry UNKNOWN nên **không phải xác nhận độc lập**.

---

## 2. Các nguồn trực tuyến khác

| Nguồn | Ancestry | Nhãn | Tier |
|---|---|---|---|
| **amlich.app** | **Tự công bố** dựa trên thuật toán Hồ Ngọc Đức (tác giả Trần Trọng Thanh) | FACT | **4** |
| lichngaytot.com | Không công bố | UNKNOWN | 3 |
| licham.vn | Không công bố | UNKNOWN | 3 |
| Các web lịch vạn niên khác | Không công bố | UNKNOWN | 3 |
| **HKO** (Hồng Kông) | Tự tính, số liệu HM Nautical Almanac Office | FACT | **1** *(nhưng là lịch **Trung Quốc**)* |
| **NASA/GSFC** | Espenak, dựa trên Meeus | FACT | **1** *(dữ liệu thiên văn, không phải lịch VN)* |
| **Văn bản nhà nước VN** | Quyết định hành chính | FACT | **1** *(chỉ quanh Tết)* |
| **Trần Tiến Bình** (sách in) | Ban Lịch Nhà nước | FACT | **1** *(chưa có trong tay)* |

**FACT.** Không tìm được **bảng lịch âm dương 1901–2100 trực tuyến** nào của Ban Lịch
Nhà nước hay cơ quan nhà nước Việt Nam. Sách vẫn là đường duy nhất cho oracle Tier 1
đầy đủ.

---

## 3. Independence graph

```
NHÁNH A — Nhà nước Việt Nam            NHÁNH B — HKO           NHÁNH C — NASA/GSFC
  Ban Lịch Nhà nước                      tự tính                 Espenak
   ├─ Trần Tiến Bình (sách in)           (lịch TRUNG QUỐC)       (dữ liệu Mặt Trăng)
   └─ văn bản nghỉ Tết
                                    NHÁNH D — Hồ Ngọc Đức
                                      ├─ amlich.app  (TỰ CÔNG BỐ là HND)
                                      ├─ hàng chục web lịch VN
                                      └─ các repo "MIT"

   lichviet.app  ──?──  ancestry KHÔNG XÁC ĐỊNH ĐƯỢC
                        (tính phía server, không đọc được)
                        có thể thuộc nhánh D, có thể không
```

**Quy tắc giữ nguyên:** hai nguồn chỉ tính là độc lập khi thuộc **hai nhánh khác
chữ cái**. lichviet.app **chưa gán được nhánh** ⇒ không bao giờ được đếm là nguồn thứ
hai bên cạnh một nguồn nhánh D.
