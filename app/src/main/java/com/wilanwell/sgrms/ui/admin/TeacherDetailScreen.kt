package com.wilanwell.sgrms.ui.admin

import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
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
import com.wilanwell.sgrms.ui.components.SgrmsInfoRow
import com.wilanwell.sgrms.viewmodel.TeacherFirebaseViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.ChipTone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Key
import com.wilanwell.sgrms.ui.components.SgrmsDetailScreen
import com.wilanwell.sgrms.ui.components.SgrmsDetailHero
import com.wilanwell.sgrms.ui.components.SgrmsInfoSection
import com.wilanwell.sgrms.ui.components.SgrmsRowDivider
import com.wilanwell.sgrms.ui.components.SgrmsEmptyState
import com.wilanwell.sgrms.ui.components.SgrmsPrimaryButton

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
        title = stringResource(R.string.teacher),
        onBack = onBackClick,
        isLoading = uiState.isLoading && teacher == null,
        errorMessage = if (teacher != null) uiState.errorMessage else "",
        bottomBar = if (teacher != null) {
            { SgrmsPrimaryButton(text = stringResource(R.string.edit_teacher_2), icon = Icons.Default.Edit, onClick = onEditClick, modifier = Modifier.weight(1f)) }
        } else {
            null
        }
    ) {
        if (teacher == null) {
            SgrmsEmptyState(
                title = stringResource(R.string.teacher_not_found),
                message = uiState.errorMessage.ifBlank { stringResource(R.string.the_selected_teacher_record_could_not) },
                icon = Icons.Default.SupervisorAccount
            )
            return@SgrmsDetailScreen
        }

        SgrmsDetailHero(
            title = teacher.teacherName.ifBlank { stringResource(R.string.unknown_teacher) },
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
            SgrmsStatusChip(teacher.position.ifBlank { stringResource(R.string.teacher) }, ChipTone.Success)
        }

        SgrmsInfoSection(stringResource(R.string.contact)) {
            SgrmsInfoRow(icon = Icons.Default.Email, label = stringResource(R.string.email), value = teacher.email.ifBlank { stringResource(R.string.not_specified) })
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Phone, label = stringResource(R.string.phone), value = teacher.phoneNumber.ifBlank { stringResource(R.string.not_specified) })
        }

        SgrmsInfoSection(stringResource(R.string.work)) {
            SgrmsInfoRow(icon = Icons.Default.Badge, label = stringResource(R.string.staff_id), value = teacher.staffId.ifBlank { stringResource(R.string.not_specified) })
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.Business, label = stringResource(R.string.department), value = teacher.department.ifBlank { stringResource(R.string.not_specified) })
            SgrmsRowDivider()
            SgrmsInfoRow(icon = Icons.Default.WorkOutline, label = stringResource(R.string.position), value = teacher.position.ifBlank { stringResource(R.string.teacher) })
            if (teacher.qualification.isNotBlank()) {
                SgrmsRowDivider()
                SgrmsInfoRow(icon = Icons.Default.School, label = stringResource(R.string.qualification), value = teacher.qualification)
            }
            if (teacher.officeLocation.isNotBlank()) {
                SgrmsRowDivider()
                SgrmsInfoRow(icon = Icons.Default.Place, label = stringResource(R.string.office), value = teacher.officeLocation)
            }
        }

        SgrmsInfoSection(stringResource(R.string.account)) {
            SgrmsInfoRow(icon = Icons.Default.Key, label = stringResource(R.string.login_user_id), value = teacher.userId.ifBlank { stringResource(R.string.not_linked) })
        }
    }
}

