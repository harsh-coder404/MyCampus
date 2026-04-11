package com.example.minorapp.presentation.screen.tasks

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minorapp.BuildConfig
import com.example.minorapp.data.session.SessionManager
import com.example.minorapp.data.tasks.BackendTasksRepository

@Composable
fun TasksRoute(
    sessionManager: SessionManager,
    onNavigateToDashboard: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToSummary: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val tasksRepository = remember { BackendTasksRepository(BuildConfig.AUTH_BASE_URL) }
    val viewModel: TasksViewModel = viewModel(
        factory = TasksViewModel.factory(tasksRepository, sessionManager)
    )

    LaunchedEffect(viewModel.uiState.shouldForceReauth) {
        if (viewModel.uiState.shouldForceReauth) {
            viewModel.onForceReauthHandled()
            onLogoutClick()
        }
    }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Ignore if URI permission cannot be persisted for this provider.
            }
            viewModel.onPdfUploaded(uri.toString())
        }
    }

    TasksScreen(
        viewModel.uiState,
        viewModel::onFilterSelected,
        viewModel::onSortMenuExpandedChange,
        viewModel::onSortSelected,
        viewModel::onPendingTaskClicked,
        viewModel::onPriorityTaskSelected,
        viewModel::onDeleteSubmittedPdf,
        viewModel::onDeleteCustomTask,
        viewModel::onEditCustomTask,
        viewModel::onCreateTask,
        { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
        viewModel::onUploadDialogDismissed,
        { pdfUri ->
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(pdfUri), "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "No app found to preview PDF", Toast.LENGTH_SHORT).show()
            }
        },
        onNavigateToDashboard,
        onNavigateToAttendance,
        onNavigateToSummary,
        onProfileClick,
        onLogoutClick
    )
}
