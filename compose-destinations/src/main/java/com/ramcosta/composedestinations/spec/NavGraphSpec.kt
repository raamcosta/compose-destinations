package com.ramcosta.composedestinations.spec

/**
 * Defines a navigation graph.
 */
interface NavGraphSpec: Direction, RouteIdentified {

    /**
     * Route for this navigation graph which serves as
     * its identifier.
     * It can also be used to navigate to it.
     */
    override val route: String

    /**
     * Start destination of this navigation graph.
     */
    val startDestination: DestinationSpec<*>

    /**
     * All destinations which belong to this navigation graph
     * by their route
     */
    val destinationsByRoute: Map<String, DestinationSpec<*>>

    /**
     * Nested navigation graphs of this navigation graph.
     */
    val nestedNavGraphs: List<NavGraphSpec> get() = emptyList()
}
