package com.example.test2.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController


fun NavHostController.isAt(route: String): Boolean = currentDestination?.route == route

fun NavHostController.navigateToTab(destination: TopLevelDestination): Boolean {
    if (isAt(destination.route)) return false

    navigate(destination.route) {
        launchSingleTop = true
        popUpTo(graph.findStartDestination().id)
    }
    return true
}

fun NavHostController.pushRoute(route: AppRoute): Boolean {
    if (isAt(route.route)) return false

    navigate(route.route) { launchSingleTop = true }
    return true
}

fun NavHostController.navigateTo(route: AppRoute): Boolean {
    val tab = TopLevelDestination.fromRoute(route.route)
    return if (tab != null) navigateToTab(tab) else pushRoute(route)
}

fun NavHostController.navigateHomeClearingAuth() {
    navigate(AppRoute.Home.route) {
        popUpTo(AppRoute.Home.route) { inclusive = true }
        launchSingleTop = true
    }
}
