package de.haaremy.hmychat.ui.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.haaremy.hmychat.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.openid.appauth.*
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(authRepository.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private var authState: AuthState = AuthState()
    private var codeVerifier: String? = null

    companion object {
        private const val SSO_ISSUER = "https://sso.haaremy.de"
        private const val CLIENT_ID = "0_j6GmSljSuOVOEXg5Gz-A"
        private const val REDIRECT_URI = "de.haaremy.hmychat://auth/callback"
    }

    fun buildAuthIntent(context: Context, onReady: (Intent) -> Unit) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("$SSO_ISSUER/authorize"),
            Uri.parse("$SSO_ISSUER/token")
        )

        val generatedVerifier = CodeVerifierUtil.generateRandomCodeVerifier()
        val codeChallenge = CodeVerifierUtil.deriveCodeVerifierChallenge(generatedVerifier)

        val authRequest = AuthorizationRequest.Builder(
            serviceConfig,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(REDIRECT_URI)
        )
            .setScope("openid profile email")
            .setCodeVerifier(generatedVerifier, codeChallenge, "S256")
            .build()

        codeVerifier = generatedVerifier

        val authService = AuthorizationService(context)
        val intent = authService.getAuthorizationRequestIntent(authRequest)
        authService.dispose()
        _uiState.value = _uiState.value.copy(isLoading = false)
        onReady(intent)
    }

    fun handleAuthResponse(intent: Intent, onSuccess: () -> Unit) {
        val response = AuthorizationResponse.fromIntent(intent)
        val exception = AuthorizationException.fromIntent(intent)

        if (response != null && response.authorizationCode != null) {
            val code = response.authorizationCode!!
            // AppAuth serializes the original request into the response, so the verifier
            // survives process death. Fall back to the in-memory value if needed.
            val verifier = response.request.codeVerifier ?: codeVerifier

            if (verifier == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Code verifier missing"
                )
                return
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            viewModelScope.launch {
                val result = authRepository.exchangeCodeForToken(code, verifier)
                result.fold(
                    onSuccess = {
                        _isLoggedIn.value = true
                        _uiState.value = LoginUiState()
                        onSuccess()
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Token exchange failed"
                        )
                    }
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = exception?.message ?: "Authorization failed"
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
