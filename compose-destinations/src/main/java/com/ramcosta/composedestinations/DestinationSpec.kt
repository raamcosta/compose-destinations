package com.ramcosta.composedestinations

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.NamedNavArgument
import com.ramcosta.composedestinations.navigation.Routed
import kotlin.reflect.KClass

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
     * [Composable] function that will be called to compose
     * the destination content in the screen, when the user
     * navigates to it.
     *
     * [situationalParameters] can contain diverse things depending
     * on the situation of the screens
     */
    @Composable
    fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        situationalParameters: Map<KClass<*>, Any>
    )
}