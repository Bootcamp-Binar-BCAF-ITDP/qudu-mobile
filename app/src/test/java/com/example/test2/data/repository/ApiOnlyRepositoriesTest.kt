package com.example.test2.data.repository

import com.example.test2.core.Outcome
import com.example.test2.core.PlafondTiers
import com.example.test2.testing.FakeTierDao
import com.example.test2.testing.apiService
import com.example.test2.testing.respond
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PlafondCatalogRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: PlafondCatalogRepository

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        repository = PlafondCatalogRepository(server.apiService(), FakeTierDao())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun tier(level: Int, extra: String = "") =
        """{"level":$level,"description":"L$level","minimumAmount":${level}000000,"maxAmount":${level}999999,""" +
            """"minTenor":1,"maxTenor":12,"interestRate":0.1,"adminFee":5000$extra}"""

    @Test
    fun `it reads the public catalog endpoint`() = runTest {
        server.respond(200, """{"data":[${tier(1)}]}""")

        repository.load()

        assertEquals("/api/plafonds/catalog", server.takeRequest().path)
    }

    @Test
    fun `a server ladder is used and marked live`() = runTest {
        server.respond(200, """{"data":[${tier(1)},${tier(2)}]}""")

        val catalog = repository.load()

        assertTrue(catalog.live)
        assertEquals(listOf(1, 2), catalog.tiers.map { it.level })
    }

    @Test
    fun `the ladder is sorted by level whatever order the server sent`() = runTest {
        server.respond(200, """{"data":[${tier(3)},${tier(1)},${tier(2)}]}""")

        assertEquals(listOf(1, 2, 3), repository.load().tiers.map { it.level })
    }

    @Test
    fun `money and rate fields convert from the server decimals`() = runTest {
        server.respond(200, """{"data":[${tier(2)}]}""")

        val only = repository.load().tiers.single()

        assertEquals(2_000_000L, only.minAmount)
        assertEquals(2_999_999L, only.maxAmount)
        assertEquals(0.1, only.annualRate, 0.0)
        assertEquals(5_000L, only.adminFee)
    }

    @Test
    fun `a tier missing a required field is skipped, not guessed`() = runTest {
        server.respond(
            200,
            """{"data":[${tier(1)},{"level":2,"minimumAmount":1,"maxAmount":2,"minTenor":1,"maxTenor":2}]}""",
        )

        assertEquals(listOf(1), repository.load().tiers.map { it.level })
    }

    @Test
    fun `a tier without a description is named by its level`() = runTest {
        server.respond(
            200,
            """{"data":[{"level":4,"description":"  ","minimumAmount":1,"maxAmount":2,"minTenor":1,"maxTenor":2,"interestRate":0.1}]}""",
        )

        val only = repository.load().tiers.single()

        assertEquals("Level 4", only.name)
        assertEquals(0L, only.adminFee)
    }

    @Test
    fun `a failed request falls back to the built-in ladder, marked not live`() = runTest {
        server.respond(500, """{"message":"down"}""")

        val catalog = repository.load()

        assertFalse(catalog.live)
        assertEquals(PlafondTiers.FALLBACK, catalog.tiers)
    }

    @Test
    fun `a failed request reuses the last server ladder, marked not live`() = runTest {
        server.respond(200, """{"data":[${tier(1)},${tier(2)}]}""")
        repository.load()

        server.respond(500, """{"message":"down"}""")
        val catalog = repository.load()

        assertFalse(catalog.live)
        assertEquals(listOf(1, 2), catalog.tiers.map { it.level })
    }

    @Test
    fun `an empty ladder also falls back`() = runTest {
        server.respond(200, """{"data":[]}""")

        assertFalse(repository.load().live)
    }

    @Test
    fun `a ladder where every tier is broken falls back`() = runTest {
        server.respond(200, """{"data":[{"level":1}]}""")

        assertEquals(PlafondTiers.FALLBACK, repository.load().tiers)
    }

    @Test
    fun `being offline falls back instead of throwing`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertFalse(repository.load().live)
    }
}

class NotificationRepositoryTest {

    private lateinit var server: MockWebServer
    private lateinit var repository: NotificationRepository

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        repository = NotificationRepository(server.apiService())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `the list is read out of the page`() = runTest {
        server.respond(
            200,
            """{"data":{"content":[{"notificationId":1,"title":"Approved","read":false},{"notificationId":2,"read":true}]}}""",
        )

        val result = repository.list() as Outcome.Success

        assertEquals(listOf(1L, 2L), result.value.map { it.notificationId })
        assertEquals(listOf(false, true), result.value.map { it.read })
    }

    @Test
    fun `a page with no content field reads as empty, although Gson leaves it null`() = runTest {
        server.respond(200, """{"data":{"page":0}}""")

        assertEquals(Outcome.Success(emptyList<Any>()), repository.list())
    }

    @Test
    fun `a missing page reads as empty`() = runTest {
        server.respond(200, """{"message":"ok"}""")

        assertEquals(Outcome.Success(emptyList<Any>()), repository.list())
    }

    @Test
    fun `a failed list passes the failure through`() = runTest {
        server.respond(401, "")

        val result = repository.list() as Outcome.Failure

        assertEquals(401, result.code)
    }

    @Test
    fun `the unread count is read from its own endpoint`() = runTest {
        server.respond(200, """{"data":{"unread":7}}""")

        assertEquals(Outcome.Success(7L), repository.unreadCount())
        assertEquals("/api/customer/notifications/unread-count", server.takeRequest().path)
    }

    @Test
    fun `a missing count reads as zero`() = runTest {
        server.respond(200, """{"data":null}""")

        assertEquals(Outcome.Success(0L), repository.unreadCount())
    }

    @Test
    fun `marking one read puts to that notification`() = runTest {
        server.respond(200, """{"data":{"notificationId":9,"read":true}}""")

        val result = repository.markRead(9) as Outcome.Success

        val recorded = server.takeRequest()
        assertEquals("PUT", recorded.method)
        assertEquals("/api/customer/notifications/9/read", recorded.path)
        assertTrue(result.value.read)
    }

    @Test
    fun `marking one read without data in the answer is a failure`() = runTest {
        server.respond(200, """{"message":"Notification not found"}""")

        assertEquals(Outcome.Failure("Notification not found"), repository.markRead(9))
    }

    @Test
    fun `marking all read succeeds whatever the body says`() = runTest {
        server.respond(200, """{"data":{"updated":3}}""")

        assertEquals(Outcome.Success(Unit), repository.markAllRead())
        assertEquals("/api/customer/notifications/read", server.takeRequest().path)
    }

    @Test
    fun `marking all read reports a failure`() = runTest {
        server.respond(500, """{"message":"Try later"}""")

        assertEquals(Outcome.Failure("Try later", 500), repository.markAllRead())
    }
}

/**
 * The branch list feeds the sign-up form, before the customer has a token. It
 * is checked at the API level because that is where it can break: a wrong path
 * or a renamed field would leave the picker silently empty and block every new
 * registration.
 */
class BranchOptionsApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: com.example.test2.data.remote.ApiService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = server.apiService()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `it reads the public branch options endpoint`() = runTest {
        server.respond(
            200,
            """{"data":[{"branchId":1,"branchCode":"BR-1","branchName":"Jakarta Pusat","location":"Jakarta"},""" +
                """{"branchId":2,"branchName":"Bandung"}]}""",
        )

        val response = api.branchOptions()

        assertEquals("/api/branches/options", server.takeRequest().path)
        assertTrue(response.isSuccessful)

        val branches = response.body()?.data.orEmpty()
        assertEquals(listOf(1, 2), branches.map { it.branchId })
        assertEquals("Jakarta Pusat", branches[0].branchName)
        assertEquals("Jakarta", branches[0].location)
    }

    @Test
    fun `a branch without a name or location still parses`() = runTest {
        server.respond(200, """{"data":[{"branchId":9}]}""")

        val branches = api.branchOptions().body()?.data.orEmpty()

        assertEquals(1, branches.size)
        assertEquals(9, branches[0].branchId)
        assertEquals(null, branches[0].branchName)
    }

    @Test
    fun `no data field reads as no branches, not a crash`() = runTest {
        server.respond(200, """{"message":"ok"}""")

        assertEquals(emptyList<Any>(), api.branchOptions().body()?.data.orEmpty())
    }
}
