package com.example.test2.data.repository

import com.example.test2.core.Outcome
import com.example.test2.data.remote.ApiService
import com.example.test2.data.dto.NotificationDto

class NotificationRepository(private val api: ApiService) {

    suspend fun list(): Outcome<List<NotificationDto>> =
        when (val result = apiCall { api.myNotifications() }) {
            is Outcome.Failure -> result
            is Outcome.Success -> Outcome.Success(result.value.data?.content.orEmpty())
        }

    suspend fun unreadCount(): Outcome<Long> =
        when (val result = apiCall { api.unreadNotificationCount() }) {
            is Outcome.Failure -> result
            is Outcome.Success -> Outcome.Success(result.value.data?.unread ?: 0L)
        }

    suspend fun markRead(notificationId: Long): Outcome<NotificationDto> =
        apiCall { api.markNotificationRead(notificationId) }.unwrapEnvelope()

    suspend fun markAllRead(): Outcome<Unit> =
        when (val result = apiCall { api.markAllNotificationsRead() }) {
            is Outcome.Failure -> result
            is Outcome.Success -> Outcome.Success(Unit)
        }
}
