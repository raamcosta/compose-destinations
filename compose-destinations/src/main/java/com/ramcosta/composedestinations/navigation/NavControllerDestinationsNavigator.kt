package com.ramcosta.composedestinations.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.ramcosta.composedestinations.spec.Routed

/**
 * Implementation of [DestinationsNavigator] that uses
 * a [NavController] to navigate.
 */
class NavControllerDestinationsNavigator(
    private val navController: NavController,
    private val navBackStackEntry: NavBackStackEntry
) : DestinationsNavigator {

    override fun navigate(routed: Routed, onlyIfResumed: Boolean, builder: NavOptionsBuilder.() -> Unit) {
        navigate(routed.route, onlyIfResumed, builder)
    }

    override fun navigate(route: String, onlyIfResumed: Boolean, builder: NavOptionsBuilder.() -> Unit) {
        if (onlyIfResumed && navBackStackEntry.lifecycle.currentState != Lifecycle.State.RESUMED) {
            return
        }

        navController.navigate(route, builder)
    }

    override fun navigateUp(): Boolean {
        return navController.navigateUp()
    }
}