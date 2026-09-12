package com.example.test2.data.repository

import com.example.test2.core.Outcome
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

internal suspend fun <T> apiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    block: suspend () -> Response<T>,
): Outcome<T> = withContext(dispatcher) {
    try {
        val response = block()
        val body = response.body()

        if (response.isSuccessful && body != null) {
            Outcome.Success(body)
        } else if (response.isSuccessful) {
            Outcome.Failure("The server returned an empty response.", response.code())
        } else {
            Outcome.Failure(
                extractMessage(response.errorBody()?.string(), response.code()),
                response.code(),
            )
        }
    } catch (e: IOException) {
        Outcome.Failure(
            "Cannot reach the server. Check your connection",
        )
    } catch (e: Exception) {
        Outcome.Failure(e.message ?: "Something went wrong. Please try again.")
    }
}

private fun extractMessage(errorBody: String?, code: Int): String {
    if (errorBody.isNullOrBlank()) return defaultMessage(code)

    return try {
        val message = JsonParser.parseString(errorBody)
            .asJsonObject
            .get("message")
            ?.asString

        if (message.isNullOrBlank()) defaultMessage(code) else message
    } catch (e: Exception) {
        defaultMessage(code)
    }
}

private fun defaultMessage(code: Int): String = when (code) {
    401 -> "Your session has expired. Please sign in again."
    403 -> "You do not have access to this action."
    404 -> "Not found."
    else -> "Request failed (HTTP $code)."
}
