package com.ramcosta.composedestinations.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Route
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * The top level navigation graph associated with this [NavController].
 * Can only be called after [com.ramcosta.composedestinations.DestinationsNavHost].
 */
val NavController.navGraph: NavGraphSpec
    get() {
        return NavGraphSpecHolder.topLevelNavGraph(this)
            ?: error("Cannot call rootNavGraph before DestinationsNavHost!")
    }

/**
 * Finds the [DestinationSpec] correspondent to this [NavBackStackEntry].
 */
fun NavBackStackEntry.destination(): DestinationSpec<*> {
    val navGraphSpec = NavGraphSpecHolder.closestNavGraph(this)
        ?: error("Cannot call NavBackStackEntry.destination() before DestinationsNavHost!")

    return destination.route?.let { navGraphSpec.findDestination(it) }
        ?: navGraphSpec.startDestination
}

/**
 * Emits the currently active [DestinationSpec] whenever it changes. If
 * there is no active [DestinationSpec], no item will be emitted.
 */
val NavController.currentDestinationFlow: Flow<DestinationSpec<*>>
    get() = currentBackStackEntryFlow.map { it.destination() }

/**
 * Gets the current [DestinationSpec] as a [State].
 */
@Composable
fun NavController.currentDestinationAsState(): State<DestinationSpec<*>?> {
    return currentDestinationFlow.collectAsState(initial = null)
}

/**
 * If this [Route] is a [DestinationSpec], returns it
 *
 * If this [Route] is a [NavGraphSpec], returns its
 * start [DestinationSpec].
 */
val Route.startDestination get(): DestinationSpec<*> {
    return when (this) {
        is DestinationSpec<*> -> this
        is NavGraphSpec -> startRoute.startDestination
    }
}

/**
 * Filters all destinations of this [NavGraphSpec] and its nested nav graphs with given [predicate]
 */
inline fun NavGraphSpec.filterDestinations(predicate: (DestinationSpec<*>) -> Boolean): List<DestinationSpec<*>> {
    return allDestinations.filter { predicate(it) }
}

/**
 * Checks if any destination of this [NavGraphSpec] matches with given [predicate]
 */
inline fun NavGraphSpec.anyDestination(predicate: (DestinationSpec<*>) -> Boolean): Boolean {
    return allDestinations.any { predicate(it) }
}

/**
 * Checks if this [NavGraphSpec] contains given [destination]
 */
fun NavGraphSpec.contains(destination: DestinationSpec<*>): Boolean {
    return allDestinations.contains(destination)
}

/**
 * Returns all [DestinationSpec]s including those of nested graphs
 */
val NavGraphSpec.allDestinations get(): List<DestinationSpec<*>> {
    val destinations = destinationsByRoute
        .values
        .toMutableList()

    nestedNavGraphs.forEach {
        destinations.addAll(it.allDestinations)
    }
    return destinations
}

/**
 * Finds a destination for a `route` in this navigation graph
 * or its nested graphs.
 * Returns `null` if there is no such destination.
 */
fun NavGraphSpec.findDestination(route: String): DestinationSpec<*>? {
    val destination = destinationsByRoute[route]

    if (destination != null) {
        return destination
    }

    nestedNavGraphs.forEach {
        val nestedDestination = it.findDestination(route)

        if (nestedDestination != null) {
            return nestedDestination
        }
    }

    return null
}

// region deprecated APIs

/**
 * Finds the destination correspondent to this [NavBackStackEntry] in [navGraph] and its nested nav graphs,
 * null if none is found or if no route is set in this back stack entry's destination.
 */
@Deprecated(
    message = "Api will be removed! Use `destination` instead.",
    replaceWith = ReplaceWith("destination()")
)
fun NavBackStackEntry.destination(navGraph: NavGraphSpec): DestinationSpec<*>? {
    return destination.route?.let { navGraph.findDestination(it) }
}

/**
 * Finds the destination correspondent to this [NavBackStackEntry] in [navGraph] and its nested nav graphs,
 * null if none is found or if no route is set in this back stack entry's destination.
 */
@Deprecated(
    message = "Api will be removed! Use `destination(NavGraphSpec)` instead.",
    replaceWith = ReplaceWith("destination()")
)
fun NavBackStackEntry.destinationSpec(navGraph: NavGraphSpec): DestinationSpec<*>? {
    return destination.route?.let { navGraph.findDestination(it) }
}

/**
 * If this [Route] is a [DestinationSpec], returns it
 *
 * If this [Route] is a [NavGraphSpec], returns its
 * start [DestinationSpec].
 */
@Deprecated(
    message = "Api will be removed! Use `startDestination` instead.",
    replaceWith = ReplaceWith("startDestination")
)
val Route.startDestinationSpec get(): DestinationSpec<*> {
    return when (this) {
        is DestinationSpec<*> -> this
        is NavGraphSpec -> startRoute.startDestination
    }
}

// endregion