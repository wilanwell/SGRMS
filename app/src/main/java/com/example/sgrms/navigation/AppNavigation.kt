package com.example.sgrms.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.sgrms.utils.FcmTokenHelper
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    var currentFirebaseUserId by rememberSaveable { mutableStateOf("") }

    var currentUserId by rememberSaveable { mutableIntStateOf(0) }
    var currentUserRole by rememberSaveable { mutableStateOf("") }
    var currentUserName by rememberSaveable { mutableStateOf("") }

    var currentStudentId by rememberSaveable { mutableIntStateOf(0) }
    var currentTeacherId by rememberSaveable { mutableIntStateOf(0) }

    var currentFirebaseStudentId by rememberSaveable { mutableStateOf("") }
    var currentFirebaseTeacherId by rememberSaveable { mutableStateOf("") }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

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

    val showBottomBar = when (currentUserRole) {
        "Admin" -> currentRoute in adminBottomBarRoutes
        "Student" -> currentRoute in studentBottomBarRoutes
        "Teacher" -> currentRoute in teacherBottomBarRoutes
        else -> false
    }

    fun clearCurrentUserSession() {
        FirebaseAuth.getInstance().signOut()

        currentFirebaseUserId = ""
        currentUserId = 0
        currentUserRole = ""
        currentUserName = ""

        currentStudentId = 0
        currentTeacherId = 0

        currentFirebaseStudentId = ""
        currentFirebaseTeacherId = ""

        navController.navigate(Screen.Login.route) {
            popUpTo(0)
        }
    }

    fun getCurrentStudentRouteId(): String {
        return currentFirebaseStudentId.ifBlank {
            currentStudentId.toString()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (showBottomBar) {
                val bottomItems = when (currentUserRole) {
                    "Admin" -> AdminBottomNavItems
                    "Student" -> StudentBottomNavItems
                    else -> TeacherBottomNavItems
                }

                BottomNavigationBar(
                    currentRoute = currentRoute,
                    items = bottomItems,
                    onItemClick = { route ->
                        if (
                            currentUserRole == "Student" &&
                            route == Screen.StudentReportSummary.route
                        ) {
                            navController.navigate(
                                Screen.StudentReportSummary.createRoute(getCurrentStudentRouteId())
                            ) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else if (
                            currentUserRole == "Student" &&
                            route == Screen.StudentNotifications.route
                        ) {
                            navController.navigate(
                                Screen.StudentNotifications.createRoute(getCurrentStudentRouteId())
                            ) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginClick = { user ->
                        currentFirebaseUserId = user.userId
                        currentUserRole = user.role
                        currentUserName = user.fullName

                        currentFirebaseStudentId = user.studentId
                        currentFirebaseTeacherId = user.teacherId

                        currentUserId = 0
                        currentStudentId = user.studentId.toIntOrNull() ?: 0
                        currentTeacherId = user.teacherId.toIntOrNull() ?: 0

                        when (user.role) {
                            "Admin" -> {
                                navController.navigate(Screen.AdminDashboard.route) {
                                    popUpTo(Screen.Login.route) {
                                        inclusive = true
                                    }
                                }
                            }

                            "Teacher" -> {
                                navController.navigate(Screen.TeacherDashboard.route) {
                                    popUpTo(Screen.Login.route) {
                                        inclusive = true
                                    }
                                }
                            }

                            "Student" -> {
                                FcmTokenHelper.saveStudentFcmToken(
                                    context = context,
                                    studentId = user.studentId
                                )

                                navController.navigate(Screen.StudentDashboard.route) {
                                    popUpTo(Screen.Login.route) {
                                        inclusive = true
                                    }
                                }
                            }

                            else -> {
                                navController.navigate(Screen.Login.route)
                            }
                        }
                    }
                )
            }

            composable(Screen.AdminDashboard.route) {
                AdminDashboardScreen(
                    adminName = currentUserName,
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

            composable(Screen.AdminProfile.route) {
                AdminProfileScreen(
                    userId = currentUserId,
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
                        clearCurrentUserSession()
                    }
                )
            }

            composable(Screen.EditAdminProfile.route) {
                EditAdminProfileScreen(
                    userId = currentUserId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.UserManagement.route) {
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

            composable(Screen.TeacherManagement.route) {
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

            composable(Screen.AddTeacher.route) {
                AddTeacherScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
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

            composable(
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

            composable(Screen.ClassManagement.route) {
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

            composable(
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

            composable(Screen.TeacherDashboard.route) {
                TeacherDashboardScreen(
                    teacherId = currentTeacherId,
                    firebaseTeacherId = currentFirebaseTeacherId,
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

            composable(Screen.StudentDashboard.route) {
                StudentDashboardScreen(
                    studentId = currentStudentId,
                    firebaseStudentId = currentFirebaseStudentId,
                    onProfileClick = {
                        navController.navigate(Screen.StudentProfile.route)
                    },
                    onReportClick = {
                        navController.navigate(
                            Screen.StudentReportSummary.createRoute(getCurrentStudentRouteId())
                        )
                    }
                )
            }

            composable(Screen.StudentManagement.route) {
                StudentManagementScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onAddStudentClick = {
                        navController.navigate(Screen.AddStudent.route)
                    },
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

            composable(Screen.AddStudent.route) {
                AddStudentScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
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

            composable(
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

            composable(Screen.SubjectManagement.route) {
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

            composable(Screen.AddSubject.route) {
                AddSubjectScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
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

            composable(
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

            composable(Screen.GradeManagement.route) {
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

            composable(Screen.AddGrade.route) {
                AddGradeScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
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

            composable(Screen.QuickAddGrade.route) {
                QuickAddGradeScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
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

            composable(
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

            composable(Screen.Report.route) {
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

            composable(
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

            composable(
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

            composable(
                route = Screen.StudentReportSummary.route,
                arguments = listOf(
                    navArgument("studentId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")
                    ?: getCurrentStudentRouteId()

                StudentReportSummaryScreen(
                    studentId = studentId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                route = Screen.StudentNotifications.route,
                arguments = listOf(
                    navArgument("studentId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getString("studentId")
                    ?: getCurrentStudentRouteId()

                StudentNotificationScreen(
                    studentId = studentId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.TeacherProfile.route) {
                TeacherProfileScreen(
                    teacherId = currentTeacherId,
                    firebaseTeacherId = currentFirebaseTeacherId,
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
                        clearCurrentUserSession()
                    }
                )
            }

            composable(Screen.EditTeacherProfile.route) {
                EditTeacherProfileScreen(
                    teacherId = currentTeacherId,
                    firebaseTeacherId = currentFirebaseTeacherId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.StudentProfile.route) {
                StudentProfileScreen(
                    studentId = currentStudentId,
                    firebaseStudentId = currentFirebaseStudentId,
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
                        clearCurrentUserSession()
                    }
                )
            }

            composable(Screen.EditStudentProfile.route) {
                EditStudentProfileScreen(
                    studentId = currentStudentId,
                    firebaseStudentId = currentFirebaseStudentId,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveSuccess = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.ChangePassword.route) {
                ChangePasswordScreen(
                    userId = currentUserId,
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