package com.ramcosta.composedestinations.spec

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink

typealias NavGraphSpec = TypedNavGraphSpec<*>

/**
 * Defines a navigation graph.
 */
interface TypedNavGraphSpec<T>: TypedRoute<T> {

    /**
     * Route string for this navigation graph which serves as
     * its [TypedRoute] identifier
     */
    override val route: String

    /**
     * Start [TypedRoute] of this navigation graph.
     */
    val startRoute: TypedRoute<T>

    /**
     * All destinations which belong to this navigation graph
     * by their route
     */
    val destinationsByRoute: Map<String, DestinationSpec>

    /**
     * Nested navigation graphs of this navigation graph.
     */
    val nestedNavGraphs: List<NavGraphSpec> get() = emptyList()

    /**
     * Function to get a [Direction] you can then pass to [com.ramcosta.composedestinations.navigation.DestinationsNavigator]
     * or to [NavController].navigateTo() to safely navigate to this [TypedRoute].
     */
    override fun invoke(navArgs: T): Direction = startRoute.invoke(navArgs)

    /**
     * All [NamedNavArgument]s that will be added to the navigation
     * graph for this destination
     */
    override val arguments: List<NamedNavArgument> get() = startRoute.arguments

    /**
     * All [NavDeepLink]s that will be added to the navigation
     * graph for this destination
     */
    override val deepLinks: List<NavDeepLink> get() = startRoute.deepLinks
}
