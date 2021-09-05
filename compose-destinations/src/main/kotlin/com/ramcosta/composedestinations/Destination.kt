package com.ramcosta.composedestinations

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NamedNavArgument

/**
 * Defines what a Destination needs to have to be able to be
 * added to a navigation graph and composed on the screen
 * when the user navigates to it.
 */
interface Destination {

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
     * [Composable] function that will be called to compose
     * the destination content in the screen, when the user
     * navigates to it.
     *
     * [scaffoldState] can be null if this Destination is not
     * a part of any Scaffold
     *
     */
    @Composable
    fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState?
    )
}