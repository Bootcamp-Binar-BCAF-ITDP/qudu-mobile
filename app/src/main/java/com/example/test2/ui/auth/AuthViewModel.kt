package com.example.test2.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.core.HTTP_ALREADY_REGISTERED
import com.example.test2.core.Outcome
import com.example.test2.data.dto.BranchDto
import com.example.test2.data.dto.RegisterRequestDto
import com.example.test2.data.repository.AuthRepository
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

const val RESET_CODE_LENGTH = 6

const val REGISTRATION_OTP_LENGTH = 6

const val MIN_RESET_PASSWORD_LENGTH = 8

@HiltViewModel
class AuthViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {

    var isBusy by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var info by mutableStateOf<String?>(null)
        private set

    var branches by mutableStateOf<List<BranchDto>>(emptyList())
        private set

    fun loadBranches() {
        if (branches.isNotEmpty()) return

        viewModelScope.launch {
            when (val result = repository.branches()) {
                is Outcome.Success -> branches = result.value
                is Outcome.Failure -> Unit
            }
        }
    }

    fun clearMessages() {
        error = null
        info = null
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {

        if (email.isBlank() || password.isBlank()) {
            error = "Email and password are required."
            return
        }

        run(
            block = { repository.login(email.trim(), password) },
            onSuccess = { onSuccess() },
        )
    }

    fun requestPasswordReset(email: String, onSent: () -> Unit) {

        val trimmed = email.trim()

        if (trimmed.isBlank() || !trimmed.contains("@")) {
            error = "Enter a valid email address."
            return
        }

        run(
            block = { repository.requestPasswordReset(trimmed) },
            onSuccess = {
                info = "If that email is registered, a code is on its way."
                onSent()
            },
        )
    }

    fun resetPassword(
        email: String,
        code: String,
        newPassword: String,
        confirmPassword: String,
        onSuccess: () -> Unit,
    ) {
        val validationError = when {
            code.isBlank() -> "The code from the email is required."
            code.trim().length != RESET_CODE_LENGTH -> "The code is $RESET_CODE_LENGTH digits long."
            newPassword.length < MIN_RESET_PASSWORD_LENGTH ->
                "The new password must be at least $MIN_RESET_PASSWORD_LENGTH characters."
            newPassword != confirmPassword -> "The password confirmation does not match."
            else -> null
        }

        if (validationError != null) {
            error = validationError
            return
        }

        run(
            block = { repository.resetPassword(email, code, newPassword, confirmPassword) },
            onSuccess = {
                info = "Password changed. Please sign in."
                onSuccess()
            },
        )
    }

    fun requestRegistrationOtp(
        form: RegisterForm,
        onSent: () -> Unit,
        onAlreadyRegistered: () -> Unit,
    ) {
        val validationError = form.firstError()
        if (validationError != null) {
            error = validationError
            return
        }

        if (isBusy) return

        clearMessages()
        isBusy = true

        viewModelScope.launch {
            when (val result = repository.requestRegistrationOtp(form.email)) {
                is Outcome.Success -> {
                    info = "Verification code sent to ${form.email.trim()}."
                    onSent()
                }

                is Outcome.Failure ->
                    if (result.code == HTTP_ALREADY_REGISTERED) {
                        info = "Email ${form.email.trim()} is already registered. Please sign in."
                        onAlreadyRegistered()
                    } else {
                        error = result.message
                    }
            }
            isBusy = false
        }
    }

    fun register(form: RegisterForm, otp: String, onSuccess: () -> Unit) {

        val validationError = form.firstError() ?: when {
            otp.isBlank() -> "The verification code is required."
            otp.trim().length != REGISTRATION_OTP_LENGTH ->
                "The code is $REGISTRATION_OTP_LENGTH digits long."
            else -> null
        }

        if (validationError != null) {
            error = validationError
            return
        }

        run(
            block = { repository.register(form.toDto(otp.trim())) },
            onSuccess = {
                info = "Registration complete. Please sign in."
                onSuccess()
            },
        )
    }

    private fun <T> run(
        block: suspend () -> Outcome<T>,
        onSuccess: (T) -> Unit,
    ) {
        if (isBusy) return

        clearMessages()
        isBusy = true

        viewModelScope.launch {
            when (val result = block()) {
                is Outcome.Success -> onSuccess(result.value)
                is Outcome.Failure -> error = result.message
            }
            isBusy = false
        }
    }
}

data class RegisterForm(
    val branchId: Int? = null,
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val phoneNumber: String = "",
    val nik: String = "",
    val address: String = "",
    val sex: String = "MALE",
    val birthPlace: String = "",
    val birthDate: String = "",
    val occupation: String = "",
    val citizenship: String = "WNI",
) {

    fun firstError(): String? = when {
        branchId == null -> "Please choose the branch nearest to you."
        fullName.isBlank() -> "Full name is required."
        email.isBlank() -> "Email is required."
        !email.contains("@") -> "That email address is not valid."
        password.length < 6 -> "The password must be at least 6 characters."
        phoneNumber.isBlank() -> "Phone number is required."
        nik.length != 16 -> "The NIK must be 16 digits."
        address.isBlank() -> "Address is required."
        birthPlace.isBlank() -> "Place of birth is required."
        !BIRTH_DATE.matches(birthDate) -> "Please select your date of birth."
        occupation.isBlank() -> "Occupation is required."
        citizenship.isBlank() -> "Please select your citizenship."
        else -> null
    }

    fun toDto(otp: String) = RegisterRequestDto(
        branchId = branchId,
        otp = otp,
        email = email.trim(),
        password = password,
        fullName = fullName.trim(),
        phoneNumber = phoneNumber.trim(),
        nik = nik.trim(),
        address = address.trim(),
        sex = sex,
        birthPlace = birthPlace.trim(),
        birthDate = birthDate.trim(),
        occupation = occupation.trim(),
        citizenship = citizenship.trim(),
    )

    private companion object {
        val BIRTH_DATE = Regex("""\d{4}-\d{2}-\d{2}""")
    }
}
