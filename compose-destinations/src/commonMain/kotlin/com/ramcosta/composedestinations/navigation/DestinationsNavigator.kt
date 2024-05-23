package com.ramcosta.composedestinations.navigation

import androidx.annotation.MainThread
import androidx.compose.runtime.Stable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.RouteOrDirection

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
     * @param builder [NavOptionsBuilder]
     *
     * @see [NavController.navigate]
     */
    fun navigate(
        direction: Direction,
        builder: DestinationsNavOptionsBuilder.() -> Unit,
    )

    /**
     * Navigates to the given [Direction].
     * [NavGraphSpec] are [Direction]. Generated `Destinations` are Direction if they don't have
     * any navigation arguments or you can call their `invoke` method passing the arguments
     * to get a [Direction] instance.
     *
     * @param navOptions [NavOptions]
     * @param navigatorExtras [Navigator.Extras]
     *
     * @see [NavController.navigate]
     */
    fun navigate(
        direction: Direction,
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
        route: RouteOrDirection,
        inclusive: Boolean,
        saveState: Boolean = false,
    ): Boolean

    /**
     * @see [NavController.clearBackStack]
     */
    @MainThread
    fun clearBackStack(route: RouteOrDirection): Boolean

    /**
     * Like [NavController.getBackStackEntry] but returns null if [RouteOrDirection]
     * is not in the back stack instead of throwing an exception.
     *
     * @see [NavController.getBackStackEntry]
     */
    fun getBackStackEntry(
        route: RouteOrDirection
    ): NavBackStackEntry?
}
