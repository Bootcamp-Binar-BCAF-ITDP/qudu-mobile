package com.example.test2.di

import android.content.Context
import androidx.room.Room
import com.example.test2.core.database.QuduDatabase
import com.example.test2.data.local.room.CustomerProfileDao
import com.example.test2.data.local.room.LoanApplicationDao
import com.example.test2.data.local.room.PlafondRequestDao
import com.example.test2.data.local.room.PlafondTierDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): QuduDatabase =
        Room.databaseBuilder(context, QuduDatabase::class.java, QuduDatabase.NAME)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun provideLoanApplicationDao(database: QuduDatabase): LoanApplicationDao =
        database.loanApplicationDao()

    @Provides
    @Singleton
    fun providePlafondRequestDao(database: QuduDatabase): PlafondRequestDao =
        database.plafondRequestDao()

    @Provides
    @Singleton
    fun provideCustomerProfileDao(database: QuduDatabase): CustomerProfileDao =
        database.customerProfileDao()

    @Provides
    @Singleton
    fun providePlafondTierDao(database: QuduDatabase): PlafondTierDao =
        database.plafondTierDao()
}
