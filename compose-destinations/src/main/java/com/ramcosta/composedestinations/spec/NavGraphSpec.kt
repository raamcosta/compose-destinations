package com.ramcosta.composedestinations.spec

typealias NavGraphSpec = TypedNavGraphSpec<*>

/**
 * Defines a navigation graph.
 */
interface TypedNavGraphSpec<T>: TypedRoute<T> {

    /**
     * Start [Route] of this navigation graph.
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
     * Default enter/exit transition animations for destinations that belong
     * to this navigation graph.
     * If this is a nested graph, then if not specified (null) the parent ones
     * will apply.
     *
     * If the [TypedDestinationSpec.style] itself defines another set of animations, then
     * those ones will apply instead of this.
     */
    val defaultTransitions: DestinationStyle.Animated?
}
