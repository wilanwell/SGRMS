package com.wilanwell.sgrms.ui.dashboard

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wilanwell.sgrms.ui.admin.greetingFor
import com.wilanwell.sgrms.ui.components.SgrmsAvatar
import com.wilanwell.sgrms.ui.components.SgrmsEmptyState
import com.wilanwell.sgrms.ui.components.SgrmsGroupedCard
import com.wilanwell.sgrms.ui.components.SgrmsIconTile
import com.wilanwell.sgrms.ui.components.SgrmsPageHeader
import com.wilanwell.sgrms.ui.components.SgrmsRowDivider
import com.wilanwell.sgrms.ui.components.SgrmsSectionTitle
import com.wilanwell.sgrms.ui.components.SgrmsStatCard
import com.wilanwell.sgrms.ui.theme.SgrmsColors
import com.wilanwell.sgrms.ui.theme.SgrmsRadius
import com.wilanwell.sgrms.ui.theme.SgrmsSpacing
import com.wilanwell.sgrms.ui.theme.markColor
import com.wilanwell.sgrms.viewmodel.TeacherDashboardViewModel
import java.util.Calendar

@Composable
fun TeacherDashboardScreen(
    onStudentClick: () -> Unit = {},
    onSubjectClick: () -> Unit = {},
    onGradeClick: () -> Unit = {},
    onReportClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val acrossSubjectsGradeRecordsText = stringResource(R.string.across_subjects_grade_records)
    val viewModel: TeacherDashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    OnReturnToScreen { viewModel.load() }
    val shown: (Int) -> String = { if (uiState.loaded) it.toString() else "–" }
    val summary = uiState.summary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SgrmsPageHeader(
            title = uiState.teacherName,
            subtitle = stringResource(greetingFor(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))),
            trailing = {
                IconButton(onClick = onProfileClick) {
                    SgrmsAvatar(name = uiState.teacherName, size = 44.dp, imageUrl = uiState.teacherImageUrl, legacyImageBase64 = uiState.teacherImageBase64)
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
            if (uiState.errorMessage.isNotBlank()) {
                Text(uiState.errorMessage, color = SgrmsColors.Error, style = MaterialTheme.typography.bodyMedium)
            }

            Surface(
                shape = RoundedCornerShape(SgrmsRadius.Hero),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.students_you_teach), style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE0E7FF))
                    Text(shown(summary.students), style = MaterialTheme.typography.displayMedium, color = Color.White)
                    Text(
                        text = if (uiState.loaded) acrossSubjectsGradeRecordsText.format(summary.subjects, summary.grades) else "Loading…",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0E7FF)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SgrmsStatCard(
                        value = shown(summary.students), label = stringResource(R.string.students), icon = Icons.Default.Group,
                        tint = MaterialTheme.colorScheme.primary, container = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onStudentClick, modifier = Modifier.weight(1f)
                    )
                    SgrmsStatCard(
                        value = shown(summary.subjects), label = stringResource(R.string.subjects), icon = Icons.AutoMirrored.Filled.MenuBook,
                        tint = SgrmsColors.Orange, container = SgrmsColors.OrangeContainer,
                        onClick = onSubjectClick, modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SgrmsStatCard(
                        value = shown(summary.grades), label = stringResource(R.string.grades), icon = Icons.Default.Assessment,
                        tint = SgrmsColors.Success, container = SgrmsColors.SuccessContainer,
                        onClick = onGradeClick, modifier = Modifier.weight(1f)
                    )
                    SgrmsStatCard(
                        value = shown(summary.reports), label = stringResource(R.string.reports), icon = Icons.Default.Summarize,
                        tint = SgrmsColors.Pink, container = SgrmsColors.PinkContainer,
                        onClick = onReportClick, modifier = Modifier.weight(1f)
                    )
                }
            }

            SgrmsSectionTitle(stringResource(R.string.quick_actions))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction(stringResource(R.string.add_grades), Icons.Default.EditNote, onGradeClick, Modifier.weight(1f))
                QuickAction(stringResource(R.string.reports), Icons.Default.Summarize, onReportClick, Modifier.weight(1f))
            }

            SgrmsSectionTitle(stringResource(R.string.subject_averages))
            when {
                uiState.isLoading && summary.subjectAverages.isEmpty() ->
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }

                summary.subjectAverages.isEmpty() -> SgrmsEmptyState(
                    title = stringResource(R.string.no_averages_yet),
                    message = stringResource(R.string.add_subjects_and_grades_to_see),
                    icon = Icons.Default.Assessment
                )

                else -> SgrmsGroupedCard {
                    summary.subjectAverages.forEachIndexed { index, item ->
                        if (index > 0) SgrmsRowDivider()
                        Column(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row {
                                Text(
                                    item.subjectName.ifBlank { stringResource(R.string.unnamed_subject) },
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text("%.1f%%".format(item.averageMarks), style = MaterialTheme.typography.titleSmall)
                            }
                            LinearProgressIndicator(
                                progress = { (item.averageMarks / 100.0).toFloat().coerceIn(0f, 1f) },
                                color = markColor(item.averageMarks),
                                trackColor = MaterialTheme.colorScheme.outlineVariant,
                                strokeCap = StrokeCap.Round,
                                gapSize = 0.dp,
                                drawStopIndicator = {},
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
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
