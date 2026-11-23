package com.example.sgrms.ui.admin

import com.example.sgrms.ui.components.OnReturnToScreen
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
import com.example.sgrms.data.model.UserRole
import com.example.sgrms.data.model.userRole
import com.example.sgrms.ui.components.ChipTone
import com.example.sgrms.ui.components.SgrmsAvatar
import com.example.sgrms.ui.components.SgrmsGroupedCard
import com.example.sgrms.ui.components.SgrmsIconTile
import com.example.sgrms.ui.components.SgrmsListRow
import com.example.sgrms.ui.components.SgrmsPageHeader
import com.example.sgrms.ui.components.SgrmsRowDivider
import com.example.sgrms.ui.components.SgrmsSectionTitle
import com.example.sgrms.ui.components.SgrmsStatCard
import com.example.sgrms.ui.components.SgrmsStatusChip
import com.example.sgrms.ui.theme.SgrmsColors
import com.example.sgrms.ui.theme.SgrmsRadius
import com.example.sgrms.ui.theme.SgrmsSpacing
import com.example.sgrms.viewmodel.AdminDashboardViewModel
import java.util.Calendar

/** "Good morning / afternoon / evening" from the phone's clock. */
fun greetingFor(hourOfDay: Int): String = when (hourOfDay) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
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
    val name = adminName.ifBlank { "Admin" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SgrmsPageHeader(
            title = name,
            subtitle = greetingFor(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)),
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
                        label = "Students",
                        icon = Icons.Default.Group,
                        tint = MaterialTheme.colorScheme.primary,
                        container = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onStudentManagementClick,
                        modifier = Modifier.weight(1f)
                    )
                    SgrmsStatCard(
                        value = shown(summary.teachers),
                        label = "Teachers",
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
                        label = "Subjects",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        tint = SgrmsColors.Orange,
                        container = SgrmsColors.OrangeContainer,
                        onClick = onSubjectManagementClick,
                        modifier = Modifier.weight(1f)
                    )
                    SgrmsStatCard(
                        value = shown(summary.classes),
                        label = "Classes",
                        icon = Icons.Default.School,
                        tint = SgrmsColors.Pink,
                        container = SgrmsColors.PinkContainer,
                        onClick = onClassManagementClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            SgrmsSectionTitle("Quick actions")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction(text = "Add teacher", icon = Icons.Default.PersonAdd, onClick = onAddTeacherClick, modifier = Modifier.weight(1f))
                QuickAction(text = "Add student", icon = Icons.Default.PersonAdd, onClick = onAddStudentAccountClick, modifier = Modifier.weight(1f))
            }

            SgrmsGroupedCard {
                NavRow(title = "User management", subtitle = "All login accounts and roles", icon = Icons.Default.ManageAccounts, onClick = onUserManagementClick)
                SgrmsRowDivider()
                NavRow(title = "Reports", subtitle = "Student results and PDF reports", icon = Icons.Default.Summarize, onClick = onReportClick)
            }

            if (uiState.recentAccounts.isNotEmpty()) {
                SgrmsSectionTitle("Recent accounts")
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
                                    text = user.role.ifBlank { "User" },
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
    Surface(
        shape = RoundedCornerShape(SgrmsRadius.Hero),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total accounts", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE0E7FF))
                    Text(if (loaded) total.toString() else "–", style = MaterialTheme.typography.displayMedium, color = Color.White)
                }
                if (loaded) Text(
                    text = if (classes == 1) "1 class" else "$classes classes",
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
                Text("$admins Admin", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE0E7FF))
                Text("$teachers Teachers", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE0E7FF))
                Text("$students Students", style = MaterialTheme.typography.bodySmall, color = Color(0xFFE0E7FF))
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
