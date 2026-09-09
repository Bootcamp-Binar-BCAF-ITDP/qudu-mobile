package com.example.test2

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.test2.di.AppContainer

class QuDuApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createLoanStatusChannel()
    }

    private fun createLoanStatusChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            getString(R.string.loan_status_channel_id),
            getString(R.string.loan_status_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.loan_status_channel_description)
        }

        getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }
}
