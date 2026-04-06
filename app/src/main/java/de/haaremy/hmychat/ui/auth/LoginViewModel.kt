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

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mfaRequired: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(authRepository.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun login(usernameOrEmail: String, password: String, totpCode: String? = null, onSuccess: () -> Unit) {
        if (usernameOrEmail.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Bitte alle Felder ausfuellen.")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = authRepository.login(usernameOrEmail, password, totpCode)
            result.fold(
                onSuccess = {
                    _isLoggedIn.value = true
                    _uiState.value = LoginUiState()
                    onSuccess()
                },
                onFailure = { e ->
                    val msg = e.message ?: "Anmeldung fehlgeschlagen."
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = msg,
                        mfaRequired = msg.contains("MFA", ignoreCase = true)
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
