package com.nepnha.ui.family

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nepnha.R
import com.nepnha.ui.components.EmptyStateCard

/**
 * Điểm đến placeholder của luồng "Thêm thành viên".
 *
 * Tồn tại ở Phase 1 chỉ để chứng minh điều hướng vào màn hình con và nút Back hoạt
 * động. Biểu mẫu thật + lưu Room thuộc Phase 2.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag("screen_add_member"),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.family_add_member_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            EmptyStateCard(
                title = stringResource(R.string.family_add_member_placeholder_title),
                body = stringResource(R.string.family_add_member_placeholder_body),
            )
        }
    }
}
