package com.ramcosta.composedestinations.spec

/**
 * Interface for all classes which instances
 * are identified by a route.
 *
 * All [DestinationSpec] and [NavGraphSpec] are
 * [Route].
 *
 * [Route] instances are not suited to navigate
 * to unless they're also [Direction].
 */
sealed interface Route {

    /**
     * Full route pattern that will be added to the navigation graph.
     * Navigation arguments are not filled in.
     */
    val route: String

    /**
     * Prefix of the route - basically [route] without argument info.
     */
    val baseRoute: String
}
