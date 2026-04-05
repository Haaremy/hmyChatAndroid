package de.haaremy.hmychat.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.haaremy.hmychat.data.api.models.TypingUserDto
import de.haaremy.hmychat.data.db.entity.MessageEntity
import de.haaremy.hmychat.data.local.TokenStore
import de.haaremy.hmychat.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class ChatUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val replyToMessage: MessageEntity? = null,
    val editingMessage: MessageEntity? = null,
    val typingUsers: List<TypingUserDto> = emptyList()
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository,
    private val tokenStore: TokenStore
) : ViewModel() {

    val chatId: String = savedStateHandle["chatId"] ?: ""
    val chatName: String = savedStateHandle["chatName"] ?: ""
    val currentUserId: String = tokenStore.userId ?: ""

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    val messages: StateFlow<List<MessageEntity>> = chatRepository.observeMessages(chatId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var pollingJob: Job? = null
    private var lastPollTimestamp = Instant.now().toString()
    private var typingJob: Job? = null

    init {
        loadMessages()
        startPolling()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = chatRepository.fetchMessages(chatId)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    markMessagesAsRead()
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
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(2000)
                try {
                    val result = chatRepository.poll(lastPollTimestamp, chatId)
                    result.onSuccess { response ->
                        lastPollTimestamp = Instant.now().toString()
                        val typing = response.typingUsers[chatId] ?: emptyList()
                        _uiState.value = _uiState.value.copy(
                            typingUsers = typing.filter { it.userId != currentUserId }
                        )
                        if (response.messages.any { it.chatId == chatId }) {
                            markMessagesAsRead()
                        }
                    }
                } catch (_: Exception) {
                    // Continue polling
                }
            }
        }
    }

    fun onInputChanged(text: String) {
        _inputText.value = text
        sendTypingIndicator()
    }

    private fun sendTypingIndicator() {
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            chatRepository.sendTypingIndicator(chatId)
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        val editing = _uiState.value.editingMessage
        val replyToId = _uiState.value.replyToMessage?.id

        viewModelScope.launch {
            if (editing != null) {
                chatRepository.editMessage(editing.id, text)
                _uiState.value = _uiState.value.copy(editingMessage = null)
            } else {
                chatRepository.sendMessage(chatId, text, replyToId)
                _uiState.value = _uiState.value.copy(replyToMessage = null)
            }
            _inputText.value = ""
        }
    }

    fun startReply(message: MessageEntity) {
        _uiState.value = _uiState.value.copy(
            replyToMessage = message,
            editingMessage = null
        )
    }

    fun startEdit(message: MessageEntity) {
        _uiState.value = _uiState.value.copy(
            editingMessage = message,
            replyToMessage = null
        )
        _inputText.value = message.content
    }

    fun cancelReplyOrEdit() {
        _uiState.value = _uiState.value.copy(
            replyToMessage = null,
            editingMessage = null
        )
        if (_uiState.value.editingMessage != null) {
            _inputText.value = ""
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId)
        }
    }

    fun forwardMessage(messageId: String, targetChatId: String) {
        viewModelScope.launch {
            chatRepository.forwardMessage(messageId, targetChatId)
        }
    }

    private fun markMessagesAsRead() {
        viewModelScope.launch {
            val unreadIds = messages.value
                .filter { it.senderId != currentUserId && it.status != "read" }
                .map { it.id }
            if (unreadIds.isNotEmpty()) {
                chatRepository.markRead(unreadIds)
                chatRepository.clearUnreadCount(chatId)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
