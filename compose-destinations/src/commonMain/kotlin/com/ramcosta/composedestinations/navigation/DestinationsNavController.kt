package com.ramcosta.composedestinations.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.RouteOrDirection

/**
 * Implementation of [DestinationsNavigator] that uses
 * a [NavController] to navigate.
 */
internal class DestinationsNavController(
    private val navController: NavController,
) : DestinationsNavigator {

    override fun navigate(
        direction: Direction,
        builder: DestinationsNavOptionsBuilder.() -> Unit,
    ) {
        navController.navigate(direction.route) {
            DestinationsNavOptionsBuilder(this).apply(builder)
        }
    }

    override fun navigate(
        direction: Direction,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?
    ) {
       navController.navigate(direction.route, navOptions, navigatorExtras)
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
        route: RouteOrDirection,
        inclusive: Boolean,
        saveState: Boolean
    ): Boolean {
        return navController.popBackStack(route.route, inclusive, saveState)
    }

    @MainThread
    override fun clearBackStack(route: RouteOrDirection): Boolean {
        return navController.clearBackStack(route.route)
    }

    override fun getBackStackEntry(route: RouteOrDirection): NavBackStackEntry? {
        return runCatching { navController.getBackStackEntry(route.route) }.getOrNull()
    }
}