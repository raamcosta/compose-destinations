package com.ramcosta.composedestinations.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.PopUpToBuilder
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.Route
import java.util.WeakHashMap

class DestinationsNavOptionsBuilder(
    private val jetpackBuilder: NavOptionsBuilder
) {

    var launchSingleTop
        get() = jetpackBuilder.launchSingleTop
        set(value) {
            jetpackBuilder.launchSingleTop = value
        }

    var restoreState
        get() = jetpackBuilder.restoreState
        set(value) {
            jetpackBuilder.restoreState = value
        }

    val popUpToRoute: String?
        get() = jetpackBuilder.popUpToRoute

    fun popUpTo(route: Route, popUpToBuilder: PopUpToBuilder.() -> Unit = {}) {
        jetpackBuilder.popUpTo(route.route, popUpToBuilder)
    }
}

private val navigators: WeakHashMap<NavController, DestinationsNavigator> = WeakHashMap()
val NavController.navigator: DestinationsNavigator
    get(): DestinationsNavigator {
        return navigators[this] ?: DestinationsNavController(this)
            .also { navigators[this] = it }
    }


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
        route: Route,
        inclusive: Boolean,
        saveState: Boolean
    ): Boolean {
        return navController.popBackStack(route.route, inclusive, saveState)
    }

    @MainThread
    override fun clearBackStack(route: Route): Boolean {
        return navController.clearBackStack(route.route)
    }
}