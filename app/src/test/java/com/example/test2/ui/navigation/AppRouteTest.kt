package com.example.test2.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppRouteTest {

    @Test
    fun `every route string is unique, or navigation would open the wrong screen`() {
        val routes = AppRoute.entries.map { it.route }

        assertEquals(routes.size, routes.toSet().size)
    }

    @Test
    fun `the entry list holds every route object`() {
        assertEquals(12, AppRoute.entries.size)
    }

    @Test
    fun `a route string resolves back to its object`() {
        AppRoute.entries.forEach { assertEquals(it, AppRoute.of(it.route)) }
    }

    @Test
    fun `an unknown or missing route resolves to nothing`() {
        assertNull(AppRoute.of("nope"))
        assertNull(AppRoute.of(null))
    }

    @Test
    fun `applying, upgrading, notifications and profile completion need a session`() {
        listOf("apply", "plafond-upgrade", "notifications", "complete-profile").forEach {
            assertTrue(it, AppRoute.requiresAuth(it))
        }
    }

    @Test
    fun `the tabs and the sign-in screens are open to guests`() {
        listOf("home", "simulate", "loans", "history", "profile", "login", "register", "forgot-password").forEach {
            assertFalse(it, AppRoute.requiresAuth(it))
        }
    }

    @Test
    fun `an unknown route is not treated as protected`() {
        assertFalse(AppRoute.requiresAuth("nope"))
        assertFalse(AppRoute.requiresAuth(null))
    }

    @Test
    fun `no sign-in screen is ever protected, or a guest could never sign in`() {
        listOf(AppRoute.Login, AppRoute.Register, AppRoute.ForgotPassword).forEach {
            assertFalse(AppRoute.requiringAuth.contains(it))
        }
    }

    @Test
    fun `the five tabs map to the five tab routes in order`() {
        assertEquals(
            listOf("home", "simulate", "loans", "history", "profile"),
            TopLevelDestination.entries.map { it.route },
        )
    }

    @Test
    fun `a tab is recognised from its route`() {
        assertEquals(TopLevelDestination.Loans, TopLevelDestination.fromRoute("loans"))
        assertTrue(TopLevelDestination.isTopLevel("profile"))
    }

    @Test
    fun `a pushed screen is not a tab, so the bottom bar hides`() {
        listOf("apply", "login", "notifications", null).forEach {
            assertFalse("$it", TopLevelDestination.isTopLevel(it))
        }
    }

    @Test
    fun `no tab requires a session, since guests browse them too`() {
        TopLevelDestination.entries.forEach { assertFalse(it.route, AppRoute.requiresAuth(it.route)) }
    }

    @Test
    fun `the sign-in screens and the apply flow hide the header and the bottom bar`() {
        listOf("login", "register", "forgot-password", "apply").forEach {
            assertFalse(it, AppRoute.showsChrome(it))
        }
    }

    @Test
    fun `every other screen keeps its chrome`() {
        val hidden = AppRoute.chromeless.map { it.route }.toSet()

        AppRoute.entries
            .filterNot { it.route in hidden }
            .forEach { assertTrue(it.route, AppRoute.showsChrome(it.route)) }
    }

    @Test
    fun `every tab keeps its chrome, or the bottom bar could not be reached again`() {
        TopLevelDestination.entries.forEach { assertTrue(it.route, AppRoute.showsChrome(it.route)) }
    }

    @Test
    fun `an unknown route keeps its chrome rather than losing the bottom bar`() {
        assertTrue(AppRoute.showsChrome("nope"))
        assertTrue(AppRoute.showsChrome(null))
    }

    @Test
    fun `plafond upgrade and complete profile keep their chrome`() {
        assertTrue(AppRoute.showsChrome("plafond-upgrade"))
        assertTrue(AppRoute.showsChrome("complete-profile"))
    }
}
