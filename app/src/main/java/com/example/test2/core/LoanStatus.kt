package com.example.test2.core

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object LoanStatus {
    const val CHECKING = "CHECKING"
    const val REJECTED_BY_MARKETING = "REJECTED_BY_MARKETING"
    const val PENDING_BRANCH_MANAGER = "PENDING_BRANCH_MANAGER"
    const val REJECTED_BY_BRANCH_MANAGER = "REJECTED_BY_BRANCH_MANAGER"
    const val PENDING_BACK_OFFICE = "PENDING_BACK_OFFICE"
    const val VERIFIED = "VERIFIED"
    const val DISBURSED = "DISBURSED"
    const val REJECTED_BY_BACK_OFFICE = "REJECTED_BY_BACK_OFFICE"

    fun isRejected(status: String?): Boolean = status?.startsWith("REJECTED_") == true

    fun isClosed(status: String?): Boolean = isRejected(status) || status == DISBURSED

    fun label(status: String?): String = when (status) {
        CHECKING -> "Under marketing review"
        PENDING_BRANCH_MANAGER -> "Awaiting branch manager"
        PENDING_BACK_OFFICE -> "Back office verification"
        VERIFIED -> "Ready for disbursement"
        DISBURSED -> "Funds disbursed"
        REJECTED_BY_MARKETING -> "Rejected - marketing"
        REJECTED_BY_BRANCH_MANAGER -> "Rejected - branch manager"
        REJECTED_BY_BACK_OFFICE -> "Rejected - back office"
        null -> "No application yet"
        else -> status
    }

    fun trackerStep(status: String?): Int = when (status) {
        CHECKING -> 1
        PENDING_BRANCH_MANAGER, PENDING_BACK_OFFICE, VERIFIED -> 1
        DISBURSED -> 2
        else -> if (isRejected(status)) 2 else 0
    }
}

private val rupiahFormat: NumberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
    maximumFractionDigits = 0
}

fun BigDecimal?.asRupiah(): String = this?.let { rupiahFormat.format(it) } ?: "-"
