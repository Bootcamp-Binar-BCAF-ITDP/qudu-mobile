package com.example.test2.ui.navigation

sealed interface AppRoute {

    val route: String


    data object Home : AppRoute {
        override val route = "home"
    }

    data object Simulate : AppRoute {
        override val route = "simulate"
    }

    data object Loans : AppRoute {
        override val route = "loans"
    }

    data object History : AppRoute {
        override val route = "history"
    }

    data object Profile : AppRoute {
        override val route = "profile"
    }


    data object Login : AppRoute {
        override val route = "login"
    }

    data object Register : AppRoute {
        override val route = "register"
    }

    data object ForgotPassword : AppRoute {
        override val route = "forgot-password"
    }

    data object CompleteProfile : AppRoute {
        override val route = "complete-profile"
    }

    data object Notifications : AppRoute {
        override val route = "notifications"
    }

    data object ApplyLoan : AppRoute {
        override val route = "apply"
    }

    data object PlafondUpgrade : AppRoute {
        override val route = "plafond-upgrade"
    }

    companion object {

        val entries: List<AppRoute> = listOf(
            Home, Simulate, Loans, History, Profile,
            Login, Register, ForgotPassword, CompleteProfile,
            Notifications, ApplyLoan, PlafondUpgrade,
        )

        val requiringAuth: Set<AppRoute> = setOf(
            Notifications, ApplyLoan, PlafondUpgrade, CompleteProfile,
        )

        fun requiresAuth(route: String?): Boolean =
            requiringAuth.any { it.route == route }

        fun of(route: String?): AppRoute? = entries.firstOrNull { it.route == route }
    }
}
