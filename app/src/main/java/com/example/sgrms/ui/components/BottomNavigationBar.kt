package com.example.sgrms.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.sgrms.navigation.Screen
import com.example.sgrms.ui.theme.SgrmsRadius

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

val AdminBottomNavItems = listOf(
    BottomNavItem(
        label = "Home",
        route = Screen.AdminDashboard.route,
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        label = "Teachers",
        route = Screen.TeacherManagement.route,
        icon = Icons.Default.SupervisorAccount
    ),
    BottomNavItem(
        label = "Classes",
        route = Screen.ClassManagement.route,
        icon = Icons.Default.School
    ),
    BottomNavItem(
        label = "Students",
        route = Screen.StudentManagement.route,
        icon = Icons.Default.Group
    ),
    BottomNavItem(
        label = "Subjects",
        route = Screen.SubjectManagement.route,
        icon = Icons.AutoMirrored.Filled.MenuBook
    )
)

val TeacherBottomNavItems = listOf(
    BottomNavItem(
        label = "Home",
        route = Screen.TeacherDashboard.route,
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        label = "Students",
        route = Screen.StudentManagement.route,
        icon = Icons.Default.Group
    ),
    BottomNavItem(
        label = "Subjects",
        route = Screen.SubjectManagement.route,
        icon = Icons.AutoMirrored.Filled.MenuBook
    ),
    BottomNavItem(
        label = "Grades",
        route = Screen.GradeManagement.route,
        icon = Icons.Default.Assessment
    ),
    BottomNavItem(
        label = "Reports",
        route = Screen.Report.route,
        icon = Icons.Default.Summarize
    )
)

val StudentBottomNavItems = listOf(
    BottomNavItem(
        label = "Home",
        route = Screen.StudentDashboard.route,
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        label = "Report",
        route = Screen.StudentReportSummary.route,
        icon = Icons.Default.Summarize
    ),
    BottomNavItem(
        label = "Alerts",
        route = Screen.StudentNotifications.route,
        icon = Icons.Default.Notifications
    ),
    BottomNavItem(
        label = "Profile",
        route = Screen.StudentProfile.route,
        icon = Icons.Default.Person
    )
)

/**
 * Floating "pill" navigation bar: white, rounded, soft shadow. The active
 * item sits on a primaryContainer pill. Labels never wrap.
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    items: List<BottomNavItem>,
    onItemClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
        shape = RoundedCornerShape(SgrmsRadius.NavBar),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            onClick = { if (!isSelected) onItemClick(item.route) }
                        )
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(item.icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp))
                    Text(
                        text = item.label,
                        color = contentColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
