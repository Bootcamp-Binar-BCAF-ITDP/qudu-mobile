package com.example.test2.core

/**
 * Mirrors com.delvin.loan.common.DocumentType on the backend.
 *
 * The split is the whole point of the profile flow: [PROFILE] papers describe
 * the person and are uploaded once, right after registering, then reused by
 * every application. [APPLICATION] papers describe one loan and one payout
 * account, so they are captured per application.
 *
 * The backend enforces the same split - posting a KTP to the application
 * endpoint is a 400, and creating an application without the three profile
 * papers on file is a 409.
 */
object DocumentTypes {

    const val KTP = "KTP"
    const val KK = "KK"
    const val SELFIE = "SELFIE"
    const val SLIP_GAJI = "SLIP_GAJI"
    const val BANK_ACCOUNT = "BANK_ACCOUNT"

    /** Required on the profile before any application may be filed. */
    val PROFILE = listOf(KTP, KK, SELFIE)

    /** Financial evidence. Re-uploaded for each submission, but stored on the customer. */
    val APPLICATION = listOf(SLIP_GAJI, BANK_ACCOUNT)

    /**
     * What the backend demands before a loan application or a limit increase may
     * be filed. Mirrors com.delvin.loan.common.DocumentType.REQUIRED_FOR_SUBMISSION.
     */
    val REQUIRED_FOR_SUBMISSION = PROFILE + APPLICATION

    fun label(type: String): String = when (type) {
        KTP -> "ID Card (KTP)"
        KK -> "Family Card (KK)"
        SELFIE -> "Selfie Photo"
        SLIP_GAJI -> "Payslip"
        BANK_ACCOUNT -> "Bank Passbook"
        else -> type
    }

    fun hint(type: String): String = when (type) {
        KTP -> "A valid national ID card"
        KK -> "The family card page"
        SELFIE -> "A selfie holding your ID card"
        SLIP_GAJI -> "Your latest monthly payslip"
        BANK_ACCOUNT -> "The front page of your passbook"
        else -> "Tap to upload"
    }

    /** The selfie is taken with the camera; everything else is picked from storage. */
    fun isCameraCapture(type: String): Boolean = type == SELFIE
}
