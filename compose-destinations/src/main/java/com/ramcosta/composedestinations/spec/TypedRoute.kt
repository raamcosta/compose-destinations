package com.ramcosta.composedestinations.spec

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavController
import androidx.navigation.NavDeepLink

typealias Route = TypedRoute<*>

/**
 * Interface for all classes which instances
 * are identified by a route.
 *
 * All [TypedDestinationSpec] and [TypedNavGraphSpec] are
 * [TypedRoute].
 *
 * [TypedRoute] instances are not suited to navigate
 * to unless they're also [Direction].
 */
sealed interface TypedRoute<T> {

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
     * Function to get a [Direction] you can then pass to [com.ramcosta.composedestinations.navigation.DestinationsNavigator]
     * or to [NavController].navigateTo() to safely navigate to this [TypedRoute].
     */
    operator fun invoke(navArgs: T): Direction
}
