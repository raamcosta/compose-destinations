package com.ramcosta.composedestinations.spec

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.NamedNavArgument
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder

/**
 * Defines what a Destination needs to have to be able to be
 * added to a navigation graph and composed on the screen
 * when the user navigates to it.
 */
interface DestinationSpec {

    /**
     * Full route that will be added to the navigation graph
     */
    val route: String

    /**
     * All [NamedNavArgument]s that will be added to the navigation
     * graph for this destination
     */
    val arguments: List<NamedNavArgument> get() = emptyList()

    /**
     * All [NavDeepLink]s that will be added to the navigation
     * graph for this destination
     */
    val deepLinks: List<NavDeepLink> get() = emptyList()

    /**
     * Style of this destination. It can be one of:
     * - [DestinationStyle.Default]
     * - [DestinationStyle.Animated]
     * - [DestinationStyle.BottomSheet]
     * - [DestinationStyle.Dialog]
     */
    val style: DestinationStyle get() = DestinationStyle.Default

    /**
     * [Composable] function that will be called to compose
     * the destination content in the screen, when the user
     * navigates to it.
     *
     * [dependenciesContainerBuilder] will be called with a [DependenciesContainerBuilder]
     * and give opportunity for other Composables on the call stack to add
     * dependencies that can be used in this destination.
     * You can add dependencies via `destinationDependencies` argument of `DestinationsNavHost` call.
     *
     * Besides, it is used internally to enable certain destination Composables
     * to be extension functions on `ColumnScope` (for [DestinationStyle.BottomSheet] destinations)
     * or `AnimatedVisibilityScope` (for [DestinationStyle.Animated]).
     */
    @Composable
    fun Content(
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.() -> Unit
    )
}