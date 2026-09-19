package com.example.test2.data.remote

import com.example.test2.data.dto.AuthResponseDto
import com.example.test2.data.dto.RefreshTokenRequestDto
import com.example.test2.data.local.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.IOException

interface RefreshApi {
    @POST("api/auth/refresh")
    fun refresh(@Body body: RefreshTokenRequestDto): Call<AuthResponseDto>
}

class TokenAuthenticator(
    private val sessionStore: TokenStore,
    private val refreshApi: RefreshApi,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {

        if (response.request.url.encodedPath.contains("/api/auth/")) return null

        val failedToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()

        synchronized(lock) {

            val current = runBlocking { sessionStore.tokenOnce() }

            if (!current.isNullOrBlank() && current != failedToken) {
                return response.request.signedWith(current)
            }

            val refreshToken = runBlocking { sessionStore.refreshTokenOnce() }
            if (refreshToken.isNullOrBlank()) {
                endSession()
                return null
            }

            val refreshed = try {
                refreshApi.refresh(RefreshTokenRequestDto(refreshToken)).execute()
            } catch (e: IOException) {
                return null
            }

            val body = refreshed.body()
            val newToken = body?.token

            if (!refreshed.isSuccessful || newToken.isNullOrBlank()) {
                endSession()
                return null
            }

            runBlocking { sessionStore.updateTokens(newToken, body.refreshToken) }

            return response.request.signedWith(newToken)
        }
    }

    private fun endSession() {
        runBlocking { sessionStore.clear() }
    }

    private fun Request.signedWith(token: String): Request =
        newBuilder().header("Authorization", "Bearer $token").build()

    private fun priorResponseCount(response: Response): Int {
        var count = 0
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
