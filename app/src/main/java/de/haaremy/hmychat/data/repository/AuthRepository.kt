package de.haaremy.hmychat.data.repository

import de.haaremy.hmychat.data.api.ChatApi
import de.haaremy.hmychat.data.api.SsoApi
import de.haaremy.hmychat.data.api.models.AppLoginRequest
import de.haaremy.hmychat.data.api.models.AppRegisterRequest
import de.haaremy.hmychat.data.api.models.AppAuthResponse
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
    private val ssoApi: SsoApi,
    private val tokenStore: TokenStore,
    private val database: AppDatabase
) {
    val isLoggedIn: Boolean
        get() = tokenStore.isLoggedIn

    val currentUserId: String?
        get() = tokenStore.userId

    val currentDisplayName: String?
        get() = tokenStore.displayName

    companion object {
        const val CLIENT_ID = "0_j6GmSljSuOVOEXg5Gz-A"
    }

    suspend fun login(usernameOrEmail: String, password: String, totpCode: String? = null): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ssoApi.appLogin(
                    AppLoginRequest(
                        usernameOrEmail = usernameOrEmail,
                        password = password,
                        totpCode = totpCode,
                        clientId = CLIENT_ID
                    )
                )
                storeAuthResponse(response)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(parseError(e))
            }
        }
    }

    suspend fun register(
        username: String,
        password: String,
        email: String?,
        clearname: String?
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = ssoApi.appRegister(
                    AppRegisterRequest(
                        username = username,
                        password = password,
                        email = email,
                        clearname = clearname,
                        eulaAccepted = true,
                        clientId = CLIENT_ID
                    )
                )
                storeAuthResponse(response)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(parseError(e))
            }
        }
    }

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

    private fun storeAuthResponse(response: AppAuthResponse) {
        tokenStore.bearerToken = response.accessToken
        tokenStore.userId = response.userId
        tokenStore.displayName = response.displayName ?: response.username
    }

    private fun parseError(e: Exception): Exception {
        if (e is retrofit2.HttpException) {
            val body = e.response()?.errorBody()?.string()
            if (body != null) {
                try {
                    val detail = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                        .decodeFromString<ErrorResponse>(body)
                    return Exception(detail.detail)
                } catch (_: Exception) {}
            }
            return when (e.code()) {
                401 -> Exception("Ungueltige Anmeldedaten.")
                409 -> Exception("Benutzername oder E-Mail bereits vergeben.")
                422 -> Exception("Bitte ueberprüfe deine Eingaben.")
                429 -> Exception("Zu viele Versuche. Bitte warte einen Moment.")
                else -> Exception("Serverfehler (${e.code()})")
            }
        }
        return Exception("Verbindungsfehler. Bitte pruefe deine Internetverbindung.")
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            tokenStore.clear()
            database.chatDao().deleteAll()
            database.messageDao().deleteAll()
        }
    }
}

@kotlinx.serialization.Serializable
private data class ErrorResponse(val detail: String = "")
