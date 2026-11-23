package com.example.sgrms.ui.admin

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sgrms.ui.components.SgrmsInfoRow
import com.example.sgrms.viewmodel.TeacherFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sgrms.ui.components.SgrmsAvatar
import com.example.sgrms.ui.components.SgrmsStatusChip
import com.example.sgrms.ui.components.ChipTone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Key
import com.example.sgrms.ui.components.SgrmsDetailScreen
import com.example.sgrms.ui.components.SgrmsDetailHero
import com.example.sgrms.ui.components.SgrmsInfoSection
import com.example.sgrms.ui.components.SgrmsRowDivider
import com.example.sgrms.ui.components.SgrmsEmptyState
import com.example.sgrms.ui.components.SgrmsPrimaryButton

@Composable
fun TeacherDetailScreen(
    teacherId: String,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {}
) {
    val teacherViewModel: TeacherFirebaseViewModel = hiltViewModel()

    val uiState by teacherViewModel.uiState.collectAsState()

    LaunchedEffect(teacherId) {
        if (teacherId.isNotBlank()) {
            teacherViewModel.loadTeacherById(teacherId)
        }
    }

    val teacher = uiState.selectedTeacher

    SgrmsDetailScreen(
        title = "Teacher",
        onBack = onBackClick,
        isLoading = uiState.isLoading && teacher == null,
        errorMessage = if (teacher != null) uiState.errorMessage else "",
        bottomBar = if (teacher != null) {
            { SgrmsPrimaryButton(text = "Edit teacher", icon = Icons.Default.Edit, onClick = onEditClick, modifier = Modifier.weight(1f)) }
        } else {
            null
        }
    ) {
        if (teacher == null) {
            SgrmsEmptyState(
                title = "Teacher not found",
                message = uiState.errorMessage.ifBlank { "The selected teacher record could not be loaded." },
                icon = Icons.Default.SupervisorAccount
            )
            return@SgrmsDetailScreen
        }

        SgrmsDetailHero(
            title = teacher.teacherName.ifBlank { "Unknown teacher" },
            subtitle = listOf(teacher.staffId, teacher.department).filter { it.isNotBlank() }.joinToString(" · "),
            leading = {
                SgrmsAvatar(
                    name = teacher.teacherName,
                    size = 88.dp,
                    imageUrl = teacher.profileImageUrl,
                    legacyImageBase64 = teacher.profileImageBase64
                )
            }
        ) {
            SgrmsStatusChip(teacher.position.ifBlank { "Teacher" }, ChipTone.Success)
        }

        SgrmsInfoSection("Contact") {
            SgrmsInfoRow(icon = Icons.Default.Email, label = "Email", value = teacher.email.ifBlank { "Not specified" })
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Phone, label = "Phone", value = teacher.phoneNumber.ifBlank { "Not specified" })
        }

        SgrmsInfoSection("Work") {
            SgrmsInfoRow(icon = Icons.Default.Badge, label = "Staff ID", value = teacher.staffId.ifBlank { "Not specified" })
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Business, label = "Department", value = teacher.department.ifBlank { "Not specified" })
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.WorkOutline, label = "Position", value = teacher.position.ifBlank { "Teacher" })
            if (teacher.qualification.isNotBlank()) {
                SgrmsRowDivider()
                SgrmsInfoRow(icon = Icons.Default.School, label = "Qualification", value = teacher.qualification)
            }
            if (teacher.officeLocation.isNotBlank()) {
                SgrmsRowDivider()
                SgrmsInfoRow(icon = Icons.Default.Place, label = "Office", value = teacher.officeLocation)
            }
        }

        SgrmsInfoSection("Account") {
            SgrmsInfoRow(icon = Icons.Default.Key, label = "Login user ID", value = teacher.userId.ifBlank { "Not linked" })
        }
    }
}

