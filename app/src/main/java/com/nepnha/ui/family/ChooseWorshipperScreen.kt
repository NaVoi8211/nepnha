package com.nepnha.ui.family

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.nepnha.domain.model.FamilyMember
import com.nepnha.ui.components.EmptyStateCard

/**
 * Chọn tín chủ — người đứng khấn.
 *
 * Nếp Nhà **không tự chọn**: "tín chủ" là vai trò trong nhà, không suy ra được từ
 * thứ tự nhập liệu. Nhà có ông, bà, bố, mẹ, con — người nhập đầu tiên không nghiễm
 * nhiên là người đứng khấn.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseWorshipperScreen(
    members: List<FamilyMember>,
    selectedId: Long?,
    onSelect: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag("screen_choose_worshipper"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.family_choose_worshipper_title)) },
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
        if (members.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(insets).padding(20.dp)) {
                EmptyStateCard(
                    title = stringResource(R.string.family_no_worshipper),
                    body = stringResource(R.string.family_choose_worshipper_empty),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(insets),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(members, key = { it.id }) { member ->
                    val isSelected = member.id == selectedId
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(member.id) }
                            .testTag("worshipper_option_${member.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            contentColor = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(member.fullName, style = MaterialTheme.typography.titleMedium)
                            member.role?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
