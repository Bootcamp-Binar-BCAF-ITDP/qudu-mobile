package com.example.test2.data.remote

import com.example.test2.data.dto.AuthResponseDto
import com.example.test2.data.dto.RefreshTokenRequestDto
import com.example.test2.data.local.SessionStore
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

/**
 * Renews an expired access token and replays the request that hit 401.
 *
 * An OkHttp Authenticator rather than an Interceptor, because OkHttp only calls
 * this on a 401 and hands back the original request to retry. Doing it in an
 * interceptor would mean rebuilding that retry by hand.
 *
 * The customer sees nothing: the screen that was loading simply loads.
 */
class TokenAuthenticator(
    private val sessionStore: SessionStore,
    private val refreshApi: RefreshApi,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {

        // Already retried once with a fresh token and still 401. Renewing again
        // would loop, so let the failure through.
        if (priorResponseCount(response) >= 1) return null

        // Sign-in and refresh answer 401 for a wrong password or a dead refresh
        // token. Those are real answers, not expiry, and must not be retried.
        if (response.request.url.encodedPath.contains("/api/auth/")) return null

        val failedToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?.trim()

        synchronized(lock) {

            val current = runBlocking { sessionStore.tokenOnce() }

            // Another request refreshed while this one waited on the lock.
            // Reuse that result instead of burning a second refresh token.
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
                // Offline, not expired. Failing the call is right; ending the
                // session here would sign the customer out for losing signal.
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

    /**
     * Clearing the store is the whole signal. The session Flow emits null, the
     * shell observes it and sends the customer to Login, so no extra callback
     * is needed and there is only one path out of a session.
     */
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
