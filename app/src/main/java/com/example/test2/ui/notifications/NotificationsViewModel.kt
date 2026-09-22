package com.example.test2.ui.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test2.core.Outcome
import com.example.test2.data.dto.NotificationDto
import com.example.test2.data.repository.NotificationRepository
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
) : ViewModel() {

    var notifications by mutableStateOf<List<NotificationDto>>(emptyList())
        private set
    var unread by mutableStateOf(0L)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun refresh(userInitiated: Boolean = false) {
        if (isLoading) return

        isLoading = true
        if (userInitiated) isRefreshing = true

        viewModelScope.launch {
            try {
                when (val result = repository.list()) {
                    is Outcome.Success -> {
                        notifications = result.value
                        unread = result.value.count { !it.read }.toLong()
                        error = null
                    }
                    is Outcome.Failure -> error = result.message
                }
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    fun refreshUnreadCount() {
        viewModelScope.launch {
            when (val result = repository.unreadCount()) {
                is Outcome.Success -> unread = result.value
                is Outcome.Failure -> Unit
            }
        }
    }

    fun markRead(notificationId: Long) {
        viewModelScope.launch {
            when (repository.markRead(notificationId)) {
                is Outcome.Success -> {
                    notifications = notifications.map {
                        if (it.notificationId == notificationId) it.copy(read = true) else it
                    }
                    unread = notifications.count { !it.read }.toLong()
                }
                is Outcome.Failure -> Unit
            }
        }
    }

    fun markAllRead() {
        if (notifications.none { !it.read }) return

        viewModelScope.launch {
            when (repository.markAllRead()) {
                is Outcome.Success -> {
                    notifications = notifications.map { it.copy(read = true) }
                    unread = 0
                }
                is Outcome.Failure -> Unit
            }
        }
    }
}
