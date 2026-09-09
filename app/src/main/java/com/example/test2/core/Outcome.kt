package com.example.test2.core

sealed interface Outcome<out T> {

    data class Success<T>(val value: T) : Outcome<T>

    data class Failure(val message: String, val code: Int? = null) : Outcome<Nothing>

    val successOrNull: T? get() = (this as? Success)?.value
}

const val HTTP_OVER_PLAFOND = 422
const val HTTP_UNAUTHORIZED = 401

/** The address already has an account - see AuthRepository.requestRegistrationOtp. */
const val HTTP_ALREADY_REGISTERED = 409
