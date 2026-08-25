package com.nepnha.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nepnha.R
import com.nepnha.ui.components.InfoCard
import com.nepnha.ui.theme.NepNhaTheme

/**
 * Cài đặt tối giản: giới thiệu, phiên bản, và một lời nói rõ dữ liệu nằm ở đâu.
 *
 * Không tài khoản, không đồng bộ, không "sao lưu đám mây" — đó là chủ đích sản phẩm
 * chứ không phải tính năng còn thiếu, nên mục "Dữ liệu của bạn" nói thẳng điều đó.
 */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // buildConfig bị tắt để build nhanh hơn ⇒ lấy version từ PackageManager.
    val versionName = remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "—"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("screen_settings"),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        InfoCard(
            title = stringResource(R.string.settings_about_title),
            body = stringResource(R.string.settings_about_body),
        )
        InfoCard(
            title = stringResource(R.string.settings_privacy_title),
            body = stringResource(R.string.settings_privacy_body),
        )
        InfoCard(
            title = stringResource(R.string.settings_display_title),
            body = stringResource(R.string.settings_display_body),
        )
        InfoCard(
            title = stringResource(R.string.settings_version_title),
            body = versionName,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    NepNhaTheme { SettingsScreen() }
}
