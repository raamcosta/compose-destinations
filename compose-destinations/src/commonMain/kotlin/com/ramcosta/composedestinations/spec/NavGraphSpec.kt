package com.ramcosta.composedestinations.spec

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry

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

    /**
     * Like [argsFrom] but throws an exception if the arguments are not present.
     *
     * In Navigation graphs, the arguments might be or might not be there depending on if
     * you have navigated directly to a destination, in which case arguments may not be present, or
     * if you have navigated to the navigation graph, in which case the arguments will be present.
     */
    fun requireGraphArgs(bundle: Bundle?) : NAV_ARGS {
        return argsFrom(bundle) ?: throwMissingNavArgsException()
    }

    /**
     * Like [argsFrom] but throws an exception if the arguments are not present.
     *
     * In Navigation graphs, the arguments might be or might not be there depending on if
     * you have navigated directly to a destination, in which case arguments may not be present, or
     * if you have navigated to the navigation graph, in which case the arguments will be present.
     */
    fun requireGraphArgs(savedStateHandle: SavedStateHandle) : NAV_ARGS {
        return argsFrom(savedStateHandle) ?: throwMissingNavArgsException()
    }

    /**
     * Like [argsFrom] but throws an exception if the arguments are not present.
     *
     * In Navigation graphs, the arguments might be or might not be there depending on if
     * you have navigated directly to a destination, in which case arguments may not be present, or
     * if you have navigated to the navigation graph, in which case the arguments will be present.
     */
    fun requireGraphArgs(navBackStackEntry: NavBackStackEntry) : NAV_ARGS {
        return argsFrom(navBackStackEntry.arguments) ?: throwMissingNavArgsException()
    }

    private fun throwMissingNavArgsException(): Nothing {
        error("$this navigation arguments were not present. Make sure you navigated to $this nav graph, and not to one of its destinations!")
    }
}
