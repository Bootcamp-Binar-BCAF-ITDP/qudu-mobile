package com.example.test2.data.repository

import com.example.test2.core.Outcome
import com.example.test2.data.local.Session
import com.example.test2.data.local.room.CustomerProfileDao
import com.example.test2.data.local.room.LoanApplicationDao
import com.example.test2.data.local.room.PlafondRequestDao
import com.example.test2.data.local.SessionStore
import com.example.test2.data.remote.ApiService
import com.example.test2.data.dto.BranchDto
import com.example.test2.data.dto.DeviceTokenRequestDto
import com.example.test2.data.dto.ForgotPasswordRequestDto
import com.example.test2.data.dto.LoginRequestDto
import com.example.test2.data.dto.RefreshTokenRequestDto
import com.example.test2.data.dto.RegisterRequestDto
import com.example.test2.data.dto.RegistrationOtpRequestDto
import com.example.test2.data.dto.ResetPasswordRequestDto
import com.example.test2.messaging.currentFcmToken

class AuthRepository(
    private val api: ApiService,
    private val sessionStore: SessionStore,
    private val applicationDao: LoanApplicationDao,
    private val plafondRequestDao: PlafondRequestDao,
    private val profileDao: CustomerProfileDao,
) {

    val session = sessionStore.session

    suspend fun branches(): Outcome<List<BranchDto>> =
        when (val result = apiCall { api.branchOptions() }) {
            is Outcome.Success -> Outcome.Success(result.value.data.orEmpty())
            is Outcome.Failure -> result
        }

    suspend fun register(body: RegisterRequestDto): Outcome<Unit> =
        when (val result = apiCall { api.register(body) }) {
            is Outcome.Success -> Outcome.Success(Unit)
            is Outcome.Failure -> result
        }

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
                    refreshToken = dto.refreshToken.orEmpty(),
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

        val refreshToken = sessionStore.refreshTokenOnce()
        if (!refreshToken.isNullOrBlank()) {
            try {
                api.logout(RefreshTokenRequestDto(refreshToken))
            } catch (e: Exception) {
            }
        }

        sessionStore.clear()

        applicationDao.deleteAll()
        plafondRequestDao.deleteAll()
        profileDao.deleteAll()
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
