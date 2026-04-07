package com.example.minorapp.presentation.screen.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.minorapp.data.session.SessionManager

@Composable
fun LibraryRoute(
    sessionManager: SessionManager,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit = {}
) {
    val viewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModel.factory(sessionManager)
    )
    val uiState by viewModel.uiState.collectAsState()

    LibraryScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onProfileClick = onProfileClick,
        onLogoutClick = onLogoutClick,
        onSearchQueryChanged = viewModel::onSearchQueryChanged
    )
}
