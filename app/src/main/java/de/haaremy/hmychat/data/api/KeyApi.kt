package de.haaremy.hmychat.data.api

import de.haaremy.hmychat.data.api.models.*
import retrofit2.http.*

interface KeyApi {

    @POST("api/keys/identity")
    suspend fun uploadIdentityKey(@Body request: IdentityKeyRequest)

    @POST("api/keys/prekeys")
    suspend fun uploadPreKeys(@Body request: PreKeysRequest)

    @POST("api/keys/signed-prekey")
    suspend fun uploadSignedPreKey(@Body request: SignedPreKeyRequest)

    @GET("api/keys/bundle/{userId}")
    suspend fun getKeyBundle(@Path("userId") userId: String): KeyBundleDto
}
