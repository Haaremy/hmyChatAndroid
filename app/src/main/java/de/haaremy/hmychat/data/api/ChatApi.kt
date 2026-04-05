package de.haaremy.hmychat.data.api

import de.haaremy.hmychat.data.api.models.*
import retrofit2.http.*

interface ChatApi {

    @GET("api/chats")
    suspend fun getChats(): List<ChatDto>

    @POST("api/chats")
    suspend fun createChat(@Body request: CreateChatRequest): ChatDto

    @GET("api/messages")
    suspend fun getMessages(@Query("chatId") chatId: String): List<MessageDto>

    @POST("api/messages")
    suspend fun sendMessage(@Body request: SendMessageRequest): MessageDto

    @PATCH("api/messages/{id}")
    suspend fun editMessage(
        @Path("id") messageId: String,
        @Body request: EditMessageRequest
    ): MessageDto

    @DELETE("api/messages/{id}")
    suspend fun deleteMessage(@Path("id") messageId: String)

    @POST("api/messages/{id}/forward")
    suspend fun forwardMessage(
        @Path("id") messageId: String,
        @Body request: ForwardMessageRequest
    ): MessageDto

    @POST("api/messages/read")
    suspend fun markMessagesRead(@Body request: MarkReadRequest)

    @GET("api/poll")
    suspend fun poll(
        @Query("since") since: String,
        @Query("activeChat") activeChat: String? = null
    ): PollResponseDto

    @GET("api/blocks")
    suspend fun getBlocks(): List<BlockDto>

    @POST("api/blocks")
    suspend fun createBlock(@Body request: CreateBlockRequest): BlockDto

    @DELETE("api/blocks")
    suspend fun deleteBlock(@Query("userId") userId: String)

    @POST("api/chats/{id}/typing")
    suspend fun sendTypingIndicator(
        @Path("id") chatId: String,
        @Body request: TypingRequest = TypingRequest()
    )

    @POST("api/auth/token")
    suspend fun exchangeToken(@Body request: AuthTokenRequest): AuthTokenResponse
}
