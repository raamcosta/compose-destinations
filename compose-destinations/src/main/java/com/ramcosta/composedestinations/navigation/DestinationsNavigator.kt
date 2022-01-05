package com.ramcosta.composedestinations.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.RouteIdentified

/**
 * Contract for a navigator of [DestinationSpec].
 * It uses components of [NavController] so implementations
 * will need one to do actual navigation.
 *
 * It is meant as a dependency inversion wrapper to make
 * Composables that depend on it be testable and "preview-able".
 */
interface DestinationsNavigator {

    /**
     * Navigates to the given [Direction].
     * [NavGraphSpec] are [Direction]. Generated `Destinations` are Direction if they don't have
     * any navigation arguments or you can call their `invoke` method passing the arguments
     * to get a [Direction] instance.
     *
     * @param onlyIfResumed if true (default), will ignore the navigation action if the
     * current `NavBackStackEntry` is not in the RESUMED state. This avoids duplicate
     * navigation actions.
     * @param builder [NavOptionsBuilder]
     *
     * @see [NavController.navigate]
     */
    fun navigate(
        direction: Direction,
        onlyIfResumed: Boolean = true,
        builder: NavOptionsBuilder.() -> Unit = {},
    ) {
        navigate(direction.route, onlyIfResumed, builder)
    }

    /**
     * Navigates to the given [route]
     *
     * @param onlyIfResumed if true (default), will ignore the navigation action if the current `NavBackStackEntry`
     * is not in the RESUMED state. This avoids duplicate navigation actions.
     * @param builder [NavOptionsBuilder]
     *
     * @see [NavController.navigate]
     */
    fun navigate(
        route: String,
        onlyIfResumed: Boolean = true,
        builder: NavOptionsBuilder.() -> Unit = {}
    )

    /**
     * @see [NavController.navigateUp]
     */
    @MainThread
    fun navigateUp(): Boolean

    /**
     * @see [NavController.popBackStack]
     */
    @MainThread
    fun popBackStack(): Boolean

    /**
     * @see [NavController.popBackStack]
     */
    @MainThread
    fun popBackStack(
        routeId: RouteIdentified,
        inclusive: Boolean,
        saveState: Boolean = false,
    ): Boolean {
        return popBackStack(routeId.route, inclusive, saveState)
    }

    /**
     * @see [NavController.popBackStack]
     */
    @MainThread
    fun popBackStack(
        route: String,
        inclusive: Boolean,
        saveState: Boolean = false,
    ): Boolean

    /**
     * @see [NavController.clearBackStack]
     */
    @MainThread
    fun clearBackStack(routeId: RouteIdentified): Boolean = clearBackStack(routeId.route)

    /**
     * @see [NavController.clearBackStack]
     */
    @MainThread
    fun clearBackStack(route: String): Boolean
}

/**
 * Navigates to the [Direction].
 */
fun NavController.navigateTo(
    direction: Direction,
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(direction.route, navOptionsBuilder)
}

