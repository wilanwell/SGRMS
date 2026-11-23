package com.wilanwell.sgrms.ui.dashboard

import com.wilanwell.sgrms.ui.components.rememberStrings
import com.wilanwell.sgrms.utils.NotificationText
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.components.SgrmsEmptyState
import com.wilanwell.sgrms.ui.components.SgrmsGroupedCard
import com.wilanwell.sgrms.ui.components.SgrmsIconTile
import com.wilanwell.sgrms.ui.components.SgrmsPageHeader
import com.wilanwell.sgrms.ui.components.SgrmsProgressBar
import com.wilanwell.sgrms.ui.components.SgrmsRowDivider
import com.wilanwell.sgrms.ui.components.SgrmsSectionTitle
import com.wilanwell.sgrms.ui.components.SgrmsStatusChip
import com.wilanwell.sgrms.ui.components.toneForGrade
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.theme.SgrmsRadius
import com.wilanwell.sgrms.ui.theme.SgrmsSpacing
import com.wilanwell.sgrms.ui.theme.markColor
import com.wilanwell.sgrms.utils.GradeCalculator
import com.wilanwell.sgrms.viewmodel.GradeFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.NotificationFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.StudentFirebaseViewModel
import com.wilanwell.sgrms.viewmodel.SubjectFirebaseViewModel

@Composable
fun StudentDashboardScreen(
    studentId: String,
    onProfileClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {}
) {
    val strings = rememberStrings()
    val notificationsUnreadText = stringResource(R.string.notifications_unread)
    val studentViewModel: StudentFirebaseViewModel = hiltViewModel()
    val gradeViewModel: GradeFirebaseViewModel = hiltViewModel()
    val subjectViewModel: SubjectFirebaseViewModel = hiltViewModel()
    val notificationViewModel: NotificationFirebaseViewModel = hiltViewModel()

    val studentUiState by studentViewModel.uiState.collectAsState()
    val gradeUiState by gradeViewModel.uiState.collectAsState()
    val subjectUiState by subjectViewModel.uiState.collectAsState()
    val notificationUiState by notificationViewModel.uiState.collectAsState()

    val loadAll = {
        if (studentId.isNotBlank()) {
            studentViewModel.loadStudentById(studentId)
            gradeViewModel.loadGradesByStudentId(studentId)
            subjectViewModel.loadSubjects()
            notificationViewModel.loadNotificationsByStudentId(studentId)
        }
    }

    LaunchedEffect(studentId) { loadAll() }
    OnReturnToScreen { loadAll() }

    val student = studentUiState.selectedStudent
    val firstName = student?.studentName?.trim()?.substringBefore(' ')?.ifBlank { null } ?: stringResource(R.string.student)
    val summary = studentGradeSummary(gradeUiState.grades, subjectUiState.subjects)
    val unread = notificationUiState.notifications.filter { !it.isRead }
    val latestUnread = unread.maxByOrNull { it.createdAt }
    val isLoading = studentUiState.isLoading || gradeUiState.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SgrmsPageHeader(
            title = stringResource(R.string.hi, firstName),
            subtitle = listOfNotNull(
                student?.className?.ifBlank { null },
                student?.studentCode?.ifBlank { null }
            ).joinToString(" · ").ifBlank { stringResource(R.string.student) },
            trailing = {
                Box {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = if (unread.isEmpty()) "Notifications" else notificationsUnreadText.format(unread.size),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (unread.isNotEmpty()) {
                        Box(
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 10.dp)
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(SgrmsColors.WarningBar)
                        )
                    }
                }
                IconButton(onClick = onProfileClick) {
                    com.wilanwell.sgrms.ui.components.SgrmsAvatar(
                        name = student?.studentName ?: stringResource(R.string.student),
                        size = 40.dp,
                        imageUrl = student?.profileImageUrl.orEmpty(),
                        legacyImageBase64 = student?.profileImageBase64.orEmpty()
                    )
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
            OverallCard(
                averageMarks = summary.averageMarks,
                overallGrade = summary.overallGrade,
                gradedCount = summary.gradedCount,
                totalCount = summary.totalCount
            )

            if (latestUnread != null) {
                Surface(
                    onClick = onNotificationsClick,
                    shape = RoundedCornerShape(SgrmsRadius.Card),
                    color = SgrmsColors.OrangeContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SgrmsIconTile(icon = Icons.Default.Notifications, tint = SgrmsColors.Orange, container = androidx.compose.ui.graphics.Color(0xFFFFEDD5))
                        Column(Modifier.weight(1f)) {
                            Text(NotificationText.title(latestUnread, strings), style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                NotificationText.body(latestUnread, strings),
                                style = MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color(0xFF9A3412),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            SgrmsSectionTitle(stringResource(R.string.my_subjects)) {
                TextButton(onClick = onReportClick) { Text(stringResource(R.string.full_report), fontWeight = FontWeight.SemiBold) }
            }

            when {
                isLoading && summary.items.isEmpty() -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                summary.items.isEmpty() -> SgrmsEmptyState(
                    title = stringResource(R.string.no_grades_yet),
                    message = stringResource(R.string.your_grades_will_appear_here_after),
                    icon = Icons.Default.Assessment
                )

                else -> SgrmsGroupedCard {
                    summary.items.forEachIndexed { index, item ->
                        if (index > 0) SgrmsRowDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row {
                                    Text(
                                        item.subjectName.ifBlank { stringResource(R.string.unknown_subject) },
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text("%.0f".format(item.marks), style = MaterialTheme.typography.titleSmall)
                                }
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress = { (item.marks / 100.0).toFloat().coerceIn(0f, 1f) },
                                    color = markColor(item.marks),
                                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                                    strokeCap = StrokeCap.Round,
                                    gapSize = 0.dp,
                                    drawStopIndicator = {},
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(3.dp))
                                )
                            }
                            SgrmsStatusChip(text = item.gradeLetter.ifBlank { "-" }, tone = toneForGrade(item.gradeLetter))
                        }
                    }
                }
            }
        }
    }
}

/** White card with a ring showing the average and the overall grade in the middle. */
@Composable
private fun OverallCard(
    averageMarks: Double,
    overallGrade: String,
    gradedCount: Int,
    totalCount: Int
) {
    val noGradesYetText = stringResource(R.string.no_grades_yet)
    Surface(
        shape = RoundedCornerShape(SgrmsRadius.Hero),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(96.dp)) {
                androidx.compose.material3.CircularProgressIndicator(
                    progress = { (averageMarks / 100.0).toFloat().coerceIn(0f, 1f) },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    modifier = Modifier.size(96.dp)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(overallGrade, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    if (gradedCount > 0) {
                        Text("%.1f%%".format(averageMarks), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.overall_average), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (gradedCount > 0) stringResource(GradeCalculator.performanceStatus(averageMarks, hasGrades = true)) else noGradesYetText,
                    style = MaterialTheme.typography.titleLarge
                )
                if (totalCount > 0) {
                    SgrmsStatusChip(
                        text = stringResource(R.string.of_subjects_graded, gradedCount, totalCount),
                        tone = if (gradedCount == totalCount) com.wilanwell.sgrms.ui.components.ChipTone.Success else com.wilanwell.sgrms.ui.components.ChipTone.Warning
                    )
                }
            }
        }
    }
}
