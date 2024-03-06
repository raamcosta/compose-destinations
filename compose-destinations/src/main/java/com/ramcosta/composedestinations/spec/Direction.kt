package com.ramcosta.composedestinations.spec

/**
 * Interface for all classes that contain a route
 * that is ready to be used in navigation.
 *
 * Generated [TypedDestinationSpec] and [TypedNavGraphSpec] are [Direction]
 * if they don't have any navigation argument. If they do, you can
 * call the invoke function passing the arguments to get a [Direction].
 */
interface Direction: RouteOrDirection {

    /**
     * Route with nav arguments filled in.
     */
    override val route: String
}

/**
 * Creates a [Direction] for given [route].
 */
fun Direction(route: String): Direction {
    return DirectionImpl(route)
}

private data class DirectionImpl(override val route: String) : Direction