package com.example.minorapp.presentation.screen.attendance

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minorapp.data.session.SessionManager

@Composable
fun ProfessorAttendanceRoute(
    sessionManager: SessionManager,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit = {},
    onNavigateToSummary: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val viewModel: ProfessorAttendanceViewModel = viewModel(
        factory = ProfessorAttendanceViewModel.factory(sessionManager)
    )

    ProfessorAttendanceScreen(
        uiState = viewModel.uiState,
        onStudentStatusChange = viewModel::onStudentStatusChange,
        onMarkAllPresent = viewModel::onMarkAllPresent,
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToTasks = onNavigateToTasks,
        onNavigateToSummary = onNavigateToSummary,
        onProfileClick = onProfileClick,
        onLogoutClick = onLogoutClick,
        onSubmitClick = { /* Handle submit later */ }
    )
}

