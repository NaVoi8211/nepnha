# Quan hệ với Meeus — nói cho đúng phạm vi

> Làm rõ **provenance**. ⚠️ **Không phải tư vấn pháp lý.**
>
> Tài liệu này thay thế mọi phát biểu ngụ ý rằng toàn bộ pipeline của Nếp Nhà
> "sạch Meeus". **Phát biểu đó không đúng** và đã được rút.

## Vì sao phải viết lại

Mô hình ΔT của đường sản xuất là đa thức NASA/GSFC ở
[deltatpoly.html](https://eclipse.gsfc.nasa.gov/SEcat5/deltatpoly.html). Trang đó ghi
ngay dưới tiêu đề:

> *Polynomial Expressions for Delta T (ΔT)*
> *Five Millennium Canon of Solar Eclipses* **[Espenak and Meeus]**

Nên không thể vừa dùng đa thức này vừa nói pipeline không liên quan gì tới Meeus.

## Còn trực tiếp hơn thế: chính dữ liệu điểm Sóc cũng dựa trên Meeus

Cuối trang phase catalog của NASA ghi nguyên văn:

> *"Algorithms used in predicting the phases of the Moon and eclipses are based on Jean
> Meeus' Astronomical Algorithms (Willmann-Bell, Inc., 1998). All calculations are by
> Fred Espenak, and he assumes full responsibility for their accuracy."*
>
> *"Permission is freely granted to reproduce this data when accompanied by an
> acknowledgment."*

Nghĩa là quan hệ với Meeus không chỉ nằm ở tiêu đề tài liệu ΔT: **dữ liệu điểm Sóc mà
Nếp Nhà dùng cũng do thuật toán dựa trên Meeus sinh ra**. Chỗ dựa của dự án là **câu
cho phép tái sử dụng ngay bên dưới**, kèm ghi công bắt buộc.

## Năm phát biểu đúng

| | |
|---|---|
| **A** | **Không dòng mã nguồn nào của Meeus được sao chép hay chạy trong Nếp Nhà.** Không có `moon98`, không có hiện thực thuật toán Meeus nào trên đường sản xuất |
| **B** | **Dữ liệu điểm Sóc** được tái sử dụng từ NASA/GSFC Six Millennium Catalog theo điều khoản NASA/Fred Espenak công bố. Chính NASA nói dữ liệu này do thuật toán **dựa trên Meeus** tính ra |
| **C** | **Mô hình ΔT** là đa thức NASA/Fred Espenak. Nó là **MÔ HÌNH**, không phải số đo |
| **D** | Tài liệu NASA có dẫn **Espenak và Meeus** |
| **E** | ⇒ Dự án **không** được tuyên bố rằng toàn bộ provenance toán học của mình là "Meeus-free" |

## Chính sách viết lại

> **Không đưa mã nguồn, hiện thực, hay biểu thức sao chép có nguồn gốc Meeus vào
> ứng dụng.**

So với hiện trạng:

| Nơi | Có gì | Thoả chính sách? |
|---|---|---|
| **APK** | chỉ một bảng số nguyên (`vn_lunar_v1.bin`) | ✅ không mã, không biểu thức |
| `tools/deltat.py` | **chép lại đa thức** do NASA công bố | ✅ nằm ở công cụ dev, **không vào ứng dụng** |
| `tools/benchmark_erfa_astronomy/` | `moon98` chỉ được **liên kết** vào binary dev, **không bao giờ được gọi** trên đường sinh dataset | ✅ không vào ứng dụng |

> ⚠️ **Một điểm diễn giải để bạn quyết, không phải tôi.** Nếu "ứng dụng" được hiểu là
> **cả kho mã** chứ không chỉ APK, thì việc `tools/deltat.py` chép lại biểu thức sẽ
> rơi vào diện cần xem xét. Theo đúng câu chữ đã thống nhất — *"vào ứng dụng"* — thì
> hiện trạng thoả. Tôi nêu ra chứ không tự kết luận thay.

## Còn nguyên hiệu lực

Không dùng Hồ Ngọc Đức · không dùng repo phái sinh HND · không sao chép mã hay dữ
liệu từ bất kỳ hiện thực lịch Việt Nam nào · không nhúng dữ liệu lịch online vào APK ·
không scrape VnExpress · không vượt robots.txt hay điều khoản dịch vụ.

## Phát biểu nào vẫn đúng nguyên văn

Câu *"nhánh `epv00`/`eqec06`/`nut06a` không phái sinh từ Meeus"* **vẫn đúng** — nó nói
về **mã nguồn ERFA dùng cho trung khí**, không nói về mô hình ΔT. Các tài liệu Phase
3A dùng câu này trong đúng phạm vi đó. Cái sai là **suy rộng** nó thành "cả pipeline
sạch Meeus".
