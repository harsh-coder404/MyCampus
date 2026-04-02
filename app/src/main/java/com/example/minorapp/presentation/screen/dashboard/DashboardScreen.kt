package com.example.minorapp.presentation.screen.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.domain.model.UserRole
import com.example.minorapp.ui.theme.MinorAppTheme

@Composable
fun DashboardScreen(
    role: UserRole,
    sessionManager: SessionManager? = null,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit = {},
    onAttendanceClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onSummaryClick: () -> Unit = {},
    onProfessorTestClick: () -> Unit = {}
) {
    when (role) {
        UserRole.STUDENT -> {
            if (sessionManager != null) {
                StudentDashboardRoute(
                    sessionManager = sessionManager,
                    onLogout = onLogout,
                    onProfileClick = onProfileClick,
                    onAttendanceClick = onAttendanceClick,
                    onTasksClick = onTasksClick,
                    onSummaryClick = onSummaryClick,
                    onProfessorTestClick = onProfessorTestClick
                )
            } else {
                StudentDashboardScreen(
                    uiState = StudentDashboardUiState(),
                    onProfileMenuClick = onProfileClick,
                    onContactAdminClick = {},
                    onLogoutMenuClick = onLogout,
                    onProfileClick = onProfileClick,
                    onAttendanceClick = onAttendanceClick,
                    onTasksClick = onTasksClick,
                    onSummaryClick = onSummaryClick,
                    onProfessorTestClick = onProfessorTestClick
                )
            }
        }

        UserRole.PROFESSOR -> {
            if (sessionManager != null) {
                ProfessorDashboardRoute(
                    sessionManager = sessionManager,
                    onLogout = onLogout,
                    onProfileClick = onProfileClick,
                    onAttendanceClick = onAttendanceClick,
                    onTasksClick = onTasksClick,
                    onSummaryClick = onSummaryClick
                )
            } else {
                ProfessorDashboardScreen(
                    uiState = ProfessorDashboardUiState(),
                    onLogout = onLogout,
                    onProfileClick = onProfileClick,
                    onAttendanceClick = onAttendanceClick,
                    onTasksClick = onTasksClick,
                    onSummaryClick = onSummaryClick
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    MinorAppTheme {
        DashboardScreen(role = UserRole.STUDENT, onLogout = {}, onProfileClick = {}, onAttendanceClick = {}, onTasksClick = {})
    }
}
