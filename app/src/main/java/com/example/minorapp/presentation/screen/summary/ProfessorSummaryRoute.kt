package com.example.minorapp.presentation.screen.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minorapp.data.session.SessionManager

@Composable
fun ProfessorSummaryRoute(
    sessionManager: SessionManager,
    onNavigateToDashboard: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    val viewModel: ProfessorSummaryViewModel = viewModel(
        factory = ProfessorSummaryViewModel.factory(sessionManager)
    )
    val uiState by viewModel.uiState.collectAsState()

    ProfessorSummaryScreen(
        uiState = uiState,
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToAttendance = onNavigateToAttendance,
        onNavigateToTasks = onNavigateToTasks,
        onNavigateToLibrary = onNavigateToLibrary,
        onProfileClick = onProfileClick,
        onLogoutClick = onLogoutClick
    )
}
