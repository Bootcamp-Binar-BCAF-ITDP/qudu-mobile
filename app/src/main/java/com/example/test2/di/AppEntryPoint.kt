package com.example.test2.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.test2.core.AppEvents
import com.example.test2.data.local.SessionStore
import com.example.test2.data.repository.AuthRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppEntryPoint {

    fun sessionStore(): SessionStore

    fun authRepository(): AuthRepository

    fun appEvents(): AppEvents
}

@Composable
fun rememberAppEntryPoint(): AppEntryPoint {
    val context = LocalContext.current
    return remember(context) {
        EntryPointAccessors.fromApplication(context.applicationContext, AppEntryPoint::class.java)
    }
}
