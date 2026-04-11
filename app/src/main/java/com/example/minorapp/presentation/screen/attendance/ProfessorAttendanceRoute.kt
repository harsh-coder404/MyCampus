package com.example.minorapp.presentation.screen.attendance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

    LaunchedEffect(viewModel.uiState.shouldForceReauth) {
        if (viewModel.uiState.shouldForceReauth) {
            viewModel.onForceReauthHandled()
            onLogoutClick()
        }
    }

    ProfessorAttendanceScreen(
        viewModel.uiState,
        viewModel::onStudentStatusChange,
        viewModel::onMarkAllPresent,
        viewModel::onModeSelected,
        viewModel::onCourseSelected,
        viewModel::onStartQrAttendance,
        onNavigateToDashboard,
        onNavigateToTasks,
        onNavigateToSummary,
        onProfileClick,
        onLogoutClick,
        viewModel::onSubmitAttendanceRecord
    )
}

