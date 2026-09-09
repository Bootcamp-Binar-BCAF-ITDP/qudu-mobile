package com.example.test2.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.test2.QuDuApplication
import com.example.test2.ui.apply.ApplyViewModel
import com.example.test2.ui.auth.AuthViewModel
import com.example.test2.ui.dashboard.DashboardViewModel
import com.example.test2.ui.loans.ApplicationsViewModel
import com.example.test2.ui.notifications.NotificationsViewModel
import com.example.test2.ui.plafond.PlafondViewModel
import com.example.test2.ui.profile.ProfileViewModel
import com.example.test2.ui.simulator.SimulatorViewModel

class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {

        modelClass.isAssignableFrom(AuthViewModel::class.java) ->
            AuthViewModel(container.authRepository) as T

        modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
            DashboardViewModel(container.loanRepository, container.sessionStore) as T

        modelClass.isAssignableFrom(ApplyViewModel::class.java) ->
            ApplyViewModel(
                container.loanRepository,
                container.profileRepository,
                container.sessionStore,
            ) as T

        modelClass.isAssignableFrom(PlafondViewModel::class.java) ->
            PlafondViewModel(container.loanRepository) as T

        modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
            ProfileViewModel(container.profileRepository, container.authRepository) as T

        modelClass.isAssignableFrom(ApplicationsViewModel::class.java) ->
            ApplicationsViewModel(container.loanRepository, container.sessionStore) as T

        modelClass.isAssignableFrom(SimulatorViewModel::class.java) ->
            SimulatorViewModel(container.plafondCatalogRepository) as T

        modelClass.isAssignableFrom(NotificationsViewModel::class.java) ->
            NotificationsViewModel(container.notificationRepository) as T

        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

@Composable
fun rememberAppContainer(): AppContainer {
    val context = LocalContext.current
    return remember(context) {
        (context.applicationContext as QuDuApplication).container
    }
}

@Composable
fun rememberViewModelFactory(): ViewModelFactory {
    val container = rememberAppContainer()
    return remember(container) { ViewModelFactory(container) }
}
