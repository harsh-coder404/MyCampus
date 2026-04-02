package com.example.minorapp.presentation.screen.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minorapp.data.session.SessionManager

@Composable
fun ProfileRoute(
    sessionManager: SessionManager,
    onBackClick: () -> Unit,
    onNavigateToResetPassword: () -> Unit,
    isProfessorFlow: Boolean = false
) {
    val context = LocalContext.current

    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModel.factory(
            sessionManager = sessionManager,
            isProfessorFlow = isProfessorFlow
        )
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                try {
                    context.contentResolver.takePersistableUriPermission(uri, flag)
                } catch (_: SecurityException) {
                    // Some providers do not grant persistable permissions.
                }

                viewModel.onImagePicked(uri.toString()) { event ->
                    when (event) {
                        ProfileOneOffUiEvent.UploadingImage -> {
                            Toast.makeText(context, "Uploading image to backend...", Toast.LENGTH_SHORT).show()
                        }

                        ProfileOneOffUiEvent.UploadComplete -> {
                            Toast.makeText(context, "Upload complete", Toast.LENGTH_SHORT).show()
                        }

                        else -> Unit
                    }
                }
            }
        }
    )

    ProfileScreen(
        uiState = viewModel.uiState,
        selectedImageUri = viewModel.uiState.selectedImageUri?.let(Uri::parse),
        onBackClick = onBackClick,
        onNavigateToResetPassword = onNavigateToResetPassword,
        onUsernameChanged = viewModel::onUsernameChanged,
        onPickProfileImage = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onRemoveProfileImage = {
            viewModel.onRemoveImage { event ->
                when (event) {
                    ProfileOneOffUiEvent.RemovingImage -> {
                        Toast.makeText(context, "Removing image from backend...", Toast.LENGTH_SHORT).show()
                    }

                    ProfileOneOffUiEvent.ImageRemoved -> {
                        Toast.makeText(context, "Image removed", Toast.LENGTH_SHORT).show()
                    }

                    else -> Unit
                }
            }
        },
        onSaveChangesClick = viewModel::onSaveChangesClick
    )
}
