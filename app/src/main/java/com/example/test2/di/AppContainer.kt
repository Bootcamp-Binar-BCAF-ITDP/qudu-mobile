package com.example.test2.di

import android.content.Context
import com.example.test2.core.AppEvents
import com.example.test2.data.local.LoanCache
import com.example.test2.data.local.ProfileCache
import com.example.test2.data.local.SessionStore
import com.example.test2.data.remote.ApiClient
import com.example.test2.data.remote.ApiService
import com.example.test2.data.repository.AuthRepository
import com.example.test2.data.repository.LoanRepository
import com.example.test2.data.repository.NotificationRepository
import com.example.test2.data.repository.PlafondCatalogRepository
import com.example.test2.data.repository.ProfileRepository

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val sessionStore: SessionStore by lazy { SessionStore(appContext) }

    val appEvents = AppEvents()

    val api: ApiService by lazy { ApiClient.create(sessionStore, appContext) }

    val authRepository: AuthRepository by lazy {
        AuthRepository(api, sessionStore, loanCache, profileCache)
    }

    val loanCache: LoanCache by lazy { LoanCache(appContext) }

    val loanRepository: LoanRepository by lazy {
        LoanRepository(api, appContext.contentResolver, loanCache)
    }

    val profileCache: ProfileCache by lazy { ProfileCache(appContext) }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(api, appContext.contentResolver, profileCache)
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepository(api)
    }

    val plafondCatalogRepository: PlafondCatalogRepository by lazy {
        PlafondCatalogRepository(api)
    }
}
