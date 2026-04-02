package com.example.minorapp.presentation.screen.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.minorapp.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class IssuedBookUi(
    val id: String,
    val title: String,
    val author: String,
    val dueDateText: String,
    val isOverdue: Boolean
)

data class LibraryBookUi(
    val id: String,
    val title: String,
    val author: String,
    val status: String
)

data class LibraryUiState(
    val profileImageUri: String? = null,
    val issuedBooks: List<IssuedBookUi> = emptyList(),
    val availableBooks: List<LibraryBookUi> = emptyList(),
    val searchQuery: String = ""
) {
    val activeLoansCount: Int
        get() = issuedBooks.size

    val filteredBooks: List<LibraryBookUi>
        get() {
            val query = searchQuery.trim().lowercase()
            if (query.isBlank()) return availableBooks
            return availableBooks.filter {
                it.title.lowercase().contains(query) || it.author.lowercase().contains(query)
            }
        }
}

class LibraryViewModel(
    sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        LibraryUiState(
            profileImageUri = sessionManager.getProfileImageUri(),
            issuedBooks = listOf(
                IssuedBookUi(
                    id = "issued-1",
                    title = "Advanced Quantum Mechanics",
                    author = "Dr. Richard Feynman",
                    dueDateText = "Oct 24, 2023",
                    isOverdue = false
                ),
                IssuedBookUi(
                    id = "issued-2",
                    title = "Organic Synthesis Vol. IV",
                    author = "Prof. Elena Rodriguez",
                    dueDateText = "Oct 18, 2023",
                    isOverdue = true
                )
            ),
            availableBooks = listOf(
                LibraryBookUi("book-1", "Digital Signal Processing", "A.V. Oppenheim", "AVAILABLE"),
                LibraryBookUi("book-2", "Microelectronic Circuits", "Sedra & Smith", "AVAILABLE"),
                LibraryBookUi("book-3", "Linear Algebra & Its Apps", "Gilbert Strang", "WAITLIST"),
                LibraryBookUi("book-4", "The Molecular Biology", "Bruce Alberts", "AVAILABLE"),
                LibraryBookUi("book-5", "Critique of Pure Reason", "Immanuel Kant", "AVAILABLE"),
                LibraryBookUi("book-6", "The Wealth of Nations", "Adam Smith", "IN REFERENCE"),
                LibraryBookUi("book-7", "Compilers: Principles", "Alfred Aho", "AVAILABLE"),
                LibraryBookUi("book-8", "Probability & Stats", "Morris DeGroot", "AVAILABLE")
            )
        )
    )
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    fun onSearchQueryChanged(value: String) {
        _uiState.value = _uiState.value.copy(searchQuery = value)
    }

    companion object {
        fun factory(sessionManager: SessionManager): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LibraryViewModel(sessionManager) as T
                }
            }
        }
    }
}

