package com.example.minorapp.presentation.screen.tasks

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minorapp.data.tasks.LocalTasksRepository
import com.example.minorapp.data.session.SessionManager

@Composable
fun TasksRoute(
    sessionManager: SessionManager,
    onNavigateToDashboard: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToSummary: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: TasksViewModel = viewModel(
        factory = TasksViewModel.factory(LocalTasksRepository(), sessionManager)
    )
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
        uiState = viewModel.uiState,
        onFilterSelected = viewModel::onFilterSelected,
        onSortMenuExpandedChange = viewModel::onSortMenuExpandedChange,
        onSortSelected = viewModel::onSortSelected,
        onPendingTaskClick = viewModel::onPendingTaskClicked,
        onPriorityTaskClick = viewModel::onPriorityTaskSelected,
        onDeleteSubmittedPdf = viewModel::onDeleteSubmittedPdf,
        onDeleteCustomTask = viewModel::onDeleteCustomTask,
        onCreateTask = viewModel::onCreateTask,
        onUploadPdfClick = {
            pdfPickerLauncher.launch(arrayOf("application/pdf"))
        },
        onDismissUploadDialog = viewModel::onUploadDialogDismissed,
        onPreviewUploadedPdf = { pdfUri ->
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
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToAttendance = onNavigateToAttendance,
        onNavigateToSummary = onNavigateToSummary,
        onProfileClick = onProfileClick
    )
}
