package com.example.test2.messaging

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.test2.MainActivity
import com.example.test2.QuDuApplication
import com.example.test2.R
import com.example.test2.core.refreshReasonOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class QuDuMessagingService : FirebaseMessagingService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        val container = (application as? QuDuApplication)?.container ?: return

        scope.launch {
            if (container.sessionStore.sessionOnce() != null) {
                container.authRepository.syncDeviceToken()
            }
        }
    }

    /**
     * Only called while the app is in the foreground.
     *
     * The backend sends a `notification` payload alongside its data
     * (`PushNotificationService.sendToCustomer`), and Android hands those
     * straight to the system tray when the app is backgrounded or dead - this
     * method never runs then. That is why the in-app refresh below is paired
     * with an ON_RESUME refresh in `QuickDuitApp`: between them they cover both
     * states. Adding one without the other looks like it works right up until
     * someone locks their phone.
     */
    override fun onMessageReceived(message: RemoteMessage) {

        val title = message.notification?.title
            ?: message.data["title"]
            ?: getString(R.string.app_name)

        val body = message.notification?.body
            ?: message.data["body"]
            ?: return

        val reference = message.data["applicationId"] ?: message.data["requestId"]

        showNotification(title, body, reference)

        // A decision landed, so whatever is on screen is now out of date - most
        // visibly the plafond limit. Published before any check on whether the
        // UI is listening: an unheard event costs nothing.
        (application as? QuDuApplication)
            ?.container
            ?.appEvents
            ?.requestRefresh(refreshReasonOf(message.data["type"]))
    }

    private fun showNotification(title: String, body: String, reference: String?) {

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            reference?.let { putExtra(EXTRA_APPLICATION_ID, it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(
            this,
            getString(R.string.loan_status_channel_id),
        )
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        getSystemService(Context.NOTIFICATION_SERVICE)
            ?.let { it as NotificationManager }
            ?.notify(reference?.hashCode() ?: 0, notification)
    }

    companion object {
        const val EXTRA_APPLICATION_ID = "applicationId"
    }
}
