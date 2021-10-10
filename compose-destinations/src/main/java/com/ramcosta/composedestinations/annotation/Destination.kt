package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.spec.DestinationStyle
import kotlin.reflect.KClass

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
 * @property route main route of this destination (with no arguments)
 * @property start `true` if this destination is the start destination of the navigation graph
 * @property navGraph route of the navigation graph this destination is a part of.
 * `"root"` is used by default. If this destination is part of a nested nav graph, then this should
 * be used
 * @property deepLinks array of [DeepLink] which can be used to navigate to this destination
 * @property style TODO
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Destination(
    val route: String = COMPOSABLE_NAME,
    val start: Boolean = false,
    val navGraph: String = ROOT_NAV_GRAPH_ROUTE,
    val deepLinks: Array<DeepLink> = [],
    val style: KClass<out DestinationStyle> = DestinationStyle.Default::class
) {
    companion object {
        const val COMPOSABLE_NAME = "@composable-name-route"
        const val ROOT_NAV_GRAPH_ROUTE = "root"
    }
}