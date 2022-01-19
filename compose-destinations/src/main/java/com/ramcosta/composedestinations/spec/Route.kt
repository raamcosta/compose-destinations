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

    val route: String
}
