package com.example.sgrms.navigation

sealed class Screen(val route: String) {

    object Login : Screen("login")

    object AdminDashboard : Screen("admin_dashboard")
    object TeacherDashboard : Screen("teacher_dashboard")
    object StudentDashboard : Screen("student_dashboard")

    object TeacherManagement : Screen("teacher_management")

    object UserManagement : Screen("user_management")
    object AddTeacher : Screen("add_teacher")

    object TeacherDetail : Screen("teacher_detail/{teacherId}") {
        fun createRoute(teacherId: String): String {
            return "teacher_detail/$teacherId"
        }
    }

    object EditTeacher : Screen("edit_teacher/{teacherId}") {
        fun createRoute(teacherId: String): String {
            return "edit_teacher/$teacherId"
        }
    }

    object AddStudentAccount : Screen("add_student_account")
    object ClassManagement : Screen("class_management")

    object ClassDetail : Screen("class_detail/{classId}") {
        fun createRoute(classId: String): String {
            return "class_detail/$classId"
        }
    }

    object EditUser : Screen("edit_user/{userId}") {
        fun createRoute(userId: Int): String {
            return "edit_user/$userId"
        }
    }

    object StudentManagement : Screen("student_management")
    object AddStudent : Screen("add_student")

    object StudentDetail : Screen("student_detail/{studentId}") {
        fun createRoute(studentId: String): String {
            return "student_detail/$studentId"
        }
    }

    object EditStudent : Screen("edit_student/{studentId}") {
        fun createRoute(studentId: String): String {
            return "edit_student/$studentId"
        }
    }

    object SubjectManagement : Screen("subject_management")
    object AddSubject : Screen("add_subject")

    object SubjectDetail : Screen("subject_detail/{subjectId}") {
        fun createRoute(subjectId: String): String {
            return "subject_detail/$subjectId"
        }
    }

    object EditSubject : Screen("edit_subject/{subjectId}") {
        fun createRoute(subjectId: String): String {
            return "edit_subject/$subjectId"
        }
    }

    object GradeManagement : Screen("grade_management")
    object AddGrade : Screen("add_grade")
    object QuickAddGrade : Screen("quick_add_grade")

    object AddStudentGrade : Screen("add_student_grade/{studentId}") {
        fun createRoute(studentId: String): String {
            return "add_student_grade/$studentId"
        }
    }

    object EditGrade : Screen("edit_grade/{gradeId}") {
        fun createRoute(gradeId: String): String {
            return "edit_grade/$gradeId"
        }
    }

    object StudentGradeDetail : Screen("student_grade_detail/{studentId}") {
        fun createRoute(studentId: String): String {
            return "student_grade_detail/$studentId"
        }
    }

    object Report : Screen("report")

    object ReportDetail : Screen("report_detail/{reportId}") {
        fun createRoute(reportId: String): String {
            return "report_detail/$reportId"
        }
    }

    object UpdateReport : Screen("update_report/{reportId}") {
        fun createRoute(reportId: String): String {
            return "update_report/$reportId"
        }
    }

    object StudentReportSummary : Screen("student_report_summary/{studentId}") {
        fun createRoute(studentId: String): String {
            return "student_report_summary/$studentId"
        }
    }

    object AdminProfile : Screen("admin_profile")
    object TeacherProfile : Screen("teacher_profile")
    object StudentProfile : Screen("student_profile")

    object StudentNotifications : Screen("student_notifications/{studentId}") {
        fun createRoute(studentId: String): String {
            return "student_notifications/$studentId"
        }
    }

    object EditAdminProfile : Screen("edit_admin_profile")
    object EditTeacherProfile : Screen("edit_teacher_profile")
    object EditStudentProfile : Screen("edit_student_profile")

    object ChangePassword : Screen("change_password")
}