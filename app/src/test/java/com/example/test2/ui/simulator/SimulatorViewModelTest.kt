package com.example.test2.ui.simulator

import com.example.test2.core.PlafondTiers
import com.example.test2.core.monthlyInstalmentFor
import com.example.test2.data.repository.PlafondCatalogRepository
import com.example.test2.testing.apiService
import com.example.test2.testing.respond
import com.example.test2.testing.waitUntil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class SimulatorViewModelTest {

    private lateinit var server: MockWebServer
    private lateinit var vm: SimulatorViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        server = MockWebServer().apply { start() }
        vm = SimulatorViewModel(PlafondCatalogRepository(server.apiService()))
    }

    @After
    fun tearDown() {
        server.shutdown()
        Dispatchers.resetMain()
    }

    private fun loadAndWait(userInitiated: Boolean = false) {
        vm.load(userInitiated)
        waitUntil { !vm.isLoading }
    }

    @Test
    fun `it opens on the built-in ladder, not yet live`() {
        assertEquals(PlafondTiers.FALLBACK, vm.tiers)
        assertFalse(vm.live)
    }

    @Test
    fun `it opens on a valid Silver example`() {
        assertEquals(25_000_000L, vm.amount)
        assertEquals(24, vm.tenor)
        assertEquals("Silver", vm.tier?.name)
        assertTrue(vm.inputsValid)
    }

    @Test
    fun `the amount range spans the whole ladder`() {
        assertEquals(1_000_000L, vm.minAmount)
        assertEquals(1_200_000_000L, vm.maxAmount)
    }

    @Test
    fun `the tenor range follows the tier of the amount`() {
        vm.updateAmount(60_000_000)

        assertEquals("Gold", vm.tier?.name)
        assertEquals(6, vm.minTenor)
        assertEquals(36, vm.maxTenor)
    }

    @Test
    fun `moving into a shorter tier pulls the tenor down with it`() {
        vm.updateAmount(5_000_000)

        assertEquals("Bronze", vm.tier?.name)
        assertEquals(12, vm.tenor)
    }

    @Test
    fun `moving into a tier with a longer minimum pushes the tenor up`() {
        vm.updateTenor(2)
        vm.updateAmount(300_000_000)

        assertEquals(12, vm.tenor)
    }

    @Test
    fun `a typed tenor is kept as typed, so the field can show it out of range`() {
        vm.updateTenor(99)

        assertEquals(99, vm.tenor)
        assertFalse(vm.tenorInRange)
        assertFalse(vm.inputsValid)
    }

    @Test
    fun `a negative amount or tenor is floored at zero`() {
        vm.updateAmount(-10)
        vm.updateTenor(-3)

        assertEquals(0L, vm.amount)
        assertEquals(0, vm.tenor)
    }

    @Test
    fun `an amount below the ladder is out of range`() {
        vm.updateAmount(500_000)

        assertFalse(vm.amountInRange)
        assertFalse(vm.inputsValid)
    }

    @Test
    fun `an amount above the ladder is out of range`() {
        vm.updateAmount(2_000_000_000)

        assertFalse(vm.amountInRange)
    }

    @Test
    fun `the instalment uses the rate of the current tier`() {
        vm.updateAmount(25_000_000)
        vm.updateTenor(12)

        assertEquals(monthlyInstalmentFor(25_000_000, 12, 0.13), vm.monthlyInstalment)
    }

    @Test
    fun `total repayment is the instalment times the tenor`() {
        assertEquals(vm.monthlyInstalment * vm.tenor, vm.totalRepayment)
    }

    @Test
    fun `total cost is the interest plus the admin fee`() {
        assertEquals(vm.totalRepayment - vm.amount + 200_000, vm.totalCost)
        assertEquals(200_000L, vm.adminFee)
    }

    @Test
    fun `total cost never goes negative, even with a zero tenor`() {
        vm.updateTenor(0)

        assertEquals(0L, vm.monthlyInstalment)
        assertEquals(vm.adminFee, vm.totalCost)
    }

    @Test
    fun `a live ladder replaces the built-in one`() {
        server.respond(
            200,
            """{"data":[{"level":1,"description":"Starter","minimumAmount":1000000,"maxAmount":30000000,"minTenor":3,"maxTenor":18,"interestRate":0.2,"adminFee":0}]}""",
        )

        loadAndWait()

        assertTrue(vm.live)
        assertEquals(listOf("Starter"), vm.tiers.map { it.name })
    }

    @Test
    fun `a live ladder snaps the amount and tenor into its range`() {
        server.respond(
            200,
            """{"data":[{"level":1,"description":"Small","minimumAmount":1000000,"maxAmount":10000000,"minTenor":3,"maxTenor":6,"interestRate":0.2}]}""",
        )

        loadAndWait()

        assertEquals(10_000_000L, vm.amount)
        assertEquals(6, vm.tenor)
        assertTrue(vm.inputsValid)
    }

    @Test
    fun `a failed load keeps the built-in ladder and says it is not live`() {
        server.respond(500, """{"message":"down"}""")

        loadAndWait()

        assertFalse(vm.live)
        assertEquals(PlafondTiers.FALLBACK, vm.tiers)
    }

    @Test
    fun `a second load while one is running is ignored`() {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""{"data":[]}""")
                .setBodyDelay(300, TimeUnit.MILLISECONDS)
        )

        vm.load()
        vm.load()
        waitUntil { !vm.isLoading }

        assertEquals(1, server.requestCount)
    }

    @Test
    fun `a pull to refresh shows the spinner and clears it afterwards`() {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""{"data":[]}""")
                .setBodyDelay(200, TimeUnit.MILLISECONDS)
        )

        vm.load(userInitiated = true)
        assertTrue(vm.isRefreshing)

        waitUntil { !vm.isLoading }
        assertFalse(vm.isRefreshing)
    }

    @Test
    fun `a background load does not show the pull to refresh spinner`() {
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""{"data":[]}""")
                .setBodyDelay(200, TimeUnit.MILLISECONDS)
        )

        vm.load()

        assertTrue(vm.isLoading)
        assertFalse(vm.isRefreshing)
        waitUntil { !vm.isLoading }
    }
}
