package com.ramcosta.composedestinations.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator

/**
 * Implementation of [DestinationsNavigator] that uses
 * a [NavController] to navigate.
 */
internal class DestinationsNavController(
    private val navController: NavController,
    private val isCurrentBackStackEntryResumed: () -> Boolean,
) : DestinationsNavigator {

    override fun navigate(
        route: String,
        onlyIfResumed: Boolean,
        builder: NavOptionsBuilder.() -> Unit,
    ) {
        if (onlyIfResumed && !isCurrentBackStackEntryResumed()) {
            return
        }

        navController.navigate(route, builder)
    }

    override fun navigate(
        route: String,
        onlyIfResumed: Boolean,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?
    ) {
        if (onlyIfResumed && !isCurrentBackStackEntryResumed()) {
            return
        }

        navController.navigate(route, navOptions, navigatorExtras)
    }

    @MainThread
    override fun navigateUp(): Boolean {
        return navController.navigateUp()
    }

    @MainThread
    override fun popBackStack(): Boolean {
        return navController.popBackStack()
    }

    @MainThread
    override fun popBackStack(
        route: String,
        inclusive: Boolean,
        saveState: Boolean,
    ): Boolean {
        return navController.popBackStack(route, inclusive, saveState)
    }

    @MainThread
    override fun clearBackStack(route: String): Boolean {
        return navController.clearBackStack(route)
    }
}