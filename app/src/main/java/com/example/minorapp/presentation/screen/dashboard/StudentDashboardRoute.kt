package com.example.minorapp.presentation.screen.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minorapp.BuildConfig
import com.example.minorapp.data.dashboard.DashboardRepository
import com.example.minorapp.data.session.SessionManager

@Composable
fun StudentDashboardRoute(
    sessionManager: SessionManager,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit = {},
    onAttendanceClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onSummaryClick: () -> Unit = {}
) {
    val dashboardRepository = remember { DashboardRepository(BuildConfig.AUTH_BASE_URL) }
    val viewModel: StudentDashboardViewModel = viewModel(
        factory = StudentDashboardViewModel.factory(sessionManager, dashboardRepository)
    )

    StudentDashboardScreen(
        uiState = viewModel.uiState,
        onProfileMenuClick = onProfileClick,
        onContactAdminClick = {},
        onLogoutMenuClick = onLogout,
        onProfileClick = onProfileClick,
        onAttendanceClick = onAttendanceClick,
        onTasksClick = onTasksClick,
        onSummaryClick = onSummaryClick
    )
}
