package de.haaremy.hmychat.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.haaremy.hmychat.data.api.models.BlockDto
import de.haaremy.hmychat.data.repository.AuthRepository
import de.haaremy.hmychat.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val contactDiscoveryEnabled: Boolean = true,
    val blockedUsers: List<BlockDto> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadBlockedUsers()
    }

    private fun loadBlockedUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = chatRepository.getBlocks()
            result.fold(
                onSuccess = { blocks ->
                    _uiState.value = _uiState.value.copy(
                        blockedUsers = blocks,
                        isLoading = false
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            )
        }
    }

    fun toggleContactDiscovery() {
        _uiState.value = _uiState.value.copy(
            contactDiscoveryEnabled = !_uiState.value.contactDiscoveryEnabled
        )
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            val result = chatRepository.unblockUser(userId)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    blockedUsers = _uiState.value.blockedUsers.filter {
                        it.blockedUserId != userId
                    }
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
