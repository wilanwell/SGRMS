package com.example.sgrms.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sgrms.ui.admin.AddTeacherScreen
import com.example.sgrms.ui.admin.AdminDashboardScreen
import com.example.sgrms.ui.admin.ClassDetailScreen
import com.example.sgrms.ui.admin.ClassManagementScreen
import com.example.sgrms.ui.admin.EditTeacherScreen
import com.example.sgrms.ui.admin.TeacherDetailScreen
import com.example.sgrms.ui.admin.TeacherManagementScreen
import com.example.sgrms.ui.admin.UserManagementScreen
import com.example.sgrms.ui.components.AdminBottomNavItems
import com.example.sgrms.ui.components.BottomNavigationBar
import com.example.sgrms.ui.components.StudentBottomNavItems
import com.example.sgrms.ui.components.TeacherBottomNavItems
import com.example.sgrms.ui.dashboard.StudentDashboardScreen
import com.example.sgrms.ui.dashboard.TeacherDashboardScreen
import com.example.sgrms.ui.grade.AddGradeScreen
import com.example.sgrms.ui.grade.AddStudentGradeScreen
import com.example.sgrms.ui.grade.EditGradeScreen
import com.example.sgrms.ui.grade.GradeManagementScreen
import com.example.sgrms.ui.grade.QuickAddGradeScreen
import com.example.sgrms.ui.grade.StudentGradeDetailScreen
import com.example.sgrms.ui.login.LoginScreen
import com.example.sgrms.ui.notification.StudentNotificationScreen
import com.example.sgrms.ui.profile.AdminProfileScreen
import com.example.sgrms.ui.profile.ChangePasswordScreen
import com.example.sgrms.ui.profile.EditAdminProfileScreen
import com.example.sgrms.ui.profile.EditStudentProfileScreen
import com.example.sgrms.ui.profile.EditTeacherProfileScreen
import com.example.sgrms.ui.profile.StudentProfileScreen
import com.example.sgrms.ui.profile.TeacherProfileScreen
import com.example.sgrms.ui.report.ReportDetailScreen
import com.example.sgrms.ui.report.ReportScreen
import com.example.sgrms.ui.report.StudentReportSummaryScreen
import com.example.sgrms.ui.report.UpdateReportScreen
import com.example.sgrms.ui.student.AddStudentScreen
import com.example.sgrms.ui.student.EditStudentScreen
import com.example.sgrms.ui.student.StudentDetailScreen
import com.example.sgrms.ui.student.StudentManagementScreen
import com.example.sgrms.ui.subject.AddSubjectScreen
import com.example.sgrms.ui.subject.EditSubjectScreen
import com.example.sgrms.ui.subject.SubjectDetailScreen
import com.example.sgrms.ui.subject.SubjectManagementScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.example.sgrms.data.model.UserModel
import com.example.sgrms.data.model.UserRole
import com.example.sgrms.data.session.SessionState
import com.example.sgrms.viewmodel.AuthViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.example.sgrms.ui.theme.ColorBackground

private val AdminOnly = setOf(UserRole.ADMIN)
private val TeacherOnly = setOf(UserRole.TEACHER)
private val StudentOnly = setOf(UserRole.STUDENT)
private val StaffOnly = setOf(UserRole.ADMIN, UserRole.TEACHER)
private val AnyRole = UserRole.entries.toSet()

private fun homeRouteFor(role: UserRole): String = when (role) {
    UserRole.ADMIN -> Screen.AdminDashboard.route
    UserRole.TEACHER -> Screen.TeacherDashboard.route
    UserRole.STUDENT -> Screen.StudentDashboard.route
}

private fun NavHostController.navigateClearingBackStack(route: String) {
    navigate(route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val session by authViewModel.session.collectAsState()

    val loggedIn = session as? SessionState.LoggedIn
    val loggedInUser: UserModel? = loggedIn?.user
    val currentRole: UserRole? = loggedIn?.role
    val currentStudentId = loggedInUser?.studentId.orEmpty()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Route the app whenever the session changes:
    // restored session -> home, login -> home, logout -> login.
    LaunchedEffect(session) {
        when (val state = session) {
            SessionState.Loading -> Unit

            SessionState.LoggedOut -> {
                if (navController.currentDestination?.route != Screen.Login.route) {
                    navController.navigateClearingBackStack(Screen.Login.route)
                }
            }

            is SessionState.LoggedIn -> {
                val route = navController.currentDestination?.route
                if (route == Screen.Splash.route || route == Screen.Login.route) {
                    navController.navigateClearingBackStack(homeRouteFor(state.role))
                }
            }
        }
    }

    val adminBottomBarRoutes = listOf(
        Screen.AdminDashboard.route,
        Screen.UserManagement.route,
        Screen.TeacherManagement.route,
        Screen.ClassManagement.route,
        Screen.StudentManagement.route,
        Screen.SubjectManagement.route,
        Screen.AdminProfile.route
    )

    val teacherBottomBarRoutes = listOf(
        Screen.TeacherDashboard.route,
        Screen.StudentManagement.route,
        Screen.SubjectManagement.route,
        Screen.GradeManagement.route,
        Screen.Report.route,
        Screen.TeacherProfile.route
    )

    val studentBottomBarRoutes = listOf(
        Screen.StudentDashboard.route,
        Screen.StudentReportSummary.route,
        Screen.StudentNotifications.route,
        Screen.StudentProfile.route
    )

    val showBottomBar = when (currentRole) {
        UserRole.ADMIN -> currentRoute in adminBottomBarRoutes
        UserRole.STUDENT -> currentRoute in studentBottomBarRoutes
        UserRole.TEACHER -> currentRoute in teacherBottomBarRoutes
        null -> false
    }

    /**
     * A destination that only [allowedRoles] may open. Anyone else (for
     * example a student reaching an admin route) is sent to their own home.
     */
    fun NavGraphBuilder.guardedComposable(
        allowedRoles: Set<UserRole>,
        route: String,
        arguments: List<NamedNavArgument> = emptyList(),
        content: @Composable (NavBackStackEntry) -> Unit
    ) {
        composable(route = route, arguments = arguments) { entry ->
            val role = (session as? SessionState.LoggedIn)?.role

            if (role != null && role in allowedRoles) {
                content(entry)
            } else {
                LaunchedEffect(role) {
                    navController.navigateClearingBackStack(
                        role?.let(::homeRouteFor) ?: Screen.Login.route
                    )
                }
            }
        }
    }

    Scaffold(
        // Keep content clear of the navigation bar / camera cutout at the
        // sides in landscape; top and bottom insets are handled per screen.
        modifier = Modifier
            .background(ColorBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                val bottomItems = when (currentRole) {
                    UserRole.ADMIN -> AdminBottomNavItems
                    UserRole.STUDENT -> StudentBottomNavItems
                    else -> TeacherBottomNavItems
                }

                BottomNavigationBar(
                    currentRoute = currentRoute,
                    items = bottomItems,
                    onItemClick = { route ->
                        val target = when {
                            currentRole == UserRole.STUDENT &&
                                route == Screen.StudentReportSummary.route ->
                                Screen.StudentReportSummary.createRoute(currentStudentId)

                            currentRole == UserRole.STUDENT &&
                                route == Screen.StudentNotifications.route ->
                                Screen.StudentNotifications.createRoute(currentStudentId)

                            else -> route
                        }

                        navController.navigate(target) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Splash.route) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            composable(Screen.Login.route) {
                LoginScreen()
            }

            guardedComposable(allowedRoles = AdminOnly, route = Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    adminName = loggedInUser?.fullName.orEmpty(),
                    onUserManagementClick = {
                        navController.navigate(Screen.UserManagement.route)
                    },
                    onAddTeacherClick = {
                        navController.navigate(Screen.AddTeacher.route)
                    },
                    onAddStudentAccountClick = {
                        navController.navigate(Screen.AddStudent.route)
                    },
                    onStudentManagementClick = {
                        navController.navigate(Screen.StudentManagement.route)
                    },
                    onSubjectManagementClick = {
                        navController.navigate(Screen.SubjectManagement.route)
                    },
                    onClassManagementClick = {
                        navController.navigate(Screen.ClassManagement.route)
                    },
                    onReportClick = {
                        navController.navigate(Screen.Report.route)
                    },
                    onProfileClick = {
                        navController.navigate(Screen.AdminProfile.route)
                    }
                )
            }

            guardedComposable(allowedRoles = AdminOnly, route = Screen.AdminProfile.route) {
                AdminProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onEditProfileClick = {
                        navController.navigate(Screen.EditAdminProfile.route)
                    },
                    onChangePasswordClick = {
                        navController.navigate(Screen.ChangePassword.route)
                    },
                    onLogoutClick = {
                        authViewModel.logout()
                    }
                )
            }

            guardedComposable(allowedRoles = AdminOnly, route = Screen.EditAdminProfile.route) {
                EditAdminProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(allowedRoles = AdminOnly, route = Screen.UserManagement.route) {
                UserManagementScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddTeacherClick = {
                        navController.navigate(Screen.AddTeacher.route)
                    },
                    onAddStudentAccountClick = {
                        navController.navigate(Screen.AddStudent.route)
                    },
                    onEditUserClick = { teacherId ->
                        navController.navigate(
                            Screen.EditTeacher.createRoute(teacherId)
                        )
                    }
                )
            }

            guardedComposable(allowedRoles = AdminOnly, route = Screen.TeacherManagement.route) {
                TeacherManagementScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddTeacherClick = {
                        navController.navigate(Screen.AddTeacher.route)
                    },
                    onViewTeacherClick = { teacherId ->
                        navController.navigate(
                            Screen.TeacherDetail.createRoute(teacherId)
                        )
                    },
                    onEditTeacherClick = { teacherId ->
                        navController.navigate(
                            Screen.EditTeacher.createRoute(teacherId)
                        )
                    }
                )
            }

            guardedComposable(allowedRoles = AdminOnly, route = Screen.AddTeacher.route) {
                AddTeacherScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(
                allowedRoles = AdminOnly,
                route = Screen.TeacherDetail.route,
                arguments = listOf(
                    navArgument("teacherId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""

                TeacherDetailScreen(
                    teacherId = teacherId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onEditClick = {
                        navController.navigate(
                            Screen.EditTeacher.createRoute(teacherId)
                        )
                    }
                )
            }

            guardedComposable(
                allowedRoles = AdminOnly,
                route = Screen.EditTeacher.route,
                arguments = listOf(
                    navArgument("teacherId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val teacherId = backStackEntry.arguments?.getString("teacherId") ?: ""

                EditTeacherScreen(
                    teacherId = teacherId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(allowedRoles = AdminOnly, route = Screen.ClassManagement.route) {
                ClassManagementScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onViewClassClick = { classId ->
                        navController.navigate(
                            Screen.ClassDetail.createRoute(classId)
                        )
                    },
                    onAddStudentClick = {
                        navController.navigate(Screen.AddStudent.route)
                    }
                )
            }

            guardedComposable(
                allowedRoles = AdminOnly,
                route = Screen.ClassDetail.route,
                arguments = listOf(
                    navArgument("classId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val classId = backStackEntry.arguments?.getString("classId") ?: ""

                ClassDetailScreen(
                    classId = classId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddStudentClick = {
                        navController.navigate(Screen.AddStudent.route)
                    }
                )
            }

            guardedComposable(allowedRoles = TeacherOnly, route = Screen.TeacherDashboard.route) {
                TeacherDashboardScreen(
                    onStudentClick = {
                        navController.navigate(Screen.StudentManagement.route)
                    },
                    onSubjectClick = {
                        navController.navigate(Screen.SubjectManagement.route)
                    },
                    onGradeClick = {
                        navController.navigate(Screen.GradeManagement.route)
                    },
                    onReportClick = {
                        navController.navigate(Screen.Report.route)
                    },
                    onProfileClick = {
                        navController.navigate(Screen.TeacherProfile.route)
                    }
                )
            }

            guardedComposable(allowedRoles = StudentOnly, route = Screen.StudentDashboard.route) {
                StudentDashboardScreen(
                    studentId = currentStudentId,
                    onProfileClick = {
                        navController.navigate(Screen.StudentProfile.route)
                    },
                    onReportClick = {
                        navController.navigate(
                            Screen.StudentReportSummary.createRoute(currentStudentId)
                        )
                    }
                )
            }

            guardedComposable(allowedRoles = StaffOnly, route = Screen.StudentManagement.route) {
                StudentManagementScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddStudentClick = {
                        navController.navigate(Screen.AddStudent.route)
                    },
                    canAddStudent = currentRole == UserRole.ADMIN,
                    onStudentDetailClick = { studentId ->
                        navController.navigate(
                            Screen.StudentDetail.createRoute(studentId)
                        )
                    },
                    onEditStudentClick = { studentId ->
                        navController.navigate(
                            Screen.EditStudent.createRoute(studentId)
                        )
                    }
                )
            }

            guardedComposable(allowedRoles = AdminOnly, route = Screen.AddStudent.route) {
                AddStudentScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(
                allowedRoles = StaffOnly,
                route = Screen.StudentDetail.route,
                arguments = listOf(
                    navArgument("studentId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId") ?: ""

                StudentDetailScreen(
                    studentId = studentId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onEditClick = {
                        navController.navigate(
                            Screen.EditStudent.createRoute(studentId)
                        )
                    },
                    onDeleteClick = {
                        navController.popBackStack()
                    },
                    onViewGradeClick = {
                        navController.navigate(
                            Screen.StudentGradeDetail.createRoute(studentId)
                        )
                    }
                )
            }

            guardedComposable(
                allowedRoles = StaffOnly,
                route = Screen.EditStudent.route,
                arguments = listOf(
                    navArgument("studentId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId") ?: ""

                EditStudentScreen(
                    studentId = studentId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(allowedRoles = StaffOnly, route = Screen.SubjectManagement.route) {
                SubjectManagementScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddSubjectClick = {
                        navController.navigate(Screen.AddSubject.route)
                    },
                    onSubjectDetailClick = { subjectId ->
                        navController.navigate(
                            Screen.SubjectDetail.createRoute(subjectId)
                        )
                    },
                    onEditSubjectClick = { subjectId ->
                        navController.navigate(
                            Screen.EditSubject.createRoute(subjectId)
                        )
                    }
                )
            }

            guardedComposable(allowedRoles = StaffOnly, route = Screen.AddSubject.route) {
                AddSubjectScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(
                allowedRoles = StaffOnly,
                route = Screen.SubjectDetail.route,
                arguments = listOf(
                    navArgument("subjectId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""

                SubjectDetailScreen(
                    subjectId = subjectId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onEditClick = {
                        navController.navigate(
                            Screen.EditSubject.createRoute(subjectId)
                        )
                    },
                    onViewStudentsClick = {
                        navController.navigate(Screen.StudentManagement.route)
                    }
                )
            }

            guardedComposable(
                allowedRoles = StaffOnly,
                route = Screen.EditSubject.route,
                arguments = listOf(
                    navArgument("subjectId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId") ?: ""

                EditSubjectScreen(
                    subjectId = subjectId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(allowedRoles = StaffOnly, route = Screen.GradeManagement.route) {
                GradeManagementScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddGradeClick = {
                        navController.navigate(Screen.QuickAddGrade.route)
                    },
                    onStudentDetailClick = { studentId ->
                        navController.navigate(
                            Screen.StudentGradeDetail.createRoute(studentId)
                        )
                    },
                    onEditGradeClick = { gradeId ->
                        navController.navigate(
                            Screen.EditGrade.createRoute(gradeId)
                        )
                    }
                )
            }

            guardedComposable(allowedRoles = StaffOnly, route = Screen.AddGrade.route) {
                AddGradeScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(
                allowedRoles = StaffOnly,
                route = Screen.AddStudentGrade.route,
                arguments = listOf(
                    navArgument("studentId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId") ?: ""

                AddStudentGradeScreen(
                    studentId = studentId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(allowedRoles = StaffOnly, route = Screen.QuickAddGrade.route) {
                QuickAddGradeScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(
                allowedRoles = StaffOnly,
                route = Screen.EditGrade.route,
                arguments = listOf(
                    navArgument("gradeId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val gradeId = backStackEntry.arguments?.getString("gradeId") ?: ""

                EditGradeScreen(
                    gradeId = gradeId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(
                allowedRoles = StaffOnly,
                route = Screen.StudentGradeDetail.route,
                arguments = listOf(
                    navArgument("studentId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId") ?: ""

                StudentGradeDetailScreen(
                    studentId = studentId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddGradeClick = {
                        navController.navigate(
                            Screen.AddStudentGrade.createRoute(studentId)
                        )
                    },
                    onGenerateReportClick = {
                        navController.navigate(Screen.Report.route)
                    }
                )
            }

            guardedComposable(allowedRoles = StaffOnly, route = Screen.Report.route) {
                ReportScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onViewReportDetailClick = { reportId ->
                        navController.navigate(
                            Screen.ReportDetail.createRoute(reportId)
                        )
                    },
                    onDownloadPdfClick = { _ ->
                        // PDF is downloaded directly inside ReportScreen.
                    },
                    onUpdateReportClick = { reportId ->
                        navController.navigate(
                            Screen.UpdateReport.createRoute(reportId)
                        )
                    }
                )
            }

            guardedComposable(
                allowedRoles = StaffOnly,
                route = Screen.ReportDetail.route,
                arguments = listOf(
                    navArgument("reportId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId") ?: ""

                ReportDetailScreen(
                    reportId = reportId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveReportClick = {
                        navController.popBackStack()
                    },
                    onUpdateReportClick = {
                        navController.navigate(
                            Screen.UpdateReport.createRoute(reportId)
                        )
                    }
                )
            }

            guardedComposable(
                allowedRoles = StaffOnly,
                route = Screen.UpdateReport.route,
                arguments = listOf(
                    navArgument("reportId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val reportId = backStackEntry.arguments?.getString("reportId") ?: ""

                UpdateReportScreen(
                    reportId = reportId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(
                allowedRoles = StudentOnly,
                route = Screen.StudentReportSummary.route,
                arguments = listOf(
                    navArgument("studentId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val studentId = if (currentRole == UserRole.STUDENT) {
                    currentStudentId
                } else {
                    backStackEntry.arguments?.getString("studentId").orEmpty()
                }

                StudentReportSummaryScreen(
                    studentId = studentId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(
                allowedRoles = StudentOnly,
                route = Screen.StudentNotifications.route,
                arguments = listOf(
                    navArgument("studentId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val studentId = if (currentRole == UserRole.STUDENT) {
                    currentStudentId
                } else {
                    backStackEntry.arguments?.getString("studentId").orEmpty()
                }

                StudentNotificationScreen(
                    studentId = studentId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(allowedRoles = TeacherOnly, route = Screen.TeacherProfile.route) {
                TeacherProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onEditProfileClick = {
                        navController.navigate(Screen.EditTeacherProfile.route)
                    },
                    onChangePasswordClick = {
                        navController.navigate(Screen.ChangePassword.route)
                    },
                    onLogoutClick = {
                        authViewModel.logout()
                    }
                )
            }

            guardedComposable(allowedRoles = TeacherOnly, route = Screen.EditTeacherProfile.route) {
                EditTeacherProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(allowedRoles = StudentOnly, route = Screen.StudentProfile.route) {
                StudentProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onEditProfileClick = {
                        navController.navigate(Screen.EditStudentProfile.route)
                    },
                    onChangePasswordClick = {
                        navController.navigate(Screen.ChangePassword.route)
                    },
                    onLogoutClick = {
                        authViewModel.logout()
                    }
                )
            }

            guardedComposable(allowedRoles = StudentOnly, route = Screen.EditStudentProfile.route) {
                EditStudentProfileScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            guardedComposable(allowedRoles = AnyRole, route = Screen.ChangePassword.route) {
                ChangePasswordScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
