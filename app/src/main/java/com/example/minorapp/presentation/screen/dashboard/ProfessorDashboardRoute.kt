package com.example.minorapp.presentation.screen.dashboard

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minorapp.data.session.SessionManager

@Composable
fun ProfessorDashboardRoute(
    sessionManager: SessionManager,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit = {},
    onAttendanceClick: () -> Unit = {},
    onTasksClick: () -> Unit = {},
    onSummaryClick: () -> Unit = {}
) {
    val viewModel: ProfessorDashboardViewModel = viewModel(
        factory = ProfessorDashboardViewModel.factory(sessionManager)
    )

    ProfessorDashboardScreen(
        uiState = viewModel.uiState,
        onLogout = onLogout,
        onProfileClick = onProfileClick,
        onAttendanceClick = onAttendanceClick,
        onTasksClick = onTasksClick,
        onSummaryClick = onSummaryClick
    )
}
