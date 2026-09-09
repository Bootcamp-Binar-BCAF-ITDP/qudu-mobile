package com.example.test2.data.repository

import com.example.test2.core.Outcome
import com.example.test2.data.local.Session
import com.example.test2.data.local.SessionStore
import com.example.test2.data.remote.ApiService
import com.example.test2.data.dto.DeviceTokenRequestDto
import com.example.test2.data.dto.ForgotPasswordRequestDto
import com.example.test2.data.dto.LoginRequestDto
import com.example.test2.data.dto.RegisterRequestDto
import com.example.test2.data.dto.RegistrationOtpRequestDto
import com.example.test2.data.dto.ResetPasswordRequestDto
import com.example.test2.messaging.currentFcmToken

class AuthRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore,
) {

    val session = sessionStore.session

    suspend fun register(body: RegisterRequestDto): Outcome<Unit> =
        when (val result = apiCall { api.register(body) }) {
            is Outcome.Success -> Outcome.Success(Unit)
            is Outcome.Failure -> result
        }

    /**
     * Step 1 of signup: ask the backend to email a verification code.
     *
     * A failure carrying [com.example.test2.core.HTTP_ALREADY_REGISTERED] means
     * the address already has an account - the one failure the register screen
     * answers with a route to login rather than a red message.
     */
    suspend fun requestRegistrationOtp(email: String): Outcome<Unit> =
        when (val result = apiCall {
            api.requestRegistrationOtp(RegistrationOtpRequestDto(email.trim()))
        }) {
            is Outcome.Success -> Outcome.Success(Unit)
            is Outcome.Failure -> result
        }

    suspend fun requestPasswordReset(email: String): Outcome<Unit> =
        when (val result = apiCall { api.forgotPassword(ForgotPasswordRequestDto(email.trim())) }) {
            is Outcome.Success -> Outcome.Success(Unit)
            is Outcome.Failure -> result
        }

    suspend fun resetPassword(
        email: String,
        code: String,
        newPassword: String,
        confirmPassword: String,
    ): Outcome<Unit> {

        val body = ResetPasswordRequestDto(
            email = email.trim(),
            token = code.trim(),
            newPassword = newPassword,
            confirmPassword = confirmPassword,
        )

        return when (val result = apiCall { api.resetPassword(body) }) {
            is Outcome.Success -> Outcome.Success(Unit)
            is Outcome.Failure -> result
        }
    }

    suspend fun login(email: String, password: String): Outcome<Session> {

        val result = apiCall {
            api.login(LoginRequestDto(usernameOrEmail = email, password = password))
        }

        return when (result) {
            is Outcome.Failure -> result
            is Outcome.Success -> {
                val dto = result.value

                if (dto.token.isNullOrBlank() || dto.userId.isNullOrBlank()) {
                    return Outcome.Failure("The server did not return a valid token.")
                }

                val session = Session(
                    token = dto.token,
                    customerId = dto.userId,
                    fullName = dto.fullName.orEmpty(),
                    email = dto.email ?: email,
                )

                sessionStore.save(session)

                syncDeviceToken()

                Outcome.Success(session)
            }
        }
    }

    suspend fun logout() {
        try {
            val token = currentFcmToken()
            api.removeDeviceToken(token)
        } catch (e: Exception) {
        }
        sessionStore.clear()
    }

    suspend fun syncDeviceToken(): Outcome<Unit> =
        try {
            val token = currentFcmToken()

            when (val result = apiCall { api.registerDeviceToken(DeviceTokenRequestDto(token)) }) {
                is Outcome.Success -> Outcome.Success(Unit)
                is Outcome.Failure -> result
            }
        } catch (e: Exception) {
            Outcome.Failure(
                "Could not register this device for notifications: ${e.message}",
            )
        }
}
