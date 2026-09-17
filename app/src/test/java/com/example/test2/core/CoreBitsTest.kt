package com.example.test2.core

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.async
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OutcomeTest {

    @Test
    fun `a success hands back its value`() {
        assertEquals(42, Outcome.Success(42).successOrNull)
    }

    @Test
    fun `a failure hands back nothing`() {
        assertNull((Outcome.Failure("boom") as Outcome<Int>).successOrNull)
    }

    @Test
    fun `a failure carries no code unless the server gave one`() {
        assertNull(Outcome.Failure("offline").code)
        assertEquals(422, Outcome.Failure("over", HTTP_OVER_PLAFOND).code)
    }

    @Test
    fun `the status codes the app reacts to are the http ones`() {
        assertEquals(401, HTTP_UNAUTHORIZED)
        assertEquals(409, HTTP_ALREADY_REGISTERED)
        assertEquals(422, HTTP_OVER_PLAFOND)
    }
}

class DocumentTypesTest {

    @Test
    fun `submission needs the three profile documents and the two application ones`() {
        assertEquals(
            listOf("KTP", "KK", "SELFIE", "SLIP_GAJI", "BANK_ACCOUNT"),
            DocumentTypes.REQUIRED_FOR_SUBMISSION,
        )
    }

    @Test
    fun `no document is both a profile and an application document`() {
        assertTrue(DocumentTypes.PROFILE.intersect(DocumentTypes.APPLICATION.toSet()).isEmpty())
    }

    @Test
    fun `every known type has an English label and a hint`() {
        DocumentTypes.REQUIRED_FOR_SUBMISSION.forEach { type ->
            assertFalse(type, DocumentTypes.label(type) == type)
            assertFalse(type, DocumentTypes.hint(type) == "Tap to upload")
        }
    }

    @Test
    fun `an unknown type shows its raw name and a generic hint`() {
        assertEquals("NPWP", DocumentTypes.label("NPWP"))
        assertEquals("Tap to upload", DocumentTypes.hint("NPWP"))
    }

    @Test
    fun `the selfie and the KTP must come from the camera, so neither can be a stock photo`() {
        assertTrue(DocumentTypes.isCameraCapture("SELFIE"))
        assertTrue(DocumentTypes.isCameraCapture("KTP"))
    }

    @Test
    fun `every other document may come from a file`() {
        DocumentTypes.REQUIRED_FOR_SUBMISSION
            .filter { it != "SELFIE" && it != "KTP" }
            .forEach { assertFalse(it, DocumentTypes.isCameraCapture(it)) }
    }

    @Test
    fun `an unknown document type may come from a file`() {
        assertFalse(DocumentTypes.isCameraCapture("NPWP"))
    }
}

class UploadRulesTest {

    @Test
    fun `the upload ceiling is five megabytes`() {
        assertEquals(5L * 1024 * 1024, UploadRules.MAX_FILE_BYTES)
    }

    @Test
    fun `images are squeezed well under that ceiling`() {
        assertTrue(UploadRules.TARGET_IMAGE_BYTES < UploadRules.MAX_FILE_BYTES)
    }
}

class AppEventsTest {

    @Test
    fun `a push type maps onto its refresh reason`() {
        assertEquals(RefreshReason.PLAFOND_DECISION, refreshReasonOf("PLAFOND_DECISION"))
        assertEquals(RefreshReason.LOAN_STATUS, refreshReasonOf("LOAN_STATUS"))
    }

    @Test
    fun `an unknown or missing push type still refreshes, as unknown`() {
        assertEquals(RefreshReason.UNKNOWN, refreshReasonOf("SOMETHING_NEW"))
        assertEquals(RefreshReason.UNKNOWN, refreshReasonOf(null))
        assertEquals(RefreshReason.UNKNOWN, refreshReasonOf("loan_status"))
    }

    @Test
    fun `a request reaches a listening screen`() = runTest(UnconfinedTestDispatcher()) {
        val events = AppEvents()
        val received = async { events.refreshRequests.first() }

        assertTrue(events.requestRefresh(RefreshReason.LOAN_STATUS))
        assertEquals(RefreshReason.LOAN_STATUS, received.await())
    }

    @Test
    fun `a request with nobody listening is accepted and not replayed later`() = runTest(UnconfinedTestDispatcher()) {
        val events = AppEvents()

        assertTrue(events.requestRefresh(RefreshReason.UNKNOWN))

        val late = async { events.refreshRequests.first() }
        events.requestRefresh(RefreshReason.PLAFOND_DECISION)

        assertEquals(RefreshReason.PLAFOND_DECISION, late.await())
    }

    @Test
    fun `a burst never blocks the push service, it drops the oldest instead`() {
        val events = AppEvents()

        repeat(50) { assertTrue(events.requestRefresh(RefreshReason.UNKNOWN)) }
    }
}
