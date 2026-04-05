package de.haaremy.hmychat.data.repository

import de.haaremy.hmychat.data.api.ChatApi
import de.haaremy.hmychat.data.api.models.AuthTokenRequest
import de.haaremy.hmychat.data.db.AppDatabase
import de.haaremy.hmychat.data.local.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val chatApi: ChatApi,
    private val tokenStore: TokenStore,
    private val database: AppDatabase
) {
    val isLoggedIn: Boolean
        get() = tokenStore.isLoggedIn

    val currentUserId: String?
        get() = tokenStore.userId

    val currentDisplayName: String?
        get() = tokenStore.displayName

    suspend fun exchangeCodeForToken(code: String, codeVerifier: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = chatApi.exchangeToken(
                    AuthTokenRequest(code = code, codeVerifier = codeVerifier)
                )
                tokenStore.bearerToken = response.token
                tokenStore.userId = response.userId
                tokenStore.displayName = response.displayName
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            tokenStore.clear()
            database.chatDao().deleteAll()
            database.messageDao().deleteAll()
        }
    }
}
