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
        return NavGraphRegistry[this]?.topLevelNavGraph(this)
            ?: error("Cannot call rootNavGraph before DestinationsNavHost!")
    }

/**
 * Finds the [DestinationSpec] correspondent to this [NavBackStackEntry].
 * Some [NavBackStackEntry] are not [DestinationSpec], but are [NavGraphSpec] instead.
 * If you want a method that works for both, use [route] extension function instead.
 *
 * Use this ONLY if you're sure your [NavBackStackEntry] corresponds to a [DestinationSpec],
 * for example when converting from "current NavBackStackEntry", since a [NavGraphSpec] is never
 * the "current destination" shown on screen.
 */
fun NavBackStackEntry.destination(): DestinationSpec<*> {
    return when (val route = route()) {
        is DestinationSpec<*> -> route
        is NavGraphSpec -> error(
            "Cannot call `destination()` for a NavBackStackEntry which corresponds to a nav graph, use `route()` instead!"
        )
    }
}

/**
 * Finds the [Route] (so either a [DestinationSpec] or a [NavGraphSpec])
 * correspondent to this [NavBackStackEntry].
 */
fun NavBackStackEntry.route(): Route {
    val registry = NavGraphRegistry[this]
        ?: error("Cannot call NavBackStackEntry.destination() before DestinationsNavHost!")

    val navGraph = registry.navGraph(this)
    if (navGraph != null) {
        return navGraph
    }

    // If it's not a nav graph, then it must have a parent
    val parentNavGraph = registry.parentNavGraph(this)!!
    return destination.route?.let { parentNavGraph.findDestination(it) }
        ?: parentNavGraph.startDestination
}

/**
 * Finds the [NavGraphSpec] that this [NavBackStackEntry] belongs to.
 * If [NavBackStackEntry] corresponds to the top level nav graph (i.e, there is no parent),
 * then this returns the top level [NavGraphSpec].
 */
fun NavBackStackEntry.navGraph(): NavGraphSpec {
    val registry = NavGraphRegistry[this]
        ?: error("Cannot call NavBackStackEntry.parentNavGraph() before DestinationsNavHost!")

    return registry.parentNavGraph(this) ?: route() as NavGraphSpec
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
 * Checks if a given [Route] (which is either [com.ramcosta.composedestinations.spec.NavGraphSpec]
 * or [com.ramcosta.composedestinations.spec.DestinationSpec]) is currently somewhere in the back stack.
 */
fun NavController.isRouteOnBackStack(route: Route): Boolean {
    return runCatching { getBackStackEntry(route.route) }.isSuccess
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

    for (nestedGraph in nestedNavGraphs) {
        val nestedDestination = nestedGraph.findDestination(route)

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