package com.example.test2.data.repository

import com.example.test2.core.Outcome
import com.example.test2.data.dto.ApiEnvelope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class ApiCallTest {

    private val json = "application/json".toMediaType()

    private fun <T> error(code: Int, body: String): Response<T> =
        Response.error(code, body.toResponseBody(json))

    private suspend fun <T> call(block: suspend () -> Response<T>): Outcome<T> =
        apiCall(Dispatchers.Unconfined, block)

    @Test
    fun `a successful response with a body is a success`() = runTest {
        assertEquals(Outcome.Success("ok"), call { Response.success("ok") })
    }

    @Test
    fun `a successful response with no body is a failure that keeps the code`() = runTest {
        val result = call<String> { Response.success(204, null as String?) }

        assertEquals(Outcome.Failure("The server returned an empty response.", 204), result)
    }

    @Test
    fun `an error takes the message the server wrote`() = runTest {
        val result = call<String> { error(422, """{"message":"Amount is over your plafond."}""") }

        assertEquals(Outcome.Failure("Amount is over your plafond.", 422), result)
    }

    @Test
    fun `a 401 without a message says the session expired`() = runTest {
        val result = call<String> { error(401, "") }

        assertEquals(Outcome.Failure("Your session has expired. Please sign in again.", 401), result)
    }

    @Test
    fun `a 403 and a 404 have their own wording`() = runTest {
        assertEquals("You do not have access to this action.", (call<String> { error(403, "") } as Outcome.Failure).message)
        assertEquals("Not found.", (call<String> { error(404, "") } as Outcome.Failure).message)
    }

    @Test
    fun `any other status names the code`() = runTest {
        assertEquals("Request failed (HTTP 503).", (call<String> { error(503, "") } as Outcome.Failure).message)
    }

    @Test
    fun `a blank message in the body falls back to the default wording`() = runTest {
        val result = call<String> { error(500, """{"message":"   "}""") }

        assertEquals("Request failed (HTTP 500).", (result as Outcome.Failure).message)
    }

    @Test
    fun `a body that is not json falls back rather than crashing`() = runTest {
        val result = call<String> { error(502, "<html>Bad Gateway</html>") }

        assertEquals(Outcome.Failure("Request failed (HTTP 502).", 502), result)
    }

    @Test
    fun `a json array body falls back rather than crashing`() = runTest {
        val result = call<String> { error(400, """["a","b"]""") }

        assertEquals("Request failed (HTTP 400).", (result as Outcome.Failure).message)
    }

    @Test
    fun `a network failure says the server is unreachable and carries no code`() = runTest {
        val result = call<String> { throw IOException("timeout") }

        assertEquals(Outcome.Failure("Cannot reach the server. Check your connection"), result)
    }

    @Test
    fun `any other exception surfaces its own message`() = runTest {
        val result = call<String> { throw IllegalStateException("parse error") }

        assertEquals(Outcome.Failure("parse error"), result)
    }

    @Test
    fun `an exception without a message gets a generic one`() = runTest {
        val result = call<String> { throw RuntimeException() }

        assertTrue((result as Outcome.Failure).message.startsWith("Something went wrong"))
    }

    @Test
    fun `an envelope with data unwraps to the data`() {
        val result: Outcome<ApiEnvelope<String>> = Outcome.Success(ApiEnvelope(data = "payload"))

        assertEquals(Outcome.Success("payload"), result.unwrapEnvelope())
    }

    @Test
    fun `an envelope without data becomes a failure with the server message`() {
        val result: Outcome<ApiEnvelope<String>> = Outcome.Success(ApiEnvelope(message = "Nothing here"))

        assertEquals(Outcome.Failure("Nothing here"), result.unwrapEnvelope())
    }

    @Test
    fun `an envelope with neither data nor message gets the empty response wording`() {
        val result: Outcome<ApiEnvelope<String>> = Outcome.Success(ApiEnvelope())

        assertEquals(Outcome.Failure("The server returned an empty response."), result.unwrapEnvelope())
    }

    @Test
    fun `a failure passes through unwrapping untouched`() {
        val failure = Outcome.Failure("offline", 503)
        val result: Outcome<ApiEnvelope<String>> = failure

        assertEquals(failure, result.unwrapEnvelope())
    }

    @Test
    fun `the unreadable file message names the document in English`() {
        assertEquals(
            "The Payslip file could not be read. Please pick it again.",
            unreadableFileMessage("SLIP_GAJI"),
        )
    }
}
