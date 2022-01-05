package com.ramcosta.composedestinations.spec

/**
 * Interface for all classes which instances
 * are identified by a route.
 *
 * All [DestinationSpec] and [NavGraphSpec] are
 * [RouteIdentified] because they are registered in a
 * navigation graph with this id.
 *
 * [RouteIdentified] instances are not suited to navigate
 * to unless they're also [Direction].
 */
interface RouteIdentified {

    val route: String
}