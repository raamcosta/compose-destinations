package com.ramcosta.composedestinations.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Routed

/**
 * Contract for a navigator of [DestinationSpec].
 * It uses components of [NavController] so implementations
 * will need one to actual navigate.
 *
 * It is meant as a dependency inversion wrapper to make
 * Composables that depend on it be testable and "preview-able".
 */
interface DestinationsNavigator {

    /**
     * Navigates to the given [Routed] ([NavGraphSpec] and [DestinationSpec] are [Routed]).
     *
     * @param onlyIfResumed if true (default), will ignore the navigation action if the
     * current `NavBackStackEntry` is not in the RESUMED state. This avoids duplicate
     * navigation actions.
     * @param builder [NavOptionsBuilder]
     *
     * @see [NavController.navigate]
     */
    fun navigate(routed: Routed, onlyIfResumed: Boolean = true, builder: NavOptionsBuilder.() -> Unit = {}) {
        navigate(routed.route, onlyIfResumed, builder)
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
    fun navigate(route: String, onlyIfResumed: Boolean = true, builder: NavOptionsBuilder.() -> Unit = {})

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
    fun popBackStack(routed: Routed, inclusive: Boolean, saveState: Boolean = false): Boolean {
        return popBackStack(routed.route, inclusive, saveState)
    }

    /**
     * @see [NavController.popBackStack]
     */
    @MainThread
    fun popBackStack(route: String, inclusive: Boolean, saveState: Boolean = false): Boolean

    /**
     * @see [NavController.clearBackStack]
     */
    @MainThread
    fun clearBackStack(routed: Routed): Boolean = clearBackStack(routed.route)

    /**
     * @see [NavController.clearBackStack]
     */
    @MainThread
    fun clearBackStack(route: String): Boolean
}

/**
 * Empty implementation of [DestinationsNavigator]
 * Useful for tests and Composable previews.
 */
object EmptyDestinationsNavigator : DestinationsNavigator {

    override fun navigate(route: String, onlyIfResumed: Boolean, builder: NavOptionsBuilder.() -> Unit) = Unit

    override fun navigateUp() = false

    override fun popBackStack() = false

    override fun popBackStack(route: String, inclusive: Boolean, saveState: Boolean) = false

    override fun clearBackStack(route: String) = false
}

/**
 * Navigates to the [Routed].
 */
fun NavController.navigateTo(
    routed: Routed,
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(routed.route, navOptionsBuilder)
}

