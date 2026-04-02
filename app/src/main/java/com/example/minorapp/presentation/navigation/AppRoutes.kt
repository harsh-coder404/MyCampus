package com.example.minorapp.presentation.navigation

sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Login : AppRoute("login")
    data object ForgotPassword : AppRoute("forgot_password")
    data object ResetLinkSent : AppRoute("reset_link_sent")
    data object SignUp : AppRoute("signup")
    data object Dashboard : AppRoute("dashboard")
    data object ProfessorDashboard : AppRoute("professor_dashboard")
    data object ProfessorAttendance : AppRoute("professor_attendance")
    data object ProfessorTasks : AppRoute("professor_tasks")
    data object ProfessorSummary : AppRoute("professor_summary")
    data object ProfessorLibrary : AppRoute("professor_library")
    data object ProfessorProfile : AppRoute("professor_profile")
    data object Attendance : AppRoute("attendance")
    data object Tasks : AppRoute("tasks")
    data object Summary : AppRoute("summary")
    data object Library : AppRoute("library")
    data object Profile : AppRoute("profile")
    data object ResetPassword : AppRoute("reset_password?requireCurrentPassword={requireCurrentPassword}&origin={origin}") {
        const val ORIGIN_STUDENT_PROFILE = "student_profile"
        const val ORIGIN_PROFESSOR_PROFILE = "professor_profile"
        const val ORIGIN_FORGOT_PASSWORD = "forgot_password"

        fun createRoute(
            requireCurrentPassword: Boolean = true,
            origin: String = ORIGIN_STUDENT_PROFILE
        ) = "reset_password?requireCurrentPassword=$requireCurrentPassword&origin=$origin"
    }
}
