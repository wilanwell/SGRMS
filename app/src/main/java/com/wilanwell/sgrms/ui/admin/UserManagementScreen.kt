package com.wilanwell.sgrms.ui.admin

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wilanwell.sgrms.data.model.UserModel
import com.wilanwell.sgrms.viewmodel.UserManagementViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.wilanwell.sgrms.ui.components.SgrmsDeleteDialog
import com.wilanwell.sgrms.ui.components.SgrmsInfoRow
import com.wilanwell.sgrms.ui.theme.ColorPrimary
import com.wilanwell.sgrms.ui.components.ChipGroup
import com.wilanwell.sgrms.ui.components.EmptyContent
import com.wilanwell.sgrms.ui.components.ListItemPadding
import com.wilanwell.sgrms.ui.components.RowAction
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsListItemCard
import com.wilanwell.sgrms.ui.components.SgrmsListScreen
import com.wilanwell.sgrms.ui.components.SgrmsOverflowMenu
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Add

@Composable
fun UserManagementScreen(
    onBackClick: () -> Unit = {},
    onAddTeacherClick: () -> Unit = {},
    onAddStudentAccountClick: () -> Unit = {},
    onEditUserClick: (String) -> Unit = {}
) {
    val allRolesText = stringResource(R.string.all_roles)
    val systemAdminText = stringResource(R.string.system_admin)
    val tryAnotherSearchOrRoleText = stringResource(R.string.try_another_search_or_role)
    val viewModel: UserManagementViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val users = uiState.users
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage

    var searchText by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("All Roles") }

    var selectedUserToDelete by remember { mutableStateOf<UserModel?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var selectedUserToView by remember { mutableStateOf<UserModel?>(null) }
    var selectedLinkedIdentityToView by remember { mutableStateOf("") }
    var showViewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    val roleList = listOf(
        allRolesText,
        "Admin",
        "Teacher",
        "Student"
    )

    val filteredUsers = users.filter { user ->
        val linkedId = when (user.role) {
            "Teacher" -> user.teacherId
            "Student" -> user.studentId
            "Admin" -> systemAdminText
            else -> "-"
        }

        val matchesSearch =
            user.fullName.contains(searchText, ignoreCase = true) ||
                    user.email.contains(searchText, ignoreCase = true) ||
                    user.role.contains(searchText, ignoreCase = true) ||
                    linkedId.contains(searchText, ignoreCase = true)

        val matchesRole =
            selectedRole == "All Roles" || user.role == selectedRole

        matchesSearch && matchesRole
    }

    val adminCount = users.count { it.role == "Admin" }
    val teacherCount = users.count { it.role == "Teacher" }
    val studentCount = users.count { it.role == "Student" }

    if (showDeleteDialog && selectedUserToDelete != null) {
        SgrmsDeleteDialog(
            title = stringResource(R.string.delete_user),
            message = stringResource(R.string.this_account_will_stop_working_at),
            onConfirmClick = {
                val userToDelete = selectedUserToDelete

                if (userToDelete != null) {
                    viewModel.deleteAccount(userToDelete)
                }

                showDeleteDialog = false
                selectedUserToDelete = null
            },
            onDismissClick = {
                showDeleteDialog = false
                selectedUserToDelete = null
            }
        )
    }

    if (showViewDialog && selectedUserToView != null) {
        UserDetailDialog(
            user = selectedUserToView!!,
            linkedIdentity = selectedLinkedIdentityToView,
            onDismiss = {
                showViewDialog = false
                selectedUserToView = null
                selectedLinkedIdentityToView = ""
            }
        )
    }

    SgrmsListScreen(
        title = stringResource(R.string.users),
        subtitle = stringResource(R.string.admin_teachers_students, adminCount, teacherCount, studentCount),
        onBack = onBackClick,
        searchText = searchText,
        onSearchChange = { searchText = it },
        searchPlaceholder = stringResource(R.string.search_name_email_or_role),
        isLoading = isLoading,
        errorMessage = errorMessage,
        isEmpty = filteredUsers.isEmpty(),
        empty = EmptyContent(stringResource(R.string.no_users_found), tryAnotherSearchOrRoleText, Icons.Default.ManageAccounts),
        chipGroups = listOf(ChipGroup(roleList, selectedRole) { selectedRole = it }),
        headerActions = {
            SgrmsOverflowMenu(
                contentDescription = stringResource(R.string.add_account),
                actions = listOf(
                    RowAction(stringResource(R.string.add_teacher), Icons.Default.PersonAdd, onClick = onAddTeacherClick),
                    RowAction(stringResource(R.string.add_student), Icons.Default.PersonAdd, onClick = onAddStudentAccountClick)
                )
            )
        }
    ) {
        items(items = filteredUsers, key = { it.userId }) { user ->
            val linkedIdentity = when (user.role) {
                "Student" -> user.studentId.ifBlank { stringResource(R.string.not_linked) }
                "Teacher" -> user.teacherId.ifBlank { stringResource(R.string.not_linked) }
                "Admin" -> systemAdminText
                else -> "-"
            }
            val tone = when (user.role) {
                "Admin" -> ChipTone.Warning
                "Teacher" -> ChipTone.Success
                else -> ChipTone.Primary
            }

            Box(ListItemPadding) {
                SgrmsListItemCard(
                    title = user.fullName.ifBlank { user.email },
                    subtitle = user.email,
                    leading = {
                        SgrmsAvatar(
                            name = user.fullName.ifBlank { user.email },
                            size = 44.dp,
                            imageUrl = user.profileImageUrl,
                            legacyImageBase64 = user.profileImageBase64
                        )
                    },
                    onClick = {
                        selectedUserToView = user
                        selectedLinkedIdentityToView = linkedIdentity
                        showViewDialog = true
                    }
                ) {
                    SgrmsStatusChip(user.role.ifBlank { stringResource(R.string.user) }, tone)
                    SgrmsOverflowMenu(
                        contentDescription = stringResource(R.string.actions_for, user.fullName),
                        actions = buildList {
                            add(RowAction(stringResource(R.string.view), Icons.Default.Visibility) {
                                selectedUserToView = user
                                selectedLinkedIdentityToView = linkedIdentity
                                showViewDialog = true
                            })
                            if (user.role == "Teacher" && user.teacherId.isNotBlank()) {
                                add(RowAction(stringResource(R.string.edit), Icons.Default.Edit) { onEditUserClick(user.teacherId) })
                            }
                            if (user.role != "Admin") {
                                add(RowAction(stringResource(R.string.delete), Icons.Default.Delete, destructive = true) {
                                    selectedUserToDelete = user
                                    showDeleteDialog = true
                                })
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UserDetailDialog(
    user: UserModel,
    linkedIdentity: String,
    onDismiss: () -> Unit
) {
    val notSpecifiedText = stringResource(R.string.not_specified)
    val unknownUserText = stringResource(R.string.unknown_user)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.user_details),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                SgrmsInfoRow(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.full_name),
                    value = user.fullName.ifBlank {
                        unknownUserText
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SgrmsInfoRow(
                    icon = Icons.Default.Badge,
                    label = stringResource(R.string.user_id),
                    value = user.userId.ifBlank {
                        "-"
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SgrmsInfoRow(
                    icon = Icons.Default.Email,
                    label = stringResource(R.string.email),
                    value = user.email.ifBlank {
                        notSpecifiedText
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SgrmsInfoRow(
                    icon = Icons.Default.ManageAccounts,
                    label = stringResource(R.string.role),
                    value = user.role.ifBlank {
                        "-"
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SgrmsInfoRow(
                    icon = Icons.Default.Badge,
                    label = stringResource(R.string.linked_id),
                    value = linkedIdentity
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.close),
                    color = ColorPrimary
                )
            }
        }
    )
}