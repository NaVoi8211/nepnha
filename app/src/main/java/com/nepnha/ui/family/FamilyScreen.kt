package com.nepnha.ui.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nepnha.R
import com.nepnha.ui.components.EmptyStateCard
import com.nepnha.ui.theme.NepNhaTheme

/**
 * Vỏ UI của "Gia đình tôi".
 *
 * Phase 1 **chưa có persistence**: không Room, không CRUD, không lưu gì. Nút thêm
 * thành viên chỉ điều hướng sang một màn hình nói rõ là đang xây dựng — thà vậy còn
 * hơn một biểu mẫu bấm xong mất dữ liệu. Room và CRUD thật thuộc Phase 2.
 */
@Composable
fun FamilyScreen(
    onAddMember: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("screen_family"),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.family_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        EmptyStateCard(
            title = stringResource(R.string.family_empty_title),
            body = stringResource(R.string.family_empty_body),
            actionLabel = stringResource(R.string.family_add_member),
            onAction = onAddMember,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FamilyScreenPreview() {
    NepNhaTheme { FamilyScreen(onAddMember = {}) }
}
