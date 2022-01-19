package com.ramcosta.composedestinations.utils

import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Route


/**
 * If this [Route] is a [DestinationSpec], returns it
 *
 * If this [Route] is a [NavGraphSpec], returns its
 * start [DestinationSpec].
 */
val Route.startDestinationSpec get(): DestinationSpec<*> {
    return when (this) {
        is DestinationSpec<*> -> this
        is NavGraphSpec -> startRoute.startDestinationSpec
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