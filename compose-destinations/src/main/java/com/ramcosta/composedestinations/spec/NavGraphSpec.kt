package com.ramcosta.composedestinations.spec

/**
 * Defines a navigation graph.
 */
interface NavGraphSpec: Direction, Route {

    /**
     * Route string for this navigation graph which serves as
     * its [Route] identifier and its [Direction].
     */
    override val route: String

    /**
     * Start [Route] of this navigation graph.
     */
    val startRoute: Route

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
