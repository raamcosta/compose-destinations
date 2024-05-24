package com.ramcosta.composedestinations.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.RouteOrDirection

/**
 * Empty implementation of [DestinationsNavigator]
 * Useful for tests and Composable previews.
 */
object EmptyDestinationsNavigator : DestinationsNavigator {

    override fun navigate(direction: Direction, builder: DestinationsNavOptionsBuilder.() -> Unit) = Unit

    override fun navigate(direction: Direction, navOptions: NavOptions?, navigatorExtras: Navigator.Extras?) = Unit

    override fun navigateUp() = false

    override fun popBackStack() = false

    override fun popBackStack(route: RouteOrDirection, inclusive: Boolean, saveState: Boolean): Boolean = false

    override fun clearBackStack(route: RouteOrDirection): Boolean = false

    override fun getBackStackEntry(route: RouteOrDirection): NavBackStackEntry? = null

}