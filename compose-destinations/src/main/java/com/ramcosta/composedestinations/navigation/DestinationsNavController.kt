package com.ramcosta.composedestinations.navigation

import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Implementation of [DestinationsNavigator] that uses
 * a [NavController] to navigate.
 */
class DestinationsNavController(
    private val navController: NavController,
    private val navBackStackEntry: NavBackStackEntry,
) : DestinationsNavigator {

    override fun navigate(
        route: String,
        onlyIfResumed: Boolean,
        builder: NavOptionsBuilder.() -> Unit,
    ) {
        if (onlyIfResumed && navBackStackEntry.lifecycle.currentState != Lifecycle.State.RESUMED) {
            return
        }

        navController.navigate(route, builder)
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