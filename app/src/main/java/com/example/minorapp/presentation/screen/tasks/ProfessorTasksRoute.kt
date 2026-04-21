package com.example.minorapp.presentation.screen.tasks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.widget.Toast
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
    val context = LocalContext.current
    val viewModel: ProfessorTasksViewModel = viewModel(
        factory = ProfessorTasksViewModel.factory(sessionManager)
    )

    LaunchedEffect(viewModel.uiState.shouldForceReAuth) {
        if (viewModel.uiState.shouldForceReAuth) {
            viewModel.onForceReauthHandled()
            onLogoutClick()
        }
    }

    LaunchedEffect(viewModel.uiState.deleteCommitNotice) {
        val notice = viewModel.uiState.deleteCommitNotice
        if (!notice.isNullOrBlank()) {
            Toast.makeText(context, notice, Toast.LENGTH_SHORT).show()
            viewModel.onDeleteCommitNoticeShown()
        }
    }

    LaunchedEffect(viewModel.uiState.statusMessage) {
        val message = viewModel.uiState.statusMessage ?: return@LaunchedEffect
        if (message.contains("deployed", ignoreCase = true)) {
            Toast.makeText(context, "Task deployed", Toast.LENGTH_SHORT).show()
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
        onEditTitleChange = viewModel::onEditTitleChange,
        onEditDescriptionChange = viewModel::onEditDescriptionChange,
        onEditDeadlineChange = viewModel::onEditDeadlineChange,
        onEditCategorySelect = viewModel::onEditCategorySelect,
        onEditCategoryDropdownToggle = viewModel::toggleEditCategoryDropdown,
        onEditCategoryDropdownHide = viewModel::hideEditCategoryDropdown,
        onEditClassTargetSelected = viewModel::onEditClassTargetSelected,
        onSelectChecklistTask = viewModel::onSelectChecklistTask,
        onEditTask = viewModel::onEditTask,
        onCancelEditTask = viewModel::onCancelEditTask,
        onDeleteTask = viewModel::onDeleteTask,
        onUndoDeleteTask = viewModel::onUndoDeleteTask,
        onSubmitTaskUpdate = viewModel::onSubmitTaskUpdate,
        onDismissUpdateConfirmation = viewModel::onUpdateConfirmationDismissed,
        onDeployAssignment = viewModel::onDeployAssignment,
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToAttendance = onNavigateToAttendance,
        onNavigateToSummary = onNavigateToSummary,
        onProfileClick = onProfileClick,
        onLogoutClick = onLogoutClick
    )
}

