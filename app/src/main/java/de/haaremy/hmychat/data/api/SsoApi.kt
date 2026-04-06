package de.haaremy.hmychat.data.api

import de.haaremy.hmychat.data.api.models.*
import retrofit2.http.*

interface SsoApi {

    @POST("api/v1/auth/app-login")
    suspend fun appLogin(@Body request: AppLoginRequest): AppAuthResponse

    @POST("api/v1/auth/app-register")
    suspend fun appRegister(@Body request: AppRegisterRequest): AppAuthResponse
}
