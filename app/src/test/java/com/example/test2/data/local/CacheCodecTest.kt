package com.example.test2.data.local

import com.example.test2.data.dto.LoanApplicationDto
import com.example.test2.data.dto.PlafondRequestDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class CacheCodecTest {

    private val codec = CacheCodec()

    private fun application(id: String, status: String = "DISBURSED") = LoanApplicationDto(
        applicationId = id,
        status = status,
        requestedAmount = BigDecimal("2000000"),
        tenor = 12,
        purpose = "Education",
    )

    private fun request(id: String) = PlafondRequestDto(
        requestId = id,
        status = "APPROVED",
        requestedAmount = BigDecimal("11000000"),
    )


    @Test
    fun `applications survive a round trip with their fields intact`() {
        val json = codec.encode(
            items = listOf(application("LA-1"), application("LA-2", status = "CHECKING")),
            elementType = LoanApplicationDto::class.java,
            fetchedAt = 1_700_000_000_000L,
        )

        val decoded: CachedList<LoanApplicationDto> =
            codec.decode(json, LoanApplicationDto::class.java)

        assertEquals(2, decoded.items.size)
        assertEquals("LA-1", decoded.items[0].applicationId)
        assertEquals("CHECKING", decoded.items[1].status)
        assertEquals(BigDecimal("2000000"), decoded.items[0].requestedAmount)
        assertEquals(12, decoded.items[0].tenor)
    }

    @Test
    fun `plafond requests survive a round trip`() {
        val json = codec.encode(
            items = listOf(request("PR-1")),
            elementType = PlafondRequestDto::class.java,
            fetchedAt = 1_700_000_000_000L,
        )

        val decoded: CachedList<PlafondRequestDto> =
            codec.decode(json, PlafondRequestDto::class.java)

        assertEquals(1, decoded.items.size)
        assertEquals("PR-1", decoded.items[0].requestId)
        assertEquals("APPROVED", decoded.items[0].status)
    }

    @Test
    fun `the fetch timestamp comes back`() {
        val stamp = 1_700_000_000_000L

        val decoded: CachedList<LoanApplicationDto> = codec.decode(
            codec.encode(listOf(application("LA-1")), LoanApplicationDto::class.java, stamp),
            LoanApplicationDto::class.java,
        )

        assertEquals(stamp, decoded.fetchedAt)
    }

    @Test
    fun `every entry carries the timestamp, not just the list`() {
        val json = codec.encode(
            items = listOf(application("LA-1"), application("LA-2")),
            elementType = LoanApplicationDto::class.java,
            fetchedAt = 1_700_000_000_000L,
        )

        assertEquals(
            "each entry should carry its own fetchedAt",
            2,
            Regex("\"fetchedAt\"").findAll(json).count(),
        )
    }


    @Test
    fun `nothing ever cached reads as an unknown age`() {
        val decoded: CachedList<LoanApplicationDto> =
            codec.decode(null, LoanApplicationDto::class.java)

        assertTrue(decoded.isEmpty)
        assertNull("never fetched, so no age", decoded.fetchedAt)
        assertTrue(!decoded.everCached)
    }

    @Test
    fun `an empty list was still fetched, and says so`() {
        val json = codec.encode(
            items = emptyList<LoanApplicationDto>(),
            elementType = LoanApplicationDto::class.java,
            fetchedAt = 1_700_000_000_000L,
        )

        val decoded: CachedList<LoanApplicationDto> =
            codec.decode(json, LoanApplicationDto::class.java)

        assertTrue(decoded.isEmpty)
        assertNull(decoded.fetchedAt)
    }


    @Test
    fun `malformed json decodes to an empty cache`() {
        val decoded: CachedList<LoanApplicationDto> =
            codec.decode("{not json at all", LoanApplicationDto::class.java)

        assertTrue(decoded.isEmpty)
        assertNull(decoded.fetchedAt)
    }

    @Test
    fun `json of the wrong shape decodes to an empty cache`() {
        val decoded: CachedList<LoanApplicationDto> =
            codec.decode("""{"unexpected":"object"}""", LoanApplicationDto::class.java)

        assertTrue(decoded.isEmpty)
    }

    @Test
    fun `a blank string decodes to an empty cache`() {
        val decoded: CachedList<LoanApplicationDto> =
            codec.decode("   ", LoanApplicationDto::class.java)

        assertTrue(decoded.isEmpty)
    }


    @Test
    fun `encoding a shorter list drops what is no longer there`() {
        val first = codec.encode(
            listOf(application("LA-1"), application("LA-2")),
            LoanApplicationDto::class.java,
            1L,
        )
        val second = codec.encode(
            listOf(application("LA-2")),
            LoanApplicationDto::class.java,
            2L,
        )

        val decodedFirst: CachedList<LoanApplicationDto> =
            codec.decode(first, LoanApplicationDto::class.java)
        val decodedSecond: CachedList<LoanApplicationDto> =
            codec.decode(second, LoanApplicationDto::class.java)

        assertEquals(2, decodedFirst.items.size)
        assertEquals(1, decodedSecond.items.size)
        assertEquals("LA-2", decodedSecond.items[0].applicationId)
    }
}
