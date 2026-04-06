package de.haaremy.hmychat.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.haaremy.hmychat.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(
        username: String,
        email: String,
        password: String,
        passwordConfirm: String,
        clearname: String,
        onSuccess: () -> Unit
    ) {
        // Validation
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Benutzername und Passwort sind erforderlich.")
            return
        }
        if (!username.matches(Regex("^[a-zA-Z0-9_]{3,50}$"))) {
            _uiState.value = _uiState.value.copy(error = "Benutzername: nur Buchstaben, Ziffern und _ (3-50 Zeichen).")
            return
        }
        if (password.length < 8) {
            _uiState.value = _uiState.value.copy(error = "Passwort muss mindestens 8 Zeichen lang sein.")
            return
        }
        if (password != passwordConfirm) {
            _uiState.value = _uiState.value.copy(error = "Passwoerter stimmen nicht ueberein.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = authRepository.register(
                username = username,
                password = password,
                email = email.ifBlank { null },
                clearname = clearname.ifBlank { null }
            )
            result.fold(
                onSuccess = {
                    _uiState.value = RegisterUiState()
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Registrierung fehlgeschlagen."
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
