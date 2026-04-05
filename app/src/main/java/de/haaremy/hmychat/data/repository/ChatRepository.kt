package de.haaremy.hmychat.data.repository

import de.haaremy.hmychat.data.api.ChatApi
import de.haaremy.hmychat.data.api.models.*
import de.haaremy.hmychat.data.db.dao.ChatDao
import de.haaremy.hmychat.data.db.dao.MessageDao
import de.haaremy.hmychat.data.db.entity.ChatEntity
import de.haaremy.hmychat.data.db.entity.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatApi: ChatApi,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun observeChats(): Flow<List<ChatEntity>> = chatDao.observeAll()

    fun observeMessages(chatId: String): Flow<List<MessageEntity>> =
        messageDao.observeByChatId(chatId)

    suspend fun refreshChats(): Result<List<ChatDto>> = withContext(Dispatchers.IO) {
        try {
            val chats = chatApi.getChats()
            val entities = chats.map { it.toEntity() }
            chatDao.upsertAll(entities)
            Result.success(chats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createChat(memberIds: List<String>, name: String?, isGroup: Boolean): Result<ChatDto> =
        withContext(Dispatchers.IO) {
            try {
                val chat = chatApi.createChat(
                    CreateChatRequest(memberIds = memberIds, name = name, isGroup = isGroup)
                )
                chatDao.upsert(chat.toEntity())
                Result.success(chat)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun fetchMessages(chatId: String): Result<List<MessageDto>> =
        withContext(Dispatchers.IO) {
            try {
                val messages = chatApi.getMessages(chatId)
                val entities = messages.map { it.toEntity() }
                messageDao.upsertAll(entities)
                Result.success(messages)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun sendMessage(chatId: String, content: String, replyToId: String? = null): Result<MessageDto> =
        withContext(Dispatchers.IO) {
            try {
                val message = chatApi.sendMessage(
                    SendMessageRequest(chatId = chatId, content = content, replyToId = replyToId)
                )
                messageDao.upsert(message.toEntity())
                Result.success(message)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun editMessage(messageId: String, content: String): Result<MessageDto> =
        withContext(Dispatchers.IO) {
            try {
                val message = chatApi.editMessage(messageId, EditMessageRequest(content))
                messageDao.upsert(message.toEntity())
                Result.success(message)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deleteMessage(messageId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            chatApi.deleteMessage(messageId)
            messageDao.deleteById(messageId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forwardMessage(messageId: String, targetChatId: String): Result<MessageDto> =
        withContext(Dispatchers.IO) {
            try {
                val message = chatApi.forwardMessage(
                    messageId,
                    ForwardMessageRequest(targetChatId)
                )
                messageDao.upsert(message.toEntity())
                Result.success(message)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun markRead(messageIds: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            chatApi.markMessagesRead(MarkReadRequest(messageIds))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun poll(since: String, activeChat: String? = null): Result<PollResponseDto> =
        withContext(Dispatchers.IO) {
            try {
                val response = chatApi.poll(since, activeChat)
                if (response.messages.isNotEmpty()) {
                    messageDao.upsertAll(response.messages.map { it.toEntity() })
                }
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getBlocks(): Result<List<BlockDto>> = withContext(Dispatchers.IO) {
        try {
            Result.success(chatApi.getBlocks())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun blockUser(userId: String): Result<BlockDto> = withContext(Dispatchers.IO) {
        try {
            Result.success(chatApi.createBlock(CreateBlockRequest(userId)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unblockUser(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            chatApi.deleteBlock(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendTypingIndicator(chatId: String) {
        withContext(Dispatchers.IO) {
            try {
                chatApi.sendTypingIndicator(chatId)
            } catch (_: Exception) {
                // Typing indicators are best-effort
            }
        }
    }

    suspend fun clearUnreadCount(chatId: String) {
        chatDao.clearUnread(chatId)
    }

    private fun ChatDto.toEntity(): ChatEntity = ChatEntity(
        id = id,
        name = name,
        isGroup = isGroup,
        membersJson = json.encodeToString(members),
        lastMessageId = lastMessage?.id,
        lastMessageContent = lastMessage?.content,
        lastMessageSenderName = lastMessage?.senderName,
        lastMessageTimestamp = lastMessage?.createdAt,
        unreadCount = unreadCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun MessageDto.toEntity(): MessageEntity = MessageEntity(
        id = id,
        chatId = chatId,
        senderId = senderId,
        senderName = senderName,
        content = content,
        type = type,
        status = status,
        replyToId = replyToId,
        replyToContent = replyTo?.content,
        replyToSenderName = replyTo?.senderName,
        forwardedFromId = forwardedFromId,
        forwardedFromName = forwardedFromName,
        isEdited = isEdited,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
