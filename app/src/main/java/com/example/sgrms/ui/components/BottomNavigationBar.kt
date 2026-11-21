package com.example.sgrms.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sgrms.navigation.Screen
import com.example.sgrms.ui.theme.ColorSurface
import com.example.sgrms.ui.theme.ColorPrimary
import com.example.sgrms.ui.theme.ColorOnSurfaceVariant

data class BottomNavItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

val AdminBottomNavItems = listOf(
    BottomNavItem(
        label = "Dashboard",
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
        icon = Icons.Default.MenuBook
    )
)

val TeacherBottomNavItems = listOf(
    BottomNavItem(
        label = "Dashboard",
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
        icon = Icons.Default.MenuBook
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
        label = "Dashboard",
        route = Screen.StudentDashboard.route,
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        label = "My Reports",
        route = Screen.StudentReportSummary.route,
        icon = Icons.Default.Summarize
    ),
    BottomNavItem(
        label = "Notifications",
        route = Screen.StudentNotifications.route,
        icon = Icons.Default.Notifications
    ),
    BottomNavItem(
        label = "Profile",
        route = Screen.StudentProfile.route,
        icon = Icons.Default.Person
    )
)

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    items: List<BottomNavItem>,
    onItemClick: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val itemCount = items.size

    val isSmallScreen = screenWidthDp < 380
    val isVerySmallScreen = screenWidthDp < 340
    val hasManyItems = itemCount >= 5

    val iconSize = when {
        isVerySmallScreen && hasManyItems -> 18.dp
        isVerySmallScreen -> 20.dp
        isSmallScreen && hasManyItems -> 20.dp
        isSmallScreen -> 22.dp
        hasManyItems -> 22.dp
        else -> 24.dp
    }

    val fontSize = when {
        isVerySmallScreen && hasManyItems -> 8.sp
        isVerySmallScreen -> 9.sp
        isSmallScreen && hasManyItems -> 9.sp
        isSmallScreen -> 10.sp
        hasManyItems -> 10.sp
        else -> 11.sp
    }

    NavigationBar(
        containerColor = ColorSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onItemClick(item.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(iconSize)
                    )
                },
                label = {
                    Text(
                        text = getAdaptiveBottomNavLabel(
                            label = item.label,
                            isSmallScreen = isSmallScreen,
                            isVerySmallScreen = isVerySmallScreen,
                            hasManyItems = hasManyItems
                        ),
                        fontSize = fontSize,
                        fontWeight = if (isSelected) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ColorPrimary,
                    selectedTextColor = ColorPrimary,
                    unselectedIconColor = ColorOnSurfaceVariant,
                    unselectedTextColor = ColorOnSurfaceVariant,
                    indicatorColor = ColorPrimary.copy(alpha = 0.12f)
                )
            )
        }
    }
}

private fun getAdaptiveBottomNavLabel(
    label: String,
    isSmallScreen: Boolean,
    isVerySmallScreen: Boolean,
    hasManyItems: Boolean
): String {
    return when {
        isVerySmallScreen && hasManyItems -> {
            when (label) {
                "Dashboard" -> "Home"
                "Teachers" -> "Teach"
                "Students" -> "Stud"
                "Subjects" -> "Subj"
                "Classes" -> "Class"
                "Reports" -> "Rpt"
                "My Reports" -> "Report"
                "Notifications" -> "Notif"
                "Profile" -> "Me"
                "Grades" -> "Grade"
                else -> label
            }
        }

        isSmallScreen && hasManyItems -> {
            when (label) {
                "Dashboard" -> "Home"
                "Notifications" -> "Notif"
                "My Reports" -> "Report"
                else -> label
            }
        }

        else -> label
    }
}