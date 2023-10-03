package com.ramcosta.composedestinations.spec

typealias NavGraphSpec = TypedNavGraphSpec<*, *>

/**
 * Defines a navigation graph.
 */
interface TypedNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS>: TypedRoute<NAV_ARGS> {

    /**
     * Start [Route] of this navigation graph.
     */
    val startRoute: TypedRoute<START_ROUTE_NAV_ARGS>

    /**
     * All destinations which belong to this navigation graph
     */
    val destinations: List<DestinationSpec>

    /**
     * Nested navigation graphs of this navigation graph.
     */
    val nestedNavGraphs: List<NavGraphSpec> get() = emptyList()

    /**
     * Default enter/exit transition animations for destinations that belong
     * to this navigation graph.
     * If not specified (null), then the parent ones will apply.
     *
     * If the [TypedDestinationSpec.style] itself defines another set of animations, then
     * those ones will apply instead of this.
     */
    val defaultTransitions: DestinationStyle.Animated?
}
