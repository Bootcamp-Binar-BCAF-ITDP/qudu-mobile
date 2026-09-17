package com.example.test2.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.test2.di.rememberAppContainer
import com.example.test2.di.rememberViewModelFactory
import com.example.test2.ui.apply.ApplyViewModel
import com.example.test2.ui.apply.ScreenBg
import com.example.test2.ui.apply.rememberApplyLoanState
import com.example.test2.ui.auth.AuthViewModel
import com.example.test2.ui.common.QuDuBottomBar
import com.example.test2.ui.common.QuDuTopBar
import com.example.test2.ui.dashboard.DashboardViewModel
import com.example.test2.ui.loans.ApplicationsViewModel
import com.example.test2.ui.notifications.NotificationsViewModel
import com.example.test2.ui.profile.ProfileViewModel
import com.example.test2.ui.simulator.SimulatorViewModel
import kotlinx.coroutines.launch

private const val SESSION_EXPIRED = "Your session has expired. Please sign in again."

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScaffold(modifier: Modifier = Modifier) {

    val container = rememberAppContainer()
    val factory = rememberViewModelFactory()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()

    val session by container.sessionStore.session.collectAsState(initial = null)
    val signedIn = session != null

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: AppRoute.Home.route

    var activeTab by rememberSaveable { mutableStateOf(TopLevelDestination.Home) }

    val applyState = rememberApplyLoanState()

    var profilePromptShown by rememberSaveable { mutableStateOf(false) }

    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val applyViewModel: ApplyViewModel = viewModel(factory = factory)
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)
    val applicationsViewModel: ApplicationsViewModel = viewModel(factory = factory)
    val notificationsViewModel: NotificationsViewModel = viewModel(factory = factory)
    val simulatorViewModel: SimulatorViewModel = viewModel(factory = factory)
    val authViewModel: AuthViewModel = viewModel(factory = factory)

    fun refreshAll() {
        dashboardViewModel.refresh()
        profileViewModel.refresh()
        applicationsViewModel.refresh()
        notificationsViewModel.refreshUnreadCount()
    }

    fun pullRefreshHome() {
        dashboardViewModel.refresh(userInitiated = true)
        profileViewModel.refresh(userInitiated = true)
        applicationsViewModel.refresh(userInitiated = true)
        notificationsViewModel.refreshUnreadCount()
        simulatorViewModel.load(userInitiated = true)
    }

    fun refreshFromServerEvent() {
        refreshAll()
        notificationsViewModel.refresh()
    }

    fun goTo(route: AppRoute) {
        val moved = navController.navigateTo(route)
        if (!moved) return

        TopLevelDestination.fromRoute(route.route)?.let { activeTab = it }
    }

    fun navigate(route: AppRoute) {
        goTo(if (!signedIn && route in AppRoute.requiringAuth) AppRoute.Login else route)
    }

    LaunchedEffect(currentRoute) {
        TopLevelDestination.fromRoute(currentRoute)?.let { activeTab = it }
    }

    LaunchedEffect(signedIn) {
        if (signedIn) {
            if (currentRoute == AppRoute.Login.route || currentRoute == AppRoute.Register.route) {
                navController.navigateHomeClearingAuth()
                activeTab = TopLevelDestination.Home
            }
            refreshAll()

            scope.launch { container.authRepository.syncDeviceToken() }
        } else {
            // Only someone who was actually inside a signed-in screen gets sent
            // to Login. A guest browsing Home or the simulator has not lost a
            // session and must not be interrupted.
            if (AppRoute.requiresAuth(currentRoute)) {
                goTo(AppRoute.Login)
                scope.launch { snackbarHostState.showSnackbar(SESSION_EXPIRED) }
            }
            profilePromptShown = false
        }
    }

    LaunchedEffect(signedIn) {
        if (!signedIn) return@LaunchedEffect
        container.appEvents.refreshRequests.collect { refreshFromServerEvent() }
    }

    var firstResumeSeen by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, signedIn) {
        val observer = LifecycleEventObserver { _, event ->
            if (event != Lifecycle.Event.ON_RESUME) return@LifecycleEventObserver
            if (!firstResumeSeen) {
                firstResumeSeen = true
            } else if (signedIn) {
                refreshFromServerEvent()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) { simulatorViewModel.load() }

    LaunchedEffect(simulatorViewModel.tiers) { applyState.tiers = simulatorViewModel.tiers }

    LaunchedEffect(profileViewModel.profileComplete, signedIn) {
        if (signedIn && profileViewModel.profileComplete == false && !profilePromptShown) {
            profilePromptShown = true
            goTo(AppRoute.CompleteProfile)
        }
    }

    val imeVisible = WindowInsets.isImeVisible

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .imePadding()
    ) {

        val showChrome = AppRoute.showsChrome(currentRoute)

        Column(Modifier.fillMaxSize()) {

            if (showChrome) {
                QuDuTopBar(
                    unreadCount = notificationsViewModel.unread,
                    showActions = signedIn,
                    onNotifications = { navigate(AppRoute.Notifications) },
                    onProfile = { goTo(AppRoute.Profile) },
                )
            }
            Box(
                Modifier
                    .weight(1f)
                    .background(ScreenBg)
            ) {
                AppNavHost(
                    navController = navController,
                    signedIn = signedIn,
                    activeTab = activeTab,
                    dependencies = AppNavDependencies(
                        dashboardViewModel = dashboardViewModel,
                        applyViewModel = applyViewModel,
                        profileViewModel = profileViewModel,
                        applicationsViewModel = applicationsViewModel,
                        notificationsViewModel = notificationsViewModel,
                        simulatorViewModel = simulatorViewModel,
                        authViewModel = authViewModel,
                        applyState = applyState,
                    ),
                    onNavigate = ::goTo,
                    onNavigateWithAuth = ::navigate,
                    onRefreshAll = ::refreshAll,
                    onPullRefreshHome = ::pullRefreshHome,
                    onMessage = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    },
                )
            }

            if (showChrome && !imeVisible) {
                QuDuBottomBar(
                    selected = activeTab,
                    onSelect = { destination -> goTo(destination.destination) },
                )
            }
        }

        LaunchedEffect(applyViewModel.error) {
            val message = applyViewModel.error ?: return@LaunchedEffect

            when {
                applyViewModel.overPlafond -> {
                    snackbarHostState.showSnackbar(message)
                    goTo(AppRoute.PlafondUpgrade)
                }

                applyViewModel.profileIncomplete -> {
                    snackbarHostState.showSnackbar(message)
                    profileViewModel.refresh()
                    goTo(AppRoute.CompleteProfile)
                }

                else -> snackbarHostState.showSnackbar(message)
            }
            applyViewModel.clearMessages()
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
