package com.wilanwell.sgrms.ui.profile

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import com.wilanwell.sgrms.ui.components.OnReturnToScreen
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import com.wilanwell.sgrms.viewmodel.AdminProfileViewModel
import com.wilanwell.sgrms.data.model.UserRole
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import com.wilanwell.sgrms.ui.components.toneForStatus
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Notes
import com.wilanwell.sgrms.ui.components.SgrmsDetailScreen
import com.wilanwell.sgrms.ui.components.SgrmsDetailHero
import com.wilanwell.sgrms.ui.components.SgrmsInfoSection
import com.wilanwell.sgrms.ui.components.SgrmsInfoRow
import com.wilanwell.sgrms.ui.components.SgrmsRowDivider
import com.wilanwell.sgrms.ui.components.SgrmsSettingsRow
import com.wilanwell.sgrms.ui.components.SgrmsGroupedCard

@Composable
fun AdminProfileScreen(
    onBackClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val adminTeachersStudents2Text = stringResource(R.string.accounts_breakdown_long)
    val classesSubjectsTeachersStudentsText = stringResource(R.string.classes_subjects_teachers_students)
    val viewModel: AdminProfileViewModel = hiltViewModel()
    OnReturnToScreen { viewModel.refresh() }
    val uiState by viewModel.profileUiState.collectAsState()
    val summary by viewModel.summary.collectAsState()

    val user = uiState.profile
    val profileFound = user != null
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage

    val fullName = user?.fullName?.ifBlank { null } ?: stringResource(R.string.admin_account)
    val username = user?.username?.ifBlank { null } ?: user?.email.orEmpty()
    val email = user?.email.orEmpty()
    val phoneNumber = user?.phoneNumber.orEmpty()
    val address = user?.address.orEmpty()
    val profileNote = user?.profileNote.orEmpty()
    val role = user?.role?.ifBlank { null } ?: UserRole.ADMIN.value
    val status = user?.status?.ifBlank { null } ?: stringResource(R.string.active)

    val userCount = summary.totalAccounts
    val teacherCount = summary.teachers
    val studentCount = summary.students
    val classCount = summary.classes
    val subjectCount = summary.subjects
    val adminCount = summary.adminAccounts
    val teacherAccountCount = summary.teacherAccounts
    val studentAccountCount = summary.studentAccounts

    SgrmsDetailScreen(
        title = stringResource(R.string.profile),
        onBack = onBackClick,
        isLoading = isLoading && user == null,
        errorMessage = errorMessage
    ) {
        SgrmsDetailHero(
            title = fullName,
            subtitle = email,
            leading = {
                SgrmsAvatar(name = fullName, size = 88.dp, imageUrl = user?.profileImageUrl.orEmpty(), legacyImageBase64 = user?.profileImageBase64.orEmpty())
            }
        ) {
            SgrmsStatusChip(role, ChipTone.Warning)
            SgrmsStatusChip(status, toneForStatus(status))
        }

        SgrmsInfoSection(stringResource(R.string.account)) {
            SgrmsInfoRow(icon = Icons.Default.Badge, label = stringResource(R.string.username), value = username)
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Phone, label = stringResource(R.string.phone), value = phoneNumber)
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Home, label = stringResource(R.string.address), value = address)
            if (profileNote.isNotBlank()) {
                SgrmsRowDivider()
                SgrmsInfoRow(icon = Icons.AutoMirrored.Filled.Notes, label = stringResource(R.string.note), value = profileNote)
            }
        }

        SgrmsInfoSection(stringResource(R.string.system)) {
            SgrmsInfoRow(icon = Icons.Default.ManageAccounts, label = stringResource(R.string.accounts), value = adminTeachersStudents2Text.format(userCount, adminCount, teacherAccountCount, studentAccountCount))
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.School, label = stringResource(R.string.school), value = classesSubjectsTeachersStudentsText.format(classCount, subjectCount, teacherCount, studentCount))
        }

        SgrmsGroupedCard {
            SgrmsSettingsRow(stringResource(R.string.edit_profile), Icons.Default.Edit, onEditProfileClick)
            SgrmsRowDivider()
            SgrmsSettingsRow(stringResource(R.string.change_password), Icons.Default.Lock, onChangePasswordClick)
        }

        SgrmsGroupedCard {
            SgrmsSettingsRow(stringResource(R.string.log_out), Icons.AutoMirrored.Filled.Logout, onLogoutClick, destructive = true)
        }
    }
}
