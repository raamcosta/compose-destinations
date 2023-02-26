package com.ramcosta.composedestinations.navigation

import androidx.annotation.MainThread
import androidx.compose.runtime.Stable
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Route

/**
 * Contract for a navigator of [DestinationSpec].
 * It uses components of [NavController] so implementations
 * will need one to do actual navigation.
 *
 * It is meant as a dependency inversion wrapper to make
 * Composables that depend on it be testable and "preview-able".
 */
@Stable
interface DestinationsNavigator {

    /**
     * Navigates to the given [Direction].
     * [NavGraphSpec] are [Direction]. Generated `Destinations` are Direction if they don't have
     * any navigation arguments or you can call their `invoke` method passing the arguments
     * to get a [Direction] instance.
     *
     * @param onlyIfResumed if true, will ignore the navigation action if the current `NavBackStackEntry`
     * is not in the RESUMED state. This avoids duplicate navigation actions.
     * By default is false to have the same behaviour as [NavController].
     *
     * @param builder [NavOptionsBuilder]
     *
     * @see [NavController.navigate]
     */
    fun navigate(
        direction: Direction,
        onlyIfResumed: Boolean = false,
        builder: NavOptionsBuilder.() -> Unit = {},
    ) {
        navigate(direction.route, onlyIfResumed, builder)
    }

    /**
     * Navigates to the given [route]
     *
     * @param onlyIfResumed if true, will ignore the navigation action if the current `NavBackStackEntry`
     * is not in the RESUMED state. This avoids duplicate navigation actions.
     * By default is false to have the same behaviour as [NavController].
     *
     * @param builder [NavOptionsBuilder]
     *
     * @see [NavController.navigate]
     */
    fun navigate(
        route: String,
        onlyIfResumed: Boolean = false,
        builder: NavOptionsBuilder.() -> Unit = {}
    )

    /**
     * Navigates to the given [Direction].
     * [NavGraphSpec] are [Direction]. Generated `Destinations` are Direction if they don't have
     * any navigation arguments or you can call their `invoke` method passing the arguments
     * to get a [Direction] instance.
     *
     * @param onlyIfResumed if true, will ignore the navigation action if the current `NavBackStackEntry`
     * is not in the RESUMED state. This avoids duplicate navigation actions.
     * By default is false to have the same behaviour as [NavController].
     *
     * @param navOptions [NavOptions]
     * @param navigatorExtras [Navigator.Extras]
     *
     * @see [NavController.navigate]
     */
    fun navigate(
        direction: Direction,
        onlyIfResumed: Boolean = false,
        navOptions: NavOptions? = null,
        navigatorExtras: Navigator.Extras? = null
    ) {
        navigate(direction.route, onlyIfResumed, navOptions, navigatorExtras)
    }

    /**
     * Navigates to the given [route]
     *
     * @param onlyIfResumed if true, will ignore the navigation action if the current `NavBackStackEntry`
     * is not in the RESUMED state. This avoids duplicate navigation actions.
     * By default is false to have the same behaviour as [NavController].
     *
     * @param navOptions [NavOptions]
     * @param navigatorExtras [Navigator.Extras]
     *
     * @see [NavController.navigate]
     */
    fun navigate(
        route: String,
        onlyIfResumed: Boolean = false,
        navOptions: NavOptions? = null,
        navigatorExtras: Navigator.Extras? = null
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
        route: Route,
        inclusive: Boolean,
        saveState: Boolean = false,
    ): Boolean {
        return popBackStack(route.route, inclusive, saveState)
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
    fun clearBackStack(route: Route): Boolean = clearBackStack(route.route)

    /**
     * @see [NavController.clearBackStack]
     */
    @MainThread
    fun clearBackStack(route: String): Boolean
}
