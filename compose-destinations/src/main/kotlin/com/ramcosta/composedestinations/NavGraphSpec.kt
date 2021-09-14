package com.ramcosta.composedestinations

/**
 * Defines a navigation graph.
 */
interface NavGraphSpec {

    /**
     * Unique name of this navigation graph.
     * It can be used to navigate to it.
     */
    val name: String

    /**
     * Start destination of this navigation graph.
     */
    val startDestination: DestinationSpec

    /**
     * All destinations which belong to this navigation graph
     * by their route
     */
    val destinations: Map<String, DestinationSpec>

    /**
     * Nested navigation graphs of this navigation graph.
     */
    val nestedNavGraphs: List<NavGraphSpec> get() = emptyList()
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
