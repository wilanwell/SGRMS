package com.wilanwell.sgrms.ui.profile

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
    val viewModel: AdminProfileViewModel = hiltViewModel()
    OnReturnToScreen { viewModel.refresh() }
    val uiState by viewModel.profileUiState.collectAsState()
    val summary by viewModel.summary.collectAsState()

    val user = uiState.profile
    val profileFound = user != null
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage

    val fullName = user?.fullName?.ifBlank { null } ?: "Admin Account"
    val username = user?.username?.ifBlank { null } ?: user?.email.orEmpty()
    val email = user?.email.orEmpty()
    val phoneNumber = user?.phoneNumber.orEmpty()
    val address = user?.address.orEmpty()
    val profileNote = user?.profileNote.orEmpty()
    val role = user?.role?.ifBlank { null } ?: UserRole.ADMIN.value
    val status = user?.status?.ifBlank { null } ?: "Active"

    val userCount = summary.totalAccounts
    val teacherCount = summary.teachers
    val studentCount = summary.students
    val classCount = summary.classes
    val subjectCount = summary.subjects
    val adminCount = summary.adminAccounts
    val teacherAccountCount = summary.teacherAccounts
    val studentAccountCount = summary.studentAccounts

    SgrmsDetailScreen(
        title = "Profile",
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

        SgrmsInfoSection("Account") {
            SgrmsInfoRow(icon = Icons.Default.Badge, label = "Username", value = username)
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Phone, label = "Phone", value = phoneNumber)
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Home, label = "Address", value = address)
            if (profileNote.isNotBlank()) {
                SgrmsRowDivider()
                SgrmsInfoRow(icon = Icons.AutoMirrored.Filled.Notes, label = "Note", value = profileNote)
            }
        }

        SgrmsInfoSection("System") {
            SgrmsInfoRow(icon = Icons.Default.ManageAccounts, label = "Accounts", value = "$userCount · $adminCount admin, $teacherAccountCount teachers, $studentAccountCount students")
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.School, label = "School", value = "$classCount classes · $subjectCount subjects · $teacherCount teachers · $studentCount students")
        }

        SgrmsGroupedCard {
            SgrmsSettingsRow("Edit profile", Icons.Default.Edit, onEditProfileClick)
            SgrmsRowDivider()
            SgrmsSettingsRow("Change password", Icons.Default.Lock, onChangePasswordClick)
        }

        SgrmsGroupedCard {
            SgrmsSettingsRow("Log out", Icons.AutoMirrored.Filled.Logout, onLogoutClick, destructive = true)
        }
    }
}
