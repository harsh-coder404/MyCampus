package com.example.minorapp.presentation.screen.tasks

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minorapp.data.session.SessionManager

@Composable
fun ProfessorTasksRoute(
    sessionManager: SessionManager,
    onNavigateToDashboard: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToSummary: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val viewModel: ProfessorTasksViewModel = viewModel(
        factory = ProfessorTasksViewModel.factory(sessionManager)
    )

    ProfessorTasksScreen(
        uiState = viewModel.uiState,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onDeadlineChange = viewModel::onDeadlineChange,
        onCategorySelect = viewModel::onCategorySelect,
        onCategoryDropdownToggle = viewModel::toggleCategoryDropdown,
        onCategoryDropdownHide = viewModel::hideCategoryDropdown,
        onDeployAssignment = {}, // Backend integration later
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToAttendance = onNavigateToAttendance,
        onNavigateToSummary = onNavigateToSummary,
        onProfileClick = onProfileClick
    )
}

