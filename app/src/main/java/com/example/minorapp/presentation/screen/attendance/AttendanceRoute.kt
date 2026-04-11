package com.example.minorapp.presentation.screen.attendance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minorapp.BuildConfig
import com.example.minorapp.data.attendance.AttendanceRepository
import com.example.minorapp.data.attendance.QrAttendanceRepository
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.domain.model.UserRole

@Composable
fun AttendanceRoute(
    sessionManager: SessionManager,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit = {},
    onNavigateToSummary: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    if (sessionManager.getSavedRole() == UserRole.PROFESSOR) {
        ProfessorAttendanceRoute(
            sessionManager = sessionManager,
            onNavigateToDashboard = onNavigateToDashboard,
            onNavigateToTasks = onNavigateToTasks,
            onNavigateToSummary = onNavigateToSummary,
            onProfileClick = onProfileClick,
            onLogoutClick = onLogoutClick
        )
    } else {
        val attendanceRepository = remember { AttendanceRepository(BuildConfig.AUTH_BASE_URL) }
        val qrAttendanceRepository = remember { QrAttendanceRepository(BuildConfig.AUTH_BASE_URL) }
        val viewModel: AttendanceViewModel = viewModel(
            factory = AttendanceViewModel.factory(sessionManager, attendanceRepository, qrAttendanceRepository)
        )

        LaunchedEffect(viewModel.uiState.shouldForceReauth) {
            if (viewModel.uiState.shouldForceReauth) {
                viewModel.onForceReauthHandled()
                onLogoutClick()
            }
        }

        AttendanceScreen(
            viewModel.uiState,
            viewModel::onInsightsPeriodSelected,
            viewModel::onScanQrPayload,
            viewModel::onQrResultMessageShown,
            onNavigateToDashboard,
            onNavigateToTasks,
            onNavigateToSummary,
            onProfileClick,
            onLogoutClick
        )
    }
}
