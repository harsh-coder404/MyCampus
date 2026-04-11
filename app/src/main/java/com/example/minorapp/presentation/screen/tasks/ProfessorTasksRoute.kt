package com.example.minorapp.presentation.screen.tasks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.minorapp.data.session.SessionManager

@Composable
fun ProfessorTasksRoute(
    sessionManager: SessionManager,
    onNavigateToDashboard: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToSummary: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: ProfessorTasksViewModel = viewModel(
        factory = ProfessorTasksViewModel.factory(sessionManager)
    )

    LaunchedEffect(viewModel.uiState.shouldForceReauth) {
        if (viewModel.uiState.shouldForceReauth) {
            viewModel.onForceReauthHandled()
            onLogoutClick()
        }
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_START -> viewModel.startChecklistPolling()
                androidx.lifecycle.Lifecycle.Event.ON_STOP,
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> viewModel.stopChecklistPolling()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopChecklistPolling()
        }
    }

    ProfessorTasksScreen(
        uiState = viewModel.uiState,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onDeadlineChange = viewModel::onDeadlineChange,
        onCategorySelect = viewModel::onCategorySelect,
        onCategoryDropdownToggle = viewModel::toggleCategoryDropdown,
        onCategoryDropdownHide = viewModel::hideCategoryDropdown,
        onClassTargetSelected = viewModel::onClassTargetSelected,
        onSelectChecklistTask = viewModel::onSelectChecklistTask,
        onDeployAssignment = viewModel::onDeployAssignment,
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToAttendance = onNavigateToAttendance,
        onNavigateToSummary = onNavigateToSummary,
        onProfileClick = onProfileClick,
        onLogoutClick = onLogoutClick
    )
}

