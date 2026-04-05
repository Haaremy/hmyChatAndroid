package de.haaremy.hmychat.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val isGroup: Boolean,
    val membersJson: String,
    val lastMessageId: String?,
    val lastMessageContent: String?,
    val lastMessageSenderName: String?,
    val lastMessageTimestamp: String?,
    val unreadCount: Int,
    val createdAt: String,
    val updatedAt: String?
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String?,
    val content: String,
    val type: String,
    val status: String,
    val replyToId: String?,
    val replyToContent: String?,
    val replyToSenderName: String?,
    val forwardedFromId: String?,
    val forwardedFromName: String?,
    val isEdited: Boolean,
    val createdAt: String,
    val updatedAt: String?
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?
)
