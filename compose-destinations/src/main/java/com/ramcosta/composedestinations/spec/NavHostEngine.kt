package com.ramcosta.composedestinations.spec

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import com.ramcosta.composedestinations.animations.defaults.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCalls
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder

/**
 * Abstraction over all needed functionality to call a "NavHost-like" composable
 * and add the nested navigation graphs and the composables.
 * Also has a way to get the best suited [NavHostController].
 *
 * This is passed in to the [com.ramcosta.composedestinations.DestinationsNavHost] call.
 * By default the engine of the main core will be used, however, if you are using
 * "io.github.raamcosta.compose-destinations:animations-core" you must pass in the animated
 * version of the engine which can be obtained by calling `rememberAnimatedNavHostEngine`.
 */
interface NavHostEngine {

    enum class Type {

        /**
         * The engine you get by default by using the library
         */
        DEFAULT,

        /**
         * The engine you get if using "io.github.raamcosta.compose-destinations:wear-core"
         * and calling `rememberWearNavHostEngine`
         */
        WEAR
    }

    /**
     * Engine type between [Type.DEFAULT] or [Type.ANIMATED]
     */
    val type: Type

    /**
     * Returns the [NavHostController] best suited for this [NavHostEngine]
     */
    @Composable
    fun rememberNavController(
        vararg navigators: Navigator<out NavDestination>
    ): NavHostController

    /**
     * "NavHost-like" Composable for this engine.
     */
    @Composable
    fun NavHost(
        modifier: Modifier,
        route: String,
        startRoute: Route,
        defaultTransitions: NavHostAnimatedDestinationStyle,
        navController: NavHostController,
        builder: NavGraphBuilder.() -> Unit,
    )

    /**
     * Creates a new navigation graph nested in the [NavGraphBuilder] graph.
     */
    fun NavGraphBuilder.navigation(
        navGraph: NavGraphSpec,
        builder: NavGraphBuilder.() -> Unit
    )

    /**
     * Adds a specific [DestinationSpec] to this [NavGraphBuilder]
     */
    fun <T> NavGraphBuilder.composable(
        destination: DestinationSpec<T>,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
        manualComposableCalls: ManualComposableCalls,
    )
}