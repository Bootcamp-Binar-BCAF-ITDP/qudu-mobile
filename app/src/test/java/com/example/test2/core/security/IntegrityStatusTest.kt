package com.example.test2.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The gate itself. The detectors need a real device, so what is guarded here
 * is the rule that decides whether the app opens at all.
 */
class IntegrityStatusTest {

    @Test
    fun `a clean device is not blocked`() {
        assertFalse(IntegrityStatus().isCompromised)
    }

    @Test
    fun `root alone blocks the app`() {
        assertTrue(IntegrityStatus(rooted = true).isCompromised)
    }

    @Test
    fun `developer options alone block the app`() {
        assertTrue(IntegrityStatus(developerOptions = true).isCompromised)
    }

    @Test
    fun `usb debugging alone blocks the app`() {
        assertTrue(IntegrityStatus(usbDebugging = true).isCompromised)
    }

    @Test
    fun `every reason is kept, so the dialog can name all of them`() {
        val status = IntegrityStatus(rooted = true, developerOptions = true, usbDebugging = true)

        assertTrue(status.rooted)
        assertTrue(status.developerOptions)
        assertTrue(status.usbDebugging)
    }
}
