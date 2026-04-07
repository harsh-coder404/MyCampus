package com.example.minorapp.presentation.navigation

import androidx.navigation.NavHostController
import com.example.minorapp.domain.model.UserRole

const val RESET_LINK_SENT_MESSAGE_KEY = "reset_link_sent_message"
const val RESET_PASSWORD_EMAIL_KEY = "reset_password_email"

fun NavHostController.navigateToLogin() {
    navigate(AppRoute.Login.route) {
        popUpTo(AppRoute.Splash.route) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.navigateToDashboard() {
    navigate(AppRoute.Dashboard.route) {
        popUpTo(AppRoute.Splash.route) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.navigateToRoleDashboard(role: UserRole) {
    val destination = when (role) {
        UserRole.PROFESSOR -> AppRoute.ProfessorDashboard.route
        UserRole.STUDENT -> AppRoute.Dashboard.route
    }
    navigate(destination) {
        popUpTo(AppRoute.Splash.route) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.navigateToProfessorDashboard() {
    navigate(AppRoute.ProfessorDashboard.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToProfessorAttendance() {
    navigate(AppRoute.ProfessorAttendance.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToAttendance() {
    navigate(AppRoute.Attendance.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToTasks() {
    navigate(AppRoute.Tasks.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToProfessorTasks() {
    navigate(AppRoute.ProfessorTasks.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToSummary() {
    navigate(AppRoute.Summary.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToProfessorSummary() {
    navigate(AppRoute.ProfessorSummary.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToProfessorLibrary() {
    navigate(AppRoute.ProfessorLibrary.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToProfessorProfile() {
    // Prevent duplicate Professor Profile entries on repeated taps.
    if (currentDestination?.route == AppRoute.ProfessorProfile.route) return

    val reusedExistingProfile = popBackStack(AppRoute.ProfessorProfile.route, inclusive = false)
    if (!reusedExistingProfile) {
        navigate(AppRoute.ProfessorProfile.route) {
            launchSingleTop = true
        }
    }
}

fun NavHostController.navigateToLibrary() {
    navigate(AppRoute.Library.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToProfile() {
    // Prevent duplicate Profile entries on rapid/repeated taps.
    if (currentDestination?.route == AppRoute.Profile.route) return

    val reusedExistingProfile = popBackStack(AppRoute.Profile.route, inclusive = false)
    if (!reusedExistingProfile) {
        navigate(AppRoute.Profile.route) {
            launchSingleTop = true
        }
    }
}

fun NavHostController.navigateToResetPassword(requireCurrentPassword: Boolean = true) {
    navigate(
        AppRoute.ResetPassword.createRoute(
            requireCurrentPassword = requireCurrentPassword,
            origin = AppRoute.ResetPassword.ORIGIN_STUDENT_PROFILE
        )
    ) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToResetPasswordFromProfessorProfile(requireCurrentPassword: Boolean = true) {
    navigate(
        AppRoute.ResetPassword.createRoute(
            requireCurrentPassword = requireCurrentPassword,
            origin = AppRoute.ResetPassword.ORIGIN_PROFESSOR_PROFILE
        )
    ) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateBackToDashboardFromProfile() {
    val popped = popBackStack(AppRoute.Dashboard.route, inclusive = false)
    if (!popped) {
        // Strict back policy: do not create a new Dashboard route from Profile.
        popBackStack()
    }
}

fun NavHostController.navigateToSignUp() {
    navigate(AppRoute.SignUp.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToForgotPassword() {
    navigate(AppRoute.ForgotPassword.route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateToResetPasswordFromForgotPassword(email: String) {
    currentBackStackEntry?.savedStateHandle?.set(RESET_PASSWORD_EMAIL_KEY, email)
    navigate(
        AppRoute.ResetPassword.createRoute(
            requireCurrentPassword = false,
            origin = AppRoute.ResetPassword.ORIGIN_FORGOT_PASSWORD
        )
    ) {
        popUpTo(AppRoute.ForgotPassword.route) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.popBackStackToPreviousOnly() {
    popBackStack()
}

fun NavHostController.navigateToResetLinkSentFromForgotPassword(message: String) {
    currentBackStackEntry?.savedStateHandle?.set(RESET_LINK_SENT_MESSAGE_KEY, message)
    navigate(AppRoute.ResetLinkSent.route) {
        popUpTo(AppRoute.ForgotPassword.route)
        launchSingleTop = true
    }
}

fun NavHostController.popBackToLoginFromSignUp() {
    val popped = popBackStack(AppRoute.Login.route, inclusive = false)
    if (!popped) {
        navigate(AppRoute.Login.route) {
            launchSingleTop = true
        }
    }
}

fun NavHostController.popBackToLoginFromForgotPassword() {
    val popped = popBackStack(AppRoute.Login.route, inclusive = false)
    if (!popped) {
        navigate(AppRoute.Login.route) {
            launchSingleTop = true
        }
    }
}

fun NavHostController.popBackToLoginFromResetLinkSent() {
    val popped = popBackStack(AppRoute.Login.route, inclusive = false)
    if (!popped) {
        navigate(AppRoute.Login.route) {
            launchSingleTop = true
        }
    }
}

fun NavHostController.navigateToLoginFromDashboard() {
    navigate(AppRoute.Login.route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavHostController.navigateBackToProfessorDashboardFromProfile() {
    val popped = popBackStack(AppRoute.ProfessorDashboard.route, inclusive = false)
    if (!popped) {
        popBackStack()
    }
}

fun NavHostController.navigateBackToProfessorSummaryFromLibrary() {
    val popped = popBackStack(AppRoute.ProfessorSummary.route, inclusive = false)
    if (!popped) {
        popBackStack()
    }
}

fun NavHostController.navigateAfterResetPassword(origin: String?) {
    when (origin) {
        AppRoute.ResetPassword.ORIGIN_PROFESSOR_PROFILE -> navigateBackToProfessorDashboardFromProfile()
        AppRoute.ResetPassword.ORIGIN_STUDENT_PROFILE -> navigateBackToDashboardFromProfile()
        AppRoute.ResetPassword.ORIGIN_FORGOT_PASSWORD -> popBackToLoginFromForgotPassword()
        else -> popBackStack()
    }
}

