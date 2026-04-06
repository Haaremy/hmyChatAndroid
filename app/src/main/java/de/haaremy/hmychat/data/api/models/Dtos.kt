package de.haaremy.hmychat.data.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val isOnline: Boolean = false
)

@Serializable
data class ChatMemberDto(
    val userId: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val role: String = "member",
    val joinedAt: String? = null
)

@Serializable
data class ChatDto(
    val id: String,
    val name: String? = null,
    val isGroup: Boolean = false,
    val members: List<ChatMemberDto> = emptyList(),
    val lastMessage: MessageDto? = null,
    val unreadCount: Int = 0,
    val createdAt: String,
    val updatedAt: String? = null
)

@Serializable
data class MessageDto(
    val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String? = null,
    val content: String,
    val type: String = "text",
    val status: String = "sent",
    val replyToId: String? = null,
    val replyTo: MessageDto? = null,
    val forwardedFromId: String? = null,
    val forwardedFromName: String? = null,
    val isEdited: Boolean = false,
    val createdAt: String,
    val updatedAt: String? = null
)

@Serializable
data class CreateChatRequest(
    val memberIds: List<String>,
    val name: String? = null,
    val isGroup: Boolean = false
)

@Serializable
data class SendMessageRequest(
    val chatId: String,
    val content: String,
    val replyToId: String? = null
)

@Serializable
data class EditMessageRequest(
    val content: String
)

@Serializable
data class ForwardMessageRequest(
    val targetChatId: String
)

@Serializable
data class MarkReadRequest(
    val messageIds: List<String>
)

@Serializable
data class PollResponseDto(
    val messages: List<MessageDto> = emptyList(),
    val typingUsers: Map<String, List<TypingUserDto>> = emptyMap(),
    val onlineUsers: List<String> = emptyList()
)

@Serializable
data class TypingUserDto(
    val userId: String,
    val displayName: String
)

@Serializable
data class BlockDto(
    val id: String,
    val blockedUserId: String,
    val blockedUserName: String? = null,
    val createdAt: String
)

@Serializable
data class CreateBlockRequest(
    val userId: String
)

@Serializable
data class TypingRequest(
    val isTyping: Boolean = true
)

@Serializable
data class AuthTokenRequest(
    val code: String,
    val codeVerifier: String
)

@Serializable
data class AuthTokenResponse(
    val token: String,
    val userId: String,
    val displayName: String? = null
)

@Serializable
data class IdentityKeyRequest(
    val identityKey: String
)

@Serializable
data class PreKeysRequest(
    val preKeys: List<PreKeyDto>
)

@Serializable
data class PreKeyDto(
    val keyId: Int,
    val publicKey: String
)

@Serializable
data class SignedPreKeyRequest(
    val keyId: Int,
    val publicKey: String,
    val signature: String
)

@Serializable
data class KeyBundleDto(
    val identityKey: String,
    val signedPreKey: SignedPreKeyDto,
    val preKey: PreKeyDto? = null
)

@Serializable
data class SignedPreKeyDto(
    val keyId: Int,
    val publicKey: String,
    val signature: String
)

// App Auth (via chat backend → SSO)

@Serializable
data class AppLoginRequest(
    @SerialName("username_or_email") val usernameOrEmail: String,
    val password: String,
    @SerialName("totp_code") val totpCode: String? = null,
    @SerialName("client_id") val clientId: String
)

@Serializable
data class AppRegisterRequest(
    val username: String,
    val password: String,
    val email: String? = null,
    val clearname: String? = null,
    @SerialName("eula_accepted") val eulaAccepted: Boolean = true,
    @SerialName("client_id") val clientId: String
)

@Serializable
data class ChatListResponse(
    val chats: List<ChatDto>
)

@Serializable
data class MessagesResponse(
    val messages: List<MessageDto>
)

@Serializable
data class BlocksResponse(
    val blocks: List<BlockDto>
)
