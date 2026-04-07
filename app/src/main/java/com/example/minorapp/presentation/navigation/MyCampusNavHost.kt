package com.example.minorapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.domain.model.UserRole
import com.example.minorapp.presentation.screen.attendance.AttendanceRoute
import com.example.minorapp.presentation.screen.attendance.ProfessorAttendanceRoute
import com.example.minorapp.presentation.screen.dashboard.DashboardScreen
import com.example.minorapp.presentation.screen.dashboard.ProfessorDashboardRoute
import com.example.minorapp.presentation.screen.forgotpassword.ForgotPasswordRoute
import com.example.minorapp.presentation.screen.forgotpassword.ResetLinkSentRoute
import com.example.minorapp.presentation.screen.login.LoginRoute
import com.example.minorapp.presentation.screen.profile.ProfileRoute
import com.example.minorapp.presentation.screen.profile.ResetPasswordScreen
import com.example.minorapp.presentation.screen.library.LibraryRoute
import com.example.minorapp.presentation.screen.signup.SignUpRoute
import com.example.minorapp.presentation.screen.splash.SplashRoute
import com.example.minorapp.presentation.screen.tasks.TasksRoute
import com.example.minorapp.presentation.screen.tasks.ProfessorTasksRoute
import com.example.minorapp.presentation.screen.summary.SummaryRoute

@Composable
fun MyCampusNavHost(
    navController: NavHostController,
    sessionManager: SessionManager,
    startDestination: String = AppRoute.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppRoute.Splash.route) {
            SplashRoute(
                sessionManager = sessionManager,
                onNavigateToLogin = navController::navigateToLogin,
                onNavigateToDashboard = navController::navigateToRoleDashboard
            )
        }

        composable(AppRoute.Login.route) {
            LoginRoute(
                sessionManager = sessionManager,
                onNavigateToForgotPassword = navController::navigateToForgotPassword,
                onNavigateToSignUp = navController::navigateToSignUp,
                onNavigateToDashboard = navController::navigateToRoleDashboard
            )
        }

        composable(AppRoute.ForgotPassword.route) {
            ForgotPasswordRoute(
                onNavigateToResetPassword = navController::navigateToResetPasswordFromForgotPassword,
                onNavigateToResetLinkSent = { },
                onBackToLoginClick = navController::popBackStackToPreviousOnly
            )
        }

        composable(AppRoute.ResetLinkSent.route) {
            val backendMessage = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>(RESET_LINK_SENT_MESSAGE_KEY)
                ?.also {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>(RESET_LINK_SENT_MESSAGE_KEY)
                }
                ?: "A secure password reset link has been sent to your institutional inbox."

            ResetLinkSentRoute(
                message = backendMessage,
                onNavigateToLogin = navController::popBackToLoginFromResetLinkSent
            )
        }

        composable(AppRoute.SignUp.route) {
            SignUpRoute(
                onSignInClick = navController::popBackToLoginFromSignUp
            )
        }

        composable(AppRoute.Dashboard.route) {
            DashboardScreen(
                role = sessionManager.getSavedRole(),
                sessionManager = sessionManager,
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigateToLoginFromDashboard()
                },
                onProfileClick = {
                    if (sessionManager.getSavedRole() == UserRole.PROFESSOR) {
                        navController.navigateToProfessorProfile()
                    } else {
                        navController.navigateToProfile()
                    }
                },
                onAttendanceClick = {
                    if (sessionManager.getSavedRole() == UserRole.PROFESSOR) {
                        navController.navigateToProfessorAttendance()
                    } else {
                        navController.navigateToAttendance()
                    }
                },
                onTasksClick = {
                    if (sessionManager.getSavedRole() == UserRole.PROFESSOR) {
                        navController.navigateToProfessorTasks()
                    } else {
                        navController.navigateToTasks()
                    }
                },
                onSummaryClick = {
                    if (sessionManager.getSavedRole() == UserRole.PROFESSOR) {
                        navController.navigateToProfessorSummary()
                    } else {
                        navController.navigateToSummary()
                    }
                }
            )
        }

        composable(AppRoute.ProfessorDashboard.route) {
            ProfessorDashboardRoute(
                sessionManager = sessionManager,
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigateToLoginFromDashboard()
                },
                onProfileClick = navController::navigateToProfessorProfile,
                onAttendanceClick = navController::navigateToProfessorAttendance,
                onTasksClick = navController::navigateToProfessorTasks,
                onSummaryClick = navController::navigateToProfessorSummary
            )
        }

        composable(AppRoute.ProfessorAttendance.route) {
            ProfessorAttendanceRoute(
                sessionManager = sessionManager,
                onNavigateToDashboard = navController::navigateToProfessorDashboard,
                onNavigateToTasks = navController::navigateToProfessorTasks,
                onNavigateToSummary = navController::navigateToProfessorSummary,
                onProfileClick = navController::navigateToProfessorProfile,
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigateToLoginFromDashboard()
                }
            )
        }

        composable(AppRoute.ProfessorTasks.route) {
            ProfessorTasksRoute(
                sessionManager = sessionManager,
                onNavigateToDashboard = navController::navigateToProfessorDashboard,
                onNavigateToAttendance = navController::navigateToProfessorAttendance,
                onNavigateToSummary = navController::navigateToProfessorSummary,
                onProfileClick = navController::navigateToProfessorProfile,
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigateToLoginFromDashboard()
                }
            )
        }

        composable(AppRoute.ProfessorSummary.route) {
            com.example.minorapp.presentation.screen.summary.ProfessorSummaryRoute(
                sessionManager = sessionManager,
                onNavigateToDashboard = navController::navigateToProfessorDashboard,
                onNavigateToAttendance = navController::navigateToProfessorAttendance,
                onNavigateToTasks = navController::navigateToProfessorTasks,
                onNavigateToLibrary = navController::navigateToProfessorLibrary,
                onProfileClick = navController::navigateToProfessorProfile,
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigateToLoginFromDashboard()
                }
            )
        }

        composable(AppRoute.ProfessorLibrary.route) {
            LibraryRoute(
                sessionManager = sessionManager,
                onBackClick = navController::navigateBackToProfessorSummaryFromLibrary,
                onProfileClick = navController::navigateToProfessorProfile,
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigateToLoginFromDashboard()
                }
            )
        }

        composable(AppRoute.Attendance.route) {
            AttendanceRoute(
                sessionManager = sessionManager,
                onNavigateToDashboard = navController::navigateToDashboard,
                onNavigateToTasks = navController::navigateToTasks,
                onNavigateToSummary = navController::navigateToSummary,
                onProfileClick = navController::navigateToProfile,
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigateToLoginFromDashboard()
                }
            )
        }

        composable(AppRoute.Tasks.route) {
            TasksRoute(
                sessionManager = sessionManager,
                onNavigateToDashboard = navController::navigateToDashboard,
                onNavigateToAttendance = navController::navigateToAttendance,
                onNavigateToSummary = navController::navigateToSummary,
                onProfileClick = navController::navigateToProfile,
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigateToLoginFromDashboard()
                }
            )
        }

        composable(AppRoute.Summary.route) {
            SummaryRoute(
                sessionManager = sessionManager,
                onNavigateToDashboard = navController::navigateToDashboard,
                onNavigateToAttendance = navController::navigateToAttendance,
                onNavigateToTasks = navController::navigateToTasks,
                onNavigateToLibrary = navController::navigateToLibrary,
                onProfileClick = navController::navigateToProfile,
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigateToLoginFromDashboard()
                }
            )
        }

        composable(AppRoute.Library.route) {
            LibraryRoute(
                sessionManager = sessionManager,
                onBackClick = { navController.popBackStack() },
                onProfileClick = navController::navigateToProfile,
                onLogoutClick = {
                    sessionManager.clearSession()
                    navController.navigateToLoginFromDashboard()
                }
            )
        }

        composable(AppRoute.Profile.route) {
            ProfileRoute(
                sessionManager = sessionManager,
                onBackClick = { navController.navigateBackToDashboardFromProfile() },
                onNavigateToResetPassword = { navController.navigateToResetPassword(requireCurrentPassword = true) },
                isProfessorFlow = false
            )
        }

        composable(AppRoute.ProfessorProfile.route) {
            ProfileRoute(
                sessionManager = sessionManager,
                onBackClick = navController::navigateBackToProfessorDashboardFromProfile,
                onNavigateToResetPassword = { navController.navigateToResetPasswordFromProfessorProfile(requireCurrentPassword = true) },
                isProfessorFlow = true
            )
        }

        composable(
            route = AppRoute.ResetPassword.route,
            arguments = listOf(
                navArgument("requireCurrentPassword") {
                    type = NavType.BoolType
                    defaultValue = true
                },
                navArgument("origin") {
                    type = NavType.StringType
                    defaultValue = AppRoute.ResetPassword.ORIGIN_STUDENT_PROFILE
                }
            )
        ) { backStackEntry ->
            val requireCurrentPassword = backStackEntry.arguments?.getBoolean("requireCurrentPassword") ?: true
            val origin = backStackEntry.arguments?.getString("origin")
            val resetEmail = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>(RESET_PASSWORD_EMAIL_KEY)
                ?.also {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.remove<String>(RESET_PASSWORD_EMAIL_KEY)
                }
            ResetPasswordScreen(
                sessionManager = sessionManager,
                requireCurrentPassword = requireCurrentPassword,
                resetEmail = resetEmail,
                onBackClick = { navController.navigateAfterResetPassword(origin) },
                onNavigateToForgotPassword = { navController.navigateToForgotPassword() },
                onPasswordUpdated = { navController.navigateAfterResetPassword(origin) }
            )
        }
    }
}
