package com.nepnha.ui.family

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nepnha.R
import com.nepnha.data.repository.FamilyOverview
import com.nepnha.domain.model.FamilyMember
import com.nepnha.ui.components.EmptyStateCard
import com.nepnha.ui.components.SectionHeader

/**
 * "Gia đình tôi" — nay chạy trên dữ liệu Room thật.
 *
 * Đổi tên gia đình dùng dialog thay vì một màn hình riêng: một ô chữ thì không đáng
 * một điểm đến điều hướng.
 */
@Composable
fun FamilyScreen(
    state: FamilyOverview,
    onAddMember: () -> Unit,
    onEditMember: (Long) -> Unit,
    onChooseWorshipper: () -> Unit,
    onRenameFamily: (String) -> Unit,
    onDeleteMember: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var renaming by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by rememberSaveable { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("screen_family"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = state.family?.name ?: stringResource(R.string.family_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                TextButton(
                    onClick = { renaming = true },
                    modifier = Modifier.testTag("btn_rename_family"),
                ) {
                    Text(stringResource(R.string.family_rename))
                }
            }
        }

        item {
            SectionHeader(text = stringResource(R.string.family_worshipper_section))
            WorshipperCard(
                primary = state.primaryMember,
                onChoose = onChooseWorshipper,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionHeader(text = stringResource(R.string.family_members_section))
                Text(
                    text = stringResource(R.string.family_member_count, state.members.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.members.isEmpty()) {
            item {
                EmptyStateCard(
                    title = stringResource(R.string.family_empty_title),
                    body = stringResource(R.string.family_empty_body),
                    actionLabel = stringResource(R.string.family_add_member),
                    onAction = onAddMember,
                )
            }
        } else {
            items(state.members, key = { it.id }) { member ->
                MemberRow(
                    member = member,
                    isWorshipper = member.id == state.primaryMember?.id,
                    onClick = { onEditMember(member.id) },
                    onDelete = { pendingDelete = member.id },
                )
            }
            item {
                OutlinedButton(
                    onClick = onAddMember,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .testTag("btn_add_member"),
                ) {
                    Text(stringResource(R.string.family_add_member))
                }
            }
        }
    }

    if (renaming) {
        RenameFamilyDialog(
            currentName = state.family?.name.orEmpty(),
            onDismiss = { renaming = false },
            onConfirm = { newName ->
                onRenameFamily(newName)
                renaming = false
            },
        )
    }

    pendingDelete?.let { memberId ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.delete_member_title)) },
            text = { Text(stringResource(R.string.delete_member_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteMember(memberId)
                        pendingDelete = null
                    },
                    modifier = Modifier.testTag("btn_confirm_delete"),
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
            modifier = Modifier.testTag("dialog_delete_member"),
        )
    }
}

@Composable
private fun WorshipperCard(primary: FamilyMember?, onChoose: () -> Unit) {
    if (primary == null) {
        EmptyStateCard(
            title = stringResource(R.string.family_no_worshipper),
            body = stringResource(R.string.family_no_worshipper_body),
            actionLabel = stringResource(R.string.family_choose_worshipper),
            onAction = onChoose,
            modifier = Modifier.testTag("card_no_worshipper"),
        )
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_worshipper"),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(text = primary.fullName, style = MaterialTheme.typography.titleMedium)
                TextButton(
                    onClick = onChoose,
                    modifier = Modifier.testTag("btn_change_worshipper"),
                ) { Text(stringResource(R.string.family_change_worshipper)) }
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: FamilyMember,
    isWorshipper: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("member_${member.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = member.fullName, style = MaterialTheme.typography.titleMedium)
            if (isWorshipper) {
                Text(
                    text = stringResource(R.string.member_worshipper_badge),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            member.role?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = member.solarBirthDate
                    ?.let { com.nepnha.core.time.VietnameseDateFormatter.shortDate(it) }
                    ?: stringResource(R.string.member_solar_unknown),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(
                onClick = onDelete,
                modifier = Modifier.testTag("btn_delete_${member.id}"),
            ) { Text(stringResource(R.string.member_delete)) }
        }
    }
}

@Composable
private fun RenameFamilyDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.family_rename)) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(stringResource(R.string.family_rename_label)) },
                singleLine = true,
                modifier = Modifier.testTag("field_family_name"),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value) },
                modifier = Modifier.testTag("btn_confirm_rename"),
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
        modifier = Modifier.testTag("dialog_rename_family"),
    )
}
