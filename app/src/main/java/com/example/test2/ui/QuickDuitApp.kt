package com.example.test2.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test2.di.rememberAppContainer
import com.example.test2.di.rememberViewModelFactory
import com.example.test2.ui.apply.ApplyLoanFlow
import com.example.test2.ui.apply.ApplyViewModel
import com.example.test2.ui.apply.rememberApplyLoanState
import com.example.test2.ui.auth.ForgotPasswordScreen
import com.example.test2.ui.auth.LoginScreen
import com.example.test2.ui.auth.RegisterScreen
import com.example.test2.ui.common.AppTab
import com.example.test2.ui.common.QuDuBottomBar
import com.example.test2.ui.common.QuDuTopBar
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
import kotlinx.coroutines.launch

private enum class Screen(val tab: AppTab? = null) {
    Home(AppTab.Home),
    Simulate(AppTab.Simulate),
    Loans(AppTab.Loans),
    History(AppTab.History),
    Profile(AppTab.Profile),

    Notifications,
    ApplyLoan,
    PlafondUpgrade,
    Login,
    Register,
    ForgotPassword,
    CompleteProfile,
}

private fun tabScreen(tab: AppTab): Screen = when (tab) {
    AppTab.Home -> Screen.Home
    AppTab.Simulate -> Screen.Simulate
    AppTab.Loans -> Screen.Loans
    AppTab.History -> Screen.History
    AppTab.Profile -> Screen.Profile
}

private fun requiresAuth(target: Screen): Boolean = when (target) {
    Screen.Notifications, Screen.ApplyLoan, Screen.PlafondUpgrade, Screen.CompleteProfile -> true
    else -> false
}

@Composable
fun QuickDuitApp(modifier: Modifier = Modifier) {

    val container = rememberAppContainer()
    val factory = rememberViewModelFactory()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val session by container.sessionStore.session.collectAsState(initial = null)
    val signedIn = session != null

    var screen by remember { mutableStateOf(Screen.Home) }

    var activeTab by remember { mutableStateOf(AppTab.Home) }

    val applyState = rememberApplyLoanState()

    var profilePromptShown by remember { mutableStateOf(false) }

    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
    val applyViewModel: ApplyViewModel = viewModel(factory = factory)
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)
    val applicationsViewModel: ApplicationsViewModel = viewModel(factory = factory)
    val notificationsViewModel: NotificationsViewModel = viewModel(factory = factory)
    val simulatorViewModel: SimulatorViewModel = viewModel(factory = factory)

    fun refreshAll() {
        dashboardViewModel.refresh()
        profileViewModel.refresh()
        applicationsViewModel.refresh()
        notificationsViewModel.refreshUnreadCount()
    }

    fun refreshFromServerEvent() {
        refreshAll()
        notificationsViewModel.refresh()
    }

    fun goTo(target: Screen) {
        target.tab?.let { activeTab = it }
        screen = target
    }

    fun navigate(target: Screen) {
        goTo(if (!signedIn && requiresAuth(target)) Screen.Login else target)
    }

    LaunchedEffect(signedIn) {
        if (signedIn) {
            if (screen == Screen.Login || screen == Screen.Register) {
                goTo(Screen.Home)
            }
            refreshAll()

            scope.launch { container.authRepository.syncDeviceToken() }
        } else {
            if (requiresAuth(screen)) {
                goTo(tabScreen(activeTab))
            }
            profilePromptShown = false
        }
    }

    // (A) Push while the app is open: the FCM service publishes onto AppEvents
    // and the screen reloads itself, so an approved limit increase shows up
    // without the customer navigating anywhere.
    //
    // Keyed on signedIn rather than capturing it, because the collector would
    // otherwise hold whichever value it was born with and keep firing tokenless
    // calls after a sign-out.
    LaunchedEffect(signedIn) {
        if (!signedIn) return@LaunchedEffect
        container.appEvents.refreshRequests.collect { refreshFromServerEvent() }
    }

    // (B) The other half. A push carrying a notification payload goes to the
    // system tray without waking onMessageReceived whenever the app is not in
    // the foreground, so (A) hears nothing at all in the common case: phone
    // locked, decision made, phone unlocked. Coming back to the app re-reads.
    //
    // The first ON_RESUME is skipped: it fires during the launch that the
    // signedIn effect above is already loading for, and refreshing twice at
    // cold start is eight redundant calls for no new information.
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

    // Fetched once at startup rather than on the Simulate tab: the Home card and
    // the apply wizard both quote from it, and neither is reachable only through
    // that tab. load() is a no-op while one is already in flight.
    LaunchedEffect(Unit) { simulatorViewModel.load() }

    // The wizard quotes rates off the same tier table the simulator shows.
    LaunchedEffect(simulatorViewModel.tiers) { applyState.tiers = simulatorViewModel.tiers }

    LaunchedEffect(profileViewModel.profileComplete, signedIn) {
        if (signedIn && profileViewModel.profileComplete == false && !profilePromptShown) {
            profilePromptShown = true
            goTo(Screen.CompleteProfile)
        }
    }

    BackHandler(enabled = screen != Screen.Home) {
        goTo(
            when {
                screen == Screen.Register || screen == Screen.ForgotPassword -> Screen.Login
                screen.tab != null -> Screen.Home
                else -> tabScreen(activeTab)
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {

        Column(Modifier.fillMaxSize()) {

            QuDuTopBar(
                unreadCount = notificationsViewModel.unread,
                showActions = signedIn,
                onNotifications = { navigate(Screen.Notifications) },
                onProfile = { goTo(Screen.Profile) },
            )

            Box(Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = screen,
                    transitionSpec = {
                        if (initialState.tab != null && targetState.tab != null) {
                            fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                        } else if (targetState.ordinal > initialState.ordinal) {
                            (slideInHorizontally(tween(250)) { it } + fadeIn()) togetherWith
                                    (slideOutHorizontally(tween(250)) { -it / 4 } + fadeOut())
                        } else {
                            (slideInHorizontally(tween(250)) { -it / 4 } + fadeIn()) togetherWith
                                    (slideOutHorizontally(tween(250)) { it } + fadeOut())
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    label = "screen"
                ) { current ->

                    when (current) {

                        Screen.Home -> DashboardScreen(
                            signedIn = signedIn,
                            userName = dashboardViewModel.customerName.ifBlank { "Customer" },
                            plafond = dashboardViewModel.plafond,
                            latestApplication = dashboardViewModel.latestApplication,
                            isLoading = dashboardViewModel.isLoading,
                            errorMessage = dashboardViewModel.error,
                            profileComplete = profileViewModel.profileComplete != false,
                            missingDocuments = profileViewModel.missingDocuments,
                            unreadCount = notificationsViewModel.unread,
                            simulatorViewModel = simulatorViewModel,
                            onApplyLoan = {
                                if (signedIn) applyViewModel.clearMessages()
                                navigate(Screen.ApplyLoan)
                            },
                            onCompleteProfile = { goTo(Screen.CompleteProfile) },
                            onSeeLoans = { goTo(Screen.Loans) },
                            onAlerts = { navigate(Screen.Notifications) },
                            onRequestUpgrade = { navigate(Screen.PlafondUpgrade) },
                            onLogin = { goTo(Screen.Login) },
                            onRegister = { goTo(Screen.Register) },
                        )

                        Screen.Simulate -> SimulateScreen(
                            viewModel = simulatorViewModel,
                            signedIn = signedIn,
                            onApplyLoan = {
                                if (signedIn) applyViewModel.clearMessages()
                                navigate(Screen.ApplyLoan)
                            },
                        )

                        // navigate(), not goTo(): this screen has no sign-in wall
                        // any more, so a visitor can reach these buttons and the
                        // auth check has to happen on the way out of them.
                        Screen.Loans -> LoansScreen(
                            viewModel = applicationsViewModel,
                            signedIn = signedIn,
                            onApplyLoan = {
                                if (signedIn) applyViewModel.clearMessages()
                                navigate(Screen.ApplyLoan)
                            },
                            onRequestUpgrade = { navigate(Screen.PlafondUpgrade) },
                        )

                        Screen.History -> HistoryScreen(
                            viewModel = applicationsViewModel,
                            signedIn = signedIn,
                            onLogin = { goTo(Screen.Login) },
                            onRegister = { goTo(Screen.Register) },
                        )

                        Screen.Profile -> ProfileScreen(
                            viewModel = profileViewModel,
                            signedIn = signedIn,
                            onLogout = { },
                            onLogin = { goTo(Screen.Login) },
                            onRegister = { goTo(Screen.Register) },
                        )

                        Screen.Login -> LoginScreen(
                            onLoggedIn = { goTo(Screen.Home) },
                            onGoToRegister = { goTo(Screen.Register) },
                            onForgotPassword = { goTo(Screen.ForgotPassword) },
                            onBack = { goTo(tabScreen(activeTab)) },
                        )

                        Screen.Register -> RegisterScreen(
                            onRegistered = { goTo(Screen.Login) },
                            onBackToLogin = { goTo(Screen.Login) },
                            // Same destination, separate callback: the message
                            // explaining the move is already set on the shared
                            // AuthViewModel and shows on the login screen.
                            onEmailAlreadyRegistered = { goTo(Screen.Login) },
                        )

                        Screen.ForgotPassword -> ForgotPasswordScreen(
                            onDone = { goTo(Screen.Login) },
                            onBackToLogin = { goTo(Screen.Login) },
                        )

                        Screen.CompleteProfile -> CompleteProfileScreen(
                            viewModel = profileViewModel,
                            onDone = {
                                goTo(Screen.Home)
                                dashboardViewModel.refresh()
                            },
                            onSkip = { goTo(Screen.Home) },
                        )

                        Screen.Notifications -> NotificationsScreen(
                            viewModel = notificationsViewModel,
                            onBack = { goTo(tabScreen(activeTab)) },
                        )

                        Screen.ApplyLoan -> ApplyLoanFlow(
                            state = applyState,
                            profile = profileViewModel.profile,
                            onExit = { goTo(tabScreen(activeTab)) },
                            onSubmit = { state ->
                                applyViewModel.submit(state) { applicationId ->
                                    applyState.reset()
                                    goTo(Screen.Loans)
                                    refreshAll()
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Application $applicationId submitted and sent to the marketing bucket."
                                        )
                                    }
                                }
                            }
                        )

                        Screen.PlafondUpgrade -> PlafondUpgradeScreen(
                            profileViewModel = profileViewModel,
                            onBack = {
                                goTo(tabScreen(activeTab))
                                refreshAll()
                            },
                            onSubmitted = { message ->
                                // Home rather than back to whichever tab they
                                // came from: the request is filed and there is
                                // nothing further to do on the plafond screen.
                                goTo(Screen.Home)
                                refreshAll()
                                scope.launch { snackbarHostState.showSnackbar(message) }
                            },
                        )
                    }
                }
            }

            QuDuBottomBar(
                selected = activeTab,
                onSelect = { selected -> goTo(tabScreen(selected)) },
            )
        }

        LaunchedEffect(applyViewModel.error) {
            val message = applyViewModel.error ?: return@LaunchedEffect

            when {
                applyViewModel.overPlafond -> {
                    snackbarHostState.showSnackbar(message)
                    goTo(Screen.PlafondUpgrade)
                }

                applyViewModel.profileIncomplete -> {
                    snackbarHostState.showSnackbar(message)
                    profileViewModel.refresh()
                    goTo(Screen.CompleteProfile)
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
