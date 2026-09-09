package com.example.test2.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Why the UI is being asked to reload. Carried for logging and future routing. */
enum class RefreshReason {
    /** A branch manager decided a limit increase. */
    PLAFOND_DECISION,

    /** A loan application moved stage or was disbursed. */
    LOAN_STATUS,

    /** A push arrived whose type this build does not recognise. */
    UNKNOWN,
}

/**
 * A one-way channel from the FCM service to whatever UI happens to be on screen.
 *
 * `QuDuMessagingService` is a Service, not the Activity - it has no reference to
 * any ViewModel and cannot get one. It publishes here instead, and the app shell
 * collects, so a decision made by a branch manager reaches the customer's screen
 * without them navigating anywhere.
 *
 * **replay = 0 on purpose.** An event fired while nothing is collecting is
 * dropped rather than replayed into the next cold start, where it would trigger
 * a redundant reload of data that launch already fetched. The gap that leaves -
 * a push arriving while the app is backgrounded - is covered by the ON_RESUME
 * refresh in `QuickDuitApp`, which is the same gap FCM itself leaves: a message
 * carrying a `notification` payload is handled by the system tray and never
 * reaches `onMessageReceived` unless the app is in the foreground.
 */
class AppEvents {

    private val _refreshRequests = MutableSharedFlow<RefreshReason>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val refreshRequests: SharedFlow<RefreshReason> = _refreshRequests.asSharedFlow()

    /**
     * Non-suspending, so the FCM callback can post from its own thread without
     * needing a coroutine. Returns false when the buffer is full, which the
     * caller has nothing useful to do about - a dropped refresh request is not
     * a dropped notification.
     */
    fun requestRefresh(reason: RefreshReason): Boolean = _refreshRequests.tryEmit(reason)
}

/** Maps the backend's `data.type` (com.delvin.loan.common.NotificationType). */
fun refreshReasonOf(type: String?): RefreshReason = when (type) {
    "PLAFOND_DECISION" -> RefreshReason.PLAFOND_DECISION
    "LOAN_STATUS" -> RefreshReason.LOAN_STATUS
    else -> RefreshReason.UNKNOWN
}
