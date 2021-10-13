package com.ramcosta.composedestinations.spec

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.NamedNavArgument

/**
 * Defines what a Destination needs to have to be able to be
 * added to a navigation graph and composed on the screen
 * when the user navigates to it.
 */
interface DestinationSpec: Routed {

    /**
     * Full route that will be added to the navigation graph
     */
    override val route: String

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
     * [situationalParameters] can contain diverse things depending
     * on the situation of the screens
     */
    @Composable
    fun Content(
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        situationalParameters: Map<Class<*>, Any>
    )
}