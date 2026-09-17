package com.example.test2.core

object DocumentTypes {

    const val KTP = "KTP"
    const val KK = "KK"
    const val SELFIE = "SELFIE"
    const val SLIP_GAJI = "SLIP_GAJI"
    const val BANK_ACCOUNT = "BANK_ACCOUNT"

    val PROFILE = listOf(KTP, KK, SELFIE)

    val APPLICATION = listOf(SLIP_GAJI, BANK_ACCOUNT)

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

    fun isCameraCapture(type: String): Boolean = type == SELFIE || type == KTP
}
