package com.ramcosta.composedestinations.spec

import com.ramcosta.composedestinations.animations.defaults.NavHostAnimatedDestinationStyle

/**
 * Defines a navigation graph.
 */
interface NavGraphSpec : Direction, Route {

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

    /**
     * Default enter/exit transition animations for destinations that belong
     * to this navigation graph.
     * If this is a nested graph, then if not specified (null) then the parent ones
     * will apply.
     *
     * If the [DestinationSpec.style] itself defines another set of animations, then
     * those ones will apply instead of this.
     */
    val defaultTransitions: DestinationStyle.Animated?
}

/**
 * Like [NavGraphSpec] but used specifically for top level navigation graphs (i.e they
 * have no parent graph) that are meant to pass to [com.ramcosta.composedestinations.DestinationsNavHost] call.
 */
interface NavHostGraphSpec : NavGraphSpec {

    /**
     * Like [NavGraphSpec.defaultTransitions] but not nullable since NavHost level
     * graphs must have animations defined (even if they are defined as "No animations")
     */
    override val defaultTransitions: NavHostAnimatedDestinationStyle
}
