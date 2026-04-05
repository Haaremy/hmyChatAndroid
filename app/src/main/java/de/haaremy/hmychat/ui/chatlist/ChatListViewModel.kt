package de.haaremy.hmychat.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.haaremy.hmychat.data.db.entity.ChatEntity
import de.haaremy.hmychat.data.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class ChatListUiState(
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    val chats: StateFlow<List<ChatEntity>> = chatRepository.observeChats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var lastPollTimestamp = Instant.now().toString()
    private var isPolling = false

    init {
        refreshChats()
        startPolling()
    }

    fun refreshChats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = chatRepository.refreshChats()
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    private fun startPolling() {
        if (isPolling) return
        isPolling = true
        viewModelScope.launch {
            while (isPolling) {
                delay(3000)
                try {
                    val result = chatRepository.poll(lastPollTimestamp)
                    result.onSuccess {
                        lastPollTimestamp = Instant.now().toString()
                        if (it.messages.isNotEmpty()) {
                            chatRepository.refreshChats()
                        }
                    }
                } catch (_: Exception) {
                    // Continue polling even on failure
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        isPolling = false
    }
}
