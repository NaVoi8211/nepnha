package com.nepnha.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Bảng màu Nếp Nhà — cố ý ÍT màu.
 *
 * Một màu nhấn duy nhất: đỏ nâu trầm, gợi sơn son / gỗ mộc trên bàn thờ Việt mà
 * không rơi vào cảm giác "tâm linh loè loẹt". Phần còn lại là nền giấy ấm và mực
 * gần đen để tương phản chữ đạt mức cao — người lớn tuổi phải đọc được.
 */

// Nhấn
val BrandRed = Color(0xFF8C1C13)
val BrandRedLight = Color(0xFFB3453B)
val BrandRedContainer = Color(0xFFFBE0DC)
val OnBrandRedContainer = Color(0xFF450E09)

// Phụ trợ (dùng rất tiết chế: badge "hôm nay", trạng thái sắp tới)
val Bronze = Color(0xFF7A5C2E)
val BronzeContainer = Color(0xFFF3E6CE)
val OnBronzeContainer = Color(0xFF3A2A0D)

// Nền & chữ — sáng
val PaperLight = Color(0xFFFFFBF8)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF2EAE5)

// Nhóm surfaceContainer — Material 3 dùng cho NavigationBar, TopAppBar, Card nâng
// cao… Phải khai báo đủ, nếu bỏ trống thì M3 rơi về baseline TÍM và thanh điều
// hướng sẽ lạc tông hoàn toàn (đã gặp đúng lỗi này khi test trên A32).
val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
val SurfaceContainerLowLight = Color(0xFFFFF7F2)
val SurfaceContainerLight = Color(0xFFF8F0EA)
val SurfaceContainerHighLight = Color(0xFFF2EAE4)
val SurfaceContainerHighestLight = Color(0xFFECE3DC)
val SurfaceDimLight = Color(0xFFE8DFD8)
val InkLight = Color(0xFF1E1A18)
val InkMutedLight = Color(0xFF5A514C)
val OutlineLight = Color(0xFFD6CBC4)

// Nền & chữ — tối
val PaperDark = Color(0xFF14100E)
val SurfaceDark = Color(0xFF1E1917)
val SurfaceVariantDark = Color(0xFF332C29)

val SurfaceContainerLowestDark = Color(0xFF0F0C0B)
val SurfaceContainerLowDark = Color(0xFF1B1614)
val SurfaceContainerDark = Color(0xFF221D1A)
val SurfaceContainerHighDark = Color(0xFF2D2724)
val SurfaceContainerHighestDark = Color(0xFF38312D)
val SurfaceDimDark = Color(0xFF14100E)
val InkDark = Color(0xFFF2EAE5)
val InkMutedDark = Color(0xFFBFB3AC)
val OutlineDark = Color(0xFF544A45)
