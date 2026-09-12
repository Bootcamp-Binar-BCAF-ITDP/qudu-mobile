package com.example.test2.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.test2.ui.apply.ApplyLoanFlow
import com.example.test2.ui.apply.ApplyLoanState
import com.example.test2.ui.apply.ApplyViewModel
import com.example.test2.ui.auth.AuthViewModel
import com.example.test2.ui.auth.ForgotPasswordScreen
import com.example.test2.ui.auth.LoginScreen
import com.example.test2.ui.auth.RegisterScreen
import com.example.test2.ui.dashboard.DashboardScreen
import com.example.test2.ui.dashboard.DashboardViewModel
import com.example.test2.ui.history.HistoryScreen
import com.example.test2.ui.loans.ApplicationsViewModel
import com.example.test2.ui.loans.LoansScreen
import com.example.test2.ui.notifications.NotificationsScreen
import com.example.test2.ui.notifications.NotificationsViewModel
import com.example.test2.ui.plafond.PlafondUpgradeScreen
import com.example.test2.ui.profile.CompleteProfileScreen
import com.example.test2.ui.profile.ProfileScreen
import com.example.test2.ui.profile.ProfileViewModel
import com.example.test2.ui.simulator.SimulateScreen
import com.example.test2.ui.simulator.SimulatorViewModel

class AppNavDependencies(
    val dashboardViewModel: DashboardViewModel,
    val applyViewModel: ApplyViewModel,
    val profileViewModel: ProfileViewModel,
    val applicationsViewModel: ApplicationsViewModel,
    val notificationsViewModel: NotificationsViewModel,
    val simulatorViewModel: SimulatorViewModel,
    val authViewModel: AuthViewModel,
    val applyState: ApplyLoanState,
)

@Composable
fun AppNavHost(
    navController: NavHostController,
    signedIn: Boolean,
    activeTab: TopLevelDestination,
    dependencies: AppNavDependencies,
    onNavigate: (AppRoute) -> Unit,
    onNavigateWithAuth: (AppRoute) -> Unit,
    onRefreshAll: () -> Unit,
    onPullRefreshHome: () -> Unit,
    onMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.route,
        modifier = modifier.fillMaxSize(),
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {
        tabDestinations(
            signedIn = signedIn,
            dependencies = dependencies,
            onNavigate = onNavigate,
            onNavigateWithAuth = onNavigateWithAuth,
            onPullRefreshHome = onPullRefreshHome,
            onMessage = onMessage,
        )

        pushedDestinations(
            navController = navController,
            activeTab = activeTab,
            dependencies = dependencies,
            onNavigate = onNavigate,
            onRefreshAll = onRefreshAll,
            onMessage = onMessage,
        )
    }
}

private fun NavGraphBuilder.tabDestinations(
    signedIn: Boolean,
    dependencies: AppNavDependencies,
    onNavigate: (AppRoute) -> Unit,
    onNavigateWithAuth: (AppRoute) -> Unit,
    onPullRefreshHome: () -> Unit,
    onMessage: (String) -> Unit,
) = with(dependencies) {

    composable(AppRoute.Home.route) {
        DashboardScreen(
            signedIn = signedIn,
            userName = dashboardViewModel.customerName.ifBlank { "Customer" },
            plafond = dashboardViewModel.plafond,
            latestApplication = dashboardViewModel.latestApplication,
            isLoading = dashboardViewModel.isLoading,
            errorMessage = dashboardViewModel.error,
            profileComplete = profileViewModel.profileComplete != false,
            missingDocuments = profileViewModel.missingDocuments,
            simulatorViewModel = simulatorViewModel,
            onApplyLoan = {
                if (signedIn) applyViewModel.clearMessages()
                onNavigateWithAuth(AppRoute.ApplyLoan)
            },
            onCompleteProfile = { onNavigate(AppRoute.CompleteProfile) },
            onSeeLoans = { onNavigate(AppRoute.Loans) },
            onBills = {
                onMessage("Features coming soon")
            },
            onRequestUpgrade = { onNavigateWithAuth(AppRoute.PlafondUpgrade) },
            onLogin = { onNavigate(AppRoute.Login) },
            onRegister = { onNavigate(AppRoute.Register) },
            isRefreshing = dashboardViewModel.isRefreshing,
            onRefresh = onPullRefreshHome,
        )
    }

    composable(AppRoute.Simulate.route) {
        SimulateScreen(
            viewModel = simulatorViewModel,
            signedIn = signedIn,
            onApplyLoan = {
                if (signedIn) applyViewModel.clearMessages()
                onNavigateWithAuth(AppRoute.ApplyLoan)
            },
        )
    }

    composable(AppRoute.Loans.route) {
        LoansScreen(
            viewModel = applicationsViewModel,
            signedIn = signedIn,
            onApplyLoan = {
                if (signedIn) applyViewModel.clearMessages()
                onNavigateWithAuth(AppRoute.ApplyLoan)
            },
            onRequestUpgrade = { onNavigateWithAuth(AppRoute.PlafondUpgrade) },
        )
    }

    composable(AppRoute.History.route) {
        HistoryScreen(
            viewModel = applicationsViewModel,
            signedIn = signedIn,
            onLogin = { onNavigate(AppRoute.Login) },
            onRegister = { onNavigate(AppRoute.Register) },
        )
    }

    composable(AppRoute.Profile.route) {
        ProfileScreen(
            viewModel = profileViewModel,
            signedIn = signedIn,
            onLogout = { },
            onLogin = { onNavigate(AppRoute.Login) },
            onRegister = { onNavigate(AppRoute.Register) },
        )
    }
}

private fun NavGraphBuilder.pushedDestinations(
    navController: NavHostController,
    activeTab: TopLevelDestination,
    dependencies: AppNavDependencies,
    onNavigate: (AppRoute) -> Unit,
    onRefreshAll: () -> Unit,
    onMessage: (String) -> Unit,
) = with(dependencies) {

    composable(AppRoute.Login.route) {
        LoginScreen(
            viewModel = authViewModel,
            onLoggedIn = { onNavigate(AppRoute.Home) },
            onGoToRegister = { onNavigate(AppRoute.Register) },
            onForgotPassword = { onNavigate(AppRoute.ForgotPassword) },
            onBack = { navController.popBackStack() },
        )
    }

    composable(AppRoute.Register.route) {
        RegisterScreen(
            viewModel = authViewModel,
            onRegistered = { onNavigate(AppRoute.Login) },
            onBackToLogin = { onNavigate(AppRoute.Login) },
            onEmailAlreadyRegistered = { onNavigate(AppRoute.Login) },
        )
    }

    composable(AppRoute.ForgotPassword.route) {
        ForgotPasswordScreen(
            viewModel = authViewModel,
            onDone = { onNavigate(AppRoute.Login) },
            onBackToLogin = { onNavigate(AppRoute.Login) },
        )
    }

    composable(AppRoute.CompleteProfile.route) {
        CompleteProfileScreen(
            viewModel = profileViewModel,
            onDone = {
                onNavigate(AppRoute.Home)
                dashboardViewModel.refresh()
            },
            onSkip = { onNavigate(AppRoute.Home) },
        )
    }

    composable(AppRoute.Notifications.route) {
        NotificationsScreen(
            viewModel = notificationsViewModel,
            onBack = { navController.popBackStack() },
        )
    }

    composable(AppRoute.ApplyLoan.route) {
        ApplyLoanFlow(
            state = applyState,
            profile = profileViewModel.profile,
            onExit = { onNavigate(activeTab.destination) },
            onSubmit = { state ->
                applyViewModel.submit(state) { applicationId ->
                    applyState.reset()
                    onNavigate(AppRoute.Loans)
                    onRefreshAll()
                    onMessage(
                        "Application $applicationId submitted and sent to the marketing bucket."
                    )
                }
            },
        )
    }

    composable(AppRoute.PlafondUpgrade.route) {
        PlafondUpgradeScreen(
            profileViewModel = profileViewModel,
            onBack = {
                onNavigate(activeTab.destination)
                onRefreshAll()
            },
            onSubmitted = { message ->
                onNavigate(AppRoute.Home)
                onRefreshAll()
                onMessage(message)
            },
        )
    }
}
