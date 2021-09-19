package com.ramcosta.composedestinations.annotation

/**
 * Marks a `Composable` function as a navigation graph destination.
 * A `DestinationSpec` will be generated for each of these which will include
 * the full route, the nav arguments and the `Composable` function which
 * will call the annotated one, once the destination gets navigated to.
 *
 * A global `Destinations` object with all the destinations as well as a `NavHost` and
 * a `Scaffold` wrapper which will include these destinations in the navigation
 * graph, will also be generated.
 *
 * @param route main route of this destination (with no arguments)
 * @param start `true` if this destination is the start destination of the navigation graph
 * @param navGraph route of the navigation graph this destination is a part of.
 * `"root"` is used by default. If this destination is part of a nested nav graph, then this should
 * be used
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Destination(
    val route: String,
    val start: Boolean = false,
    val navGraph: String = "root",
    val deepLinks: Array<DeepLink> = []
)