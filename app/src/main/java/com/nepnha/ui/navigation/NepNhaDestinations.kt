package com.nepnha.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.nepnha.R

/**
 * Bốn mục của thanh điều hướng dưới. Đúng bốn — không hơn.
 *
 * Nghi lễ và Prayer Reader **không** phải tab: chúng luôn đi từ Nhà hoặc Lịch, vì
 * người dùng không mở app để "duyệt danh mục nghi lễ" mà để biết hôm nay cần làm gì.
 *
 * Icon là vector drawable tự đóng gói trong `res/drawable`, KHÔNG dùng
 * `androidx.compose.material:material-icons-*`: Material 3 1.4 đã bỏ phụ thuộc đó và
 * bản thân thư viện icon nay là deprecated — thêm nó chỉ để lấy 5 hình là không đáng.
 */
enum class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    HOME("home", R.string.nav_home, R.drawable.ic_nav_home),
    CALENDAR("calendar", R.string.nav_calendar, R.drawable.ic_nav_calendar),
    FAMILY("family", R.string.nav_family, R.drawable.ic_nav_family),
    SETTINGS("settings", R.string.nav_settings, R.drawable.ic_nav_settings),
}

/** Các điểm đến không nằm trên thanh dưới. */
object Routes {
    const val ARG_MEMBER_ID = "memberId"

    /** Giá trị "không có id" — thêm mới thay vì sửa. NavType.LongType không nhận null. */
    const val NO_MEMBER_ID = -1L

    const val ARG_MEMORIAL_ID = "memorialId"

    /** Giá trị "không có id" — thêm mới thay vì sửa. NavType.LongType không nhận null. */
    const val NO_MEMORIAL_ID = -1L

    const val MEMBER_EDITOR = "family/member?$ARG_MEMBER_ID={$ARG_MEMBER_ID}"
    const val MEMORIALS = "memorials"
    const val MEMORIAL_EDITOR = "memorials/edit?$ARG_MEMORIAL_ID={$ARG_MEMORIAL_ID}"

    fun memorialEditor(memorialId: Long?): String =
        "memorials/edit?$ARG_MEMORIAL_ID=${memorialId ?: NO_MEMORIAL_ID}"
    const val CHOOSE_WORSHIPPER = "family/worshipper"

    fun memberEditor(memberId: Long?): String =
        "family/member?$ARG_MEMBER_ID=${memberId ?: NO_MEMBER_ID}"
}
