package com.wilanwell.sgrms.ui.admin

import androidx.annotation.StringRes
import com.wilanwell.sgrms.R
import androidx.compose.ui.res.stringResource
import com.wilanwell.sgrms.ui.components.OnReturnToScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.data.model.UserRole
import com.wilanwell.sgrms.data.model.userRole
import com.wilanwell.sgrms.ui.components.ChipTone
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsGroupedCard
import com.wilanwell.sgrms.ui.components.SgrmsIconTile
import com.wilanwell.sgrms.ui.components.SgrmsListRow
import com.wilanwell.sgrms.ui.components.SgrmsPageHeader
import com.wilanwell.sgrms.ui.components.SgrmsRowDivider
import com.wilanwell.sgrms.ui.components.SgrmsSectionTitle
import com.wilanwell.sgrms.ui.components.SgrmsStatCard
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.theme.SgrmsRadius
import com.wilanwell.sgrms.ui.theme.SgrmsSpacing
import com.wilanwell.sgrms.viewmodel.AdminDashboardViewModel
import java.util.Calendar

/** "Good morning / afternoon / evening" for the phone's clock, as a string resource. */
@StringRes
fun greetingFor(hourOfDay: Int): Int = when (hourOfDay) {
    in 5..11 -> R.string.good_morning
    in 12..17 -> R.string.good_afternoon
    else -> R.string.good_evening
}

@Composable
fun AdminDashboardScreen(
    adminName: String = "Admin",
    adminImageUrl: String = "",
    adminImageBase64: String = "",
    onUserManagementClick: () -> Unit = {},
    onAddTeacherClick: () -> Unit = {},
    onAddStudentAccountClick: () -> Unit = {},
    onStudentManagementClick: () -> Unit = {},
    onSubjectManagementClick: () -> Unit = {},
    onClassManagementClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onTeacherManagementClick: () -> Unit = {}
) {
    val viewModel: AdminDashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    OnReturnToScreen { viewModel.load() }
    val summary = uiState.summary
    val shown: (Int) -> String = { if (uiState.loaded) it.toString() else "–" }
    val name = adminName.ifBlank { stringResource(R.string.admin) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SgrmsPageHeader(
            title = name,
            subtitle = stringResource(greetingFor(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))),
            trailing = {
                IconButton(onClick = onProfileClick) {
                    SgrmsAvatar(name = name, size = 44.dp, imageUrl = adminImageUrl, legacyImageBase64 = adminImageBase64)
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SgrmsSpacing.ScreenPadding)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (uiState.isLoading && !uiState.loaded) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            if (uiState.errorMessage.isNotBlank()) {
                Text(uiState.errorMessage, color = SgrmsColors.Error, style = MaterialTheme.typography.bodyMedium)
            }

            AccountsHeroCard(
                total = summary.totalAccounts,
                admins = summary.adminAccounts,
                teachers = summary.teacherAccounts,
                students = summary.studentAccounts,
                classes = summary.classes,
                loaded = uiState.loaded
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SgrmsStatCard(
                        value = shown(summary.students),
                        label = stringResource(R.string.students),
                        icon = Icons.Default.Group,
                        tint = MaterialTheme.colorScheme.primary,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onStudentManagementClick,
                        modifier = Modifier.weight(1f)
                    )
                    SgrmsStatCard(
                        value = shown(summary.teachers),
                        label = stringResource(R.string.teachers),
                        icon = Icons.Default.SupervisorAccount,
                        tint = SgrmsColors.Success,
                        container = SgrmsColors.SuccessContainer,
                        onClick = onTeacherManagementClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SgrmsStatCard(
                        value = shown(summary.subjects),
                        label = stringResource(R.string.subjects),
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        tint = SgrmsColors.Orange,
                        container = SgrmsColors.OrangeContainer,
                        onClick = onSubjectManagementClick,
                        modifier = Modifier.weight(1f)
                    )
                    SgrmsStatCard(
                        value = shown(summary.classes),
                        label = stringResource(R.string.classes),
                        icon = Icons.Default.School,
                        tint = SgrmsColors.Pink,
                        container = SgrmsColors.PinkContainer,
                        onClick = onClassManagementClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            SgrmsSectionTitle(stringResource(R.string.quick_actions))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction(text = stringResource(R.string.add_teacher_2), icon = Icons.Default.PersonAdd, onClick = onAddTeacherClick, modifier = Modifier.weight(1f))
                QuickAction(text = stringResource(R.string.add_student), icon = Icons.Default.PersonAdd, onClick = onAddStudentAccountClick, modifier = Modifier.weight(1f))
            }

            SgrmsGroupedCard {
                NavRow(title = stringResource(R.string.user_management), subtitle = stringResource(R.string.all_login_accounts_and_roles), icon = Icons.Default.ManageAccounts, onClick = onUserManagementClick)
                SgrmsRowDivider()
                NavRow(title = stringResource(R.string.reports), subtitle = stringResource(R.string.student_results_and_pdf_reports), icon = Icons.Default.Summarize, onClick = onReportClick)
            }

            if (uiState.recentAccounts.isNotEmpty()) {
                SgrmsSectionTitle(stringResource(R.string.recent_accounts))
                SgrmsGroupedCard {
                    uiState.recentAccounts.forEachIndexed { index, user ->
                        if (index > 0) SgrmsRowDivider()
                        val role = user.userRole
                        SgrmsListRow(
                            title = user.fullName.ifBlank { user.email },
                            subtitle = user.email,
                            leading = { SgrmsAvatar(name = user.fullName.ifBlank { user.email }, size = 40.dp) },
                            trailing = {
                                SgrmsStatusChip(
                                    text = user.role.ifBlank { stringResource(R.string.user) },
                                    tone = if (role == UserRole.TEACHER) ChipTone.Success else ChipTone.Primary
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Indigo hero card: total accounts, a split bar and the role breakdown. */
@Composable
private fun AccountsHeroCard(
    total: Int,
    admins: Int,
    teachers: Int,
    students: Int,
    classes: Int,
    loaded: Boolean
) {
    val classes3Text = stringResource(R.string.classes_3)
    val t1ClassText = stringResource(R.string.t_1_class)
    Surface(
        shape = RoundedCornerShape(SgrmsRadius.Hero),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.total_accounts), style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE0E7FF))
                    Text(if (loaded) total.toString() else "–", style = MaterialTheme.typography.displayMedium, color = Color.White)
                }
                if (loaded) Text(
                    text = if (classes == 1) t1ClassText else classes3Text.format(classes),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            if (total > 0) {
                Row(modifier = Modifier.fillMaxWidth().height(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (admins > 0) Box(Modifier.weight(admins.toFloat()).fillMaxSize().clip(RoundedCornerShape(4.dp)).background(Color(0xFFFBBF24)))
                    if (teachers > 0) Box(Modifier.weight(teachers.toFloat()).fillMaxSize().clip(RoundedCornerShape(4.dp)).background(Color(0xFFA5B4FC)))
                    if (students > 0) Box(Modifier.weight(students.toFloat()).fillMaxSize().clip(RoundedCornerShape(4.dp)).background(Color.White))
                }
            }

            if (loaded) Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(R.string.admin_2, admins), style = MaterialTheme.typography.bodySmall, color = Color(0xFFE0E7FF))
                Text(stringResource(R.string.teachers_2, teachers), style = MaterialTheme.typography.bodySmall, color = Color(0xFFE0E7FF))
                Text(stringResource(R.string.students_2, students), style = MaterialTheme.typography.bodySmall, color = Color(0xFFE0E7FF))
            }
        }
    }
}

@Composable
private fun QuickAction(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(SgrmsRadius.Card),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.height(56.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SgrmsIconTile(icon = icon, tint = MaterialTheme.colorScheme.primary, container = MaterialTheme.colorScheme.primaryContainer, size = 32.dp)
            Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun NavRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    SgrmsListRow(
        title = title,
        subtitle = subtitle,
        leading = { SgrmsIconTile(icon = icon, tint = MaterialTheme.colorScheme.primary, container = MaterialTheme.colorScheme.primaryContainer) },
        trailing = {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        },
        onClick = onClick
    )
}
