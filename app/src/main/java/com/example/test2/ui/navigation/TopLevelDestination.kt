package com.example.test2.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val destination: AppRoute,
    val label: String,
    val icon: ImageVector,
) {
    Home(AppRoute.Home, "Home", Icons.Filled.Home),
    Simulate(AppRoute.Simulate, "Simulate", Icons.Filled.Calculate),
    Loans(AppRoute.Loans, "Loans", Icons.Filled.List),
    History(AppRoute.History, "History", Icons.Filled.Refresh),
    Profile(AppRoute.Profile, "Profile", Icons.Filled.Person);

    val route: String get() = destination.route

    companion object {

        fun fromRoute(route: String?): TopLevelDestination? =
            entries.firstOrNull { it.route == route }

        fun isTopLevel(route: String?): Boolean = fromRoute(route) != null
    }
}
