package com.example.test2.data.remote

import com.example.test2.data.local.TokenStore
import com.example.test2.testing.respond
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TokenAuthenticatorTest {

    private class FakeTokenStore(
        var token: String? = "old-access",
        var refresh: String? = "old-refresh",
    ) : TokenStore {
        var cleared = 0
        val updates = mutableListOf<Pair<String, String?>>()

        override suspend fun tokenOnce(): String? = token
        override suspend fun refreshTokenOnce(): String? = refresh

        override suspend fun updateTokens(token: String, refreshToken: String?) {
            updates += token to refreshToken
            this.token = token
            if (!refreshToken.isNullOrBlank()) refresh = refreshToken
        }

        override suspend fun clear() {
            cleared++
            token = null
            refresh = null
        }
    }

    private lateinit var server: MockWebServer
    private lateinit var store: FakeTokenStore
    private lateinit var authenticator: TokenAuthenticator

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        store = FakeTokenStore()

        val refreshApi = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RefreshApi::class.java)

        authenticator = TokenAuthenticator(store, refreshApi)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun unauthorized(
        path: String = "/api/customer/profile",
        token: String? = "old-access",
        prior: Response? = null,
    ): Response {
        val request = Request.Builder()
            .url(server.url(path))
            .apply { if (token != null) header("Authorization", "Bearer $token") }
            .build()

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .priorResponse(prior)
            .build()
    }

    @Test
    fun `an expired token is renewed and the request replayed with the new one`() {
        server.respond(200, """{"token":"new-access","refreshToken":"new-refresh"}""")

        val retry = authenticator.authenticate(null, unauthorized())

        assertNotNull(retry)
        assertEquals("Bearer new-access", retry!!.header("Authorization"))
    }

    @Test
    fun `the replay goes to the same url as the request that failed`() {
        server.respond(200, """{"token":"new-access","refreshToken":"new-refresh"}""")

        val retry = authenticator.authenticate(null, unauthorized("/api/customer/loans"))

        assertEquals("/api/customer/loans", retry!!.url.encodedPath)
    }

    @Test
    fun `the refresh sends the stored refresh token to the refresh endpoint`() {
        server.respond(200, """{"token":"new-access","refreshToken":"new-refresh"}""")

        authenticator.authenticate(null, unauthorized())

        val recorded = server.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("/api/auth/refresh", recorded.path)
        assertEquals("POST", recorded.method)
        assertTrue(recorded.body.readUtf8().contains("\"refreshToken\":\"old-refresh\""))
    }

    @Test
    fun `the rotated pair is stored, or the next refresh would replay a spent token`() {
        server.respond(200, """{"token":"new-access","refreshToken":"new-refresh"}""")

        authenticator.authenticate(null, unauthorized())

        assertEquals(listOf("new-access" to "new-refresh"), store.updates)
        assertEquals("new-refresh", store.refresh)
    }

    @Test
    fun `a response without a new refresh token keeps the old one`() {
        server.respond(200, """{"token":"new-access"}""")

        authenticator.authenticate(null, unauthorized())

        assertEquals("old-refresh", store.refresh)
        assertEquals("new-access", store.token)
    }

    @Test
    fun `a second 401 after a retry is let through instead of looping`() {
        val result = authenticator.authenticate(null, unauthorized(prior = unauthorized()))

        assertNull(result)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `a 401 from sign in is a wrong password, not an expiry, and is not retried`() {
        val result = authenticator.authenticate(null, unauthorized("/api/auth/login"))

        assertNull(result)
        assertEquals(0, server.requestCount)
        assertEquals(0, store.cleared)
    }

    @Test
    fun `a 401 from the refresh endpoint itself is not retried`() {
        val result = authenticator.authenticate(null, unauthorized("/api/auth/refresh"))

        assertNull(result)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `a token already renewed by another request is reused without a second refresh`() {
        store.token = "renewed-by-someone-else"

        val retry = authenticator.authenticate(null, unauthorized(token = "old-access"))

        assertEquals("Bearer renewed-by-someone-else", retry!!.header("Authorization"))
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `with no refresh token stored the session ends`() {
        store.refresh = null

        val result = authenticator.authenticate(null, unauthorized())

        assertNull(result)
        assertEquals(1, store.cleared)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `a blank refresh token ends the session too`() {
        store.refresh = "   "

        assertNull(authenticator.authenticate(null, unauthorized()))
        assertEquals(1, store.cleared)
    }

    @Test
    fun `a refused refresh ends the session, which sends the customer to login`() {
        server.respond(401, """{"message":"Refresh token revoked"}""")

        val result = authenticator.authenticate(null, unauthorized())

        assertNull(result)
        assertEquals(1, store.cleared)
    }

    @Test
    fun `a refresh answer without a token ends the session`() {
        server.respond(200, """{"refreshToken":"only-this"}""")

        assertNull(authenticator.authenticate(null, unauthorized()))
        assertEquals(1, store.cleared)
        assertTrue(store.updates.isEmpty())
    }

    @Test
    fun `losing signal fails the call but keeps the customer signed in`() {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        val result = authenticator.authenticate(null, unauthorized())

        assertNull(result)
        assertEquals(0, store.cleared)
        assertEquals("old-refresh", store.refresh)
    }

    @Test
    fun `a request that carried no token at all is still renewed`() {
        store.token = null
        server.respond(200, """{"token":"new-access","refreshToken":"new-refresh"}""")

        val retry = authenticator.authenticate(null, unauthorized(token = null))

        assertEquals("Bearer new-access", retry!!.header("Authorization"))
    }

    @Test
    fun `the replay carries exactly one authorization header`() {
        server.respond(200, """{"token":"new-access","refreshToken":"new-refresh"}""")

        val retry = authenticator.authenticate(null, unauthorized())

        assertEquals(listOf("Bearer new-access"), retry!!.headers("Authorization"))
    }

    @Test
    fun `parallel 401s spend the refresh token only once`() {
        server.respond(200, """{"token":"new-access","refreshToken":"new-refresh"}""")

        val threads = 6
        val start = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(threads)
        val results = (1..threads).map {
            pool.submit<Request?> {
                start.await()
                authenticator.authenticate(null, unauthorized())
            }
        }

        start.countDown()
        val retries = results.map { it.get(5, TimeUnit.SECONDS) }
        pool.shutdown()

        assertEquals(1, server.requestCount)
        retries.forEach { assertEquals("Bearer new-access", it!!.header("Authorization")) }
    }
}
