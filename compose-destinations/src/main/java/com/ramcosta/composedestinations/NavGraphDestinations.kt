package com.ramcosta.composedestinations

import androidx.navigation.NavGraphBuilder
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec

/**
 * Adds all destinations of the [navGraphSpec] to this
 * [NavGraphBuilder] as well as all nested nav graphs.
 */
fun NavGraphBuilder.addNavGraphDestinations(
    navGraphSpec: NavGraphSpec,
    addComposable: NavGraphBuilder.(DestinationSpec) -> Unit,
    addNavigation: NavGraphBuilder.(NavGraphSpec, NavGraphBuilder.() -> Unit) -> Unit
) {
    navGraphSpec.destinations.values.forEach { destination ->
        addComposable(destination)
    }

    addNestedNavGraphs(navGraphSpec.nestedNavGraphs, addComposable, addNavigation)
}

/**
 * Filters all destinations of this [NavGraphSpec] and its nested nav graphs with given [predicate]
 */
inline fun NavGraphSpec.filterDestinations(predicate: (DestinationSpec) -> Boolean): List<DestinationSpec> {
    return allDestinations.filter { predicate(it) }
}

/**
 * Checks if any destination of this [NavGraphSpec] matches with given [predicate]
 */
inline fun NavGraphSpec.anyDestination(predicate: (DestinationSpec) -> Boolean): Boolean {
    return allDestinations.any { predicate(it) }
}

/**
 * Checks if this [NavGraphSpec] contains given [destination]
 */
fun NavGraphSpec.contains(destination: DestinationSpec): Boolean {
    return allDestinations.contains(destination)
}

/**
 * Returns all [DestinationSpec]s including those of nested graphs
 */
val NavGraphSpec.allDestinations get(): List<DestinationSpec> {
    val destinations = destinations
        .values
        .toMutableList()

    nestedNavGraphs.forEach {
        destinations.addAll(it.destinations.values)
    }
    return destinations
}

/**
 * Finds a destination for a `route` in this navigation graph
 * or its nested graphs.
 * Returns `null` if there is no such destination.
 */
fun NavGraphSpec.findDestination(route: String): DestinationSpec? {
    val destination = destinations[route]

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

private fun NavGraphBuilder.addNestedNavGraphs(
    nestedNavGraphs: List<NavGraphSpec>,
    addComposable: NavGraphBuilder.(DestinationSpec) -> Unit,
    addNavigation: NavGraphBuilder.(NavGraphSpec, NavGraphBuilder.() -> Unit) -> Unit
) {
    nestedNavGraphs.forEach { nestedGraph ->
        addNavigation(nestedGraph) {
            addNavGraphDestinations(
                nestedGraph,
                addComposable,
                addNavigation
            )
        }
    }
}