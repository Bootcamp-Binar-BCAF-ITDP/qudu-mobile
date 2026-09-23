package com.example.test2.di

import android.content.ContentResolver
import android.content.Context
import com.example.test2.core.AppEvents
import com.example.test2.data.local.SessionStore
import com.example.test2.data.local.room.CustomerProfileDao
import com.example.test2.data.local.room.LoanApplicationDao
import com.example.test2.data.local.room.PlafondRequestDao
import com.example.test2.data.local.room.PlafondTierDao
import com.example.test2.data.remote.ApiClient
import com.example.test2.data.remote.ApiService
import com.example.test2.data.repository.AuthRepository
import com.example.test2.data.repository.LoanRepository
import com.example.test2.data.repository.NotificationRepository
import com.example.test2.data.repository.PlafondCatalogRepository
import com.example.test2.data.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSessionStore(@ApplicationContext context: Context): SessionStore =
        SessionStore(context)

    @Provides
    @Singleton
    fun provideAppEvents(): AppEvents = AppEvents()

    @Provides
    @Singleton
    fun provideApiService(
        sessionStore: SessionStore,
        @ApplicationContext context: Context,
    ): ApiService = ApiClient.create(sessionStore, context)

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver =
        context.contentResolver

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: ApiService,
        sessionStore: SessionStore,
        applicationDao: LoanApplicationDao,
        plafondRequestDao: PlafondRequestDao,
        profileDao: CustomerProfileDao,
    ): AuthRepository =
        AuthRepository(api, sessionStore, applicationDao, plafondRequestDao, profileDao)

    @Provides
    @Singleton
    fun provideLoanRepository(
        api: ApiService,
        contentResolver: ContentResolver,
        applicationDao: LoanApplicationDao,
        plafondRequestDao: PlafondRequestDao,
    ): LoanRepository =
        LoanRepository(api, contentResolver, applicationDao, plafondRequestDao)

    @Provides
    @Singleton
    fun provideProfileRepository(
        api: ApiService,
        contentResolver: ContentResolver,
        profileDao: CustomerProfileDao,
    ): ProfileRepository = ProfileRepository(api, contentResolver, profileDao)

    @Provides
    @Singleton
    fun provideNotificationRepository(api: ApiService): NotificationRepository =
        NotificationRepository(api)

    @Provides
    @Singleton
    fun providePlafondCatalogRepository(
        api: ApiService,
        tierDao: PlafondTierDao,
    ): PlafondCatalogRepository = PlafondCatalogRepository(api, tierDao)
}
