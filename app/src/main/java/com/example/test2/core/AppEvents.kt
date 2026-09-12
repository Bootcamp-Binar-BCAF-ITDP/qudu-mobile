package com.example.test2.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class RefreshReason {
    PLAFOND_DECISION,

    LOAN_STATUS,

    UNKNOWN,
}

class AppEvents {

    private val _refreshRequests = MutableSharedFlow<RefreshReason>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val refreshRequests: SharedFlow<RefreshReason> = _refreshRequests.asSharedFlow()

    fun requestRefresh(reason: RefreshReason): Boolean = _refreshRequests.tryEmit(reason)
}

fun refreshReasonOf(type: String?): RefreshReason = when (type) {
    "PLAFOND_DECISION" -> RefreshReason.PLAFOND_DECISION
    "LOAN_STATUS" -> RefreshReason.LOAN_STATUS
    else -> RefreshReason.UNKNOWN
}
