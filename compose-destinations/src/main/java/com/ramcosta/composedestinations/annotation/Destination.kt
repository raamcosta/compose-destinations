package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.spec.DestinationStyle
import kotlin.reflect.KClass

/**
 * Marks a `Composable` function as a navigation graph destination.
 * A `Destination` will be generated for each of these which will include
 * the full route, the nav arguments and the `Composable` function which
 * will call the annotated one, once the destination gets navigated to.
 *
 * A global `NavGraphs` object with all these destinations and their nav graphs
 * is generated.
 * `DestinationsNavHost` will also be generated. It is a `NavHost` wrapper
 * which will include these destinations.
 *
 * @property route main route of this destination (by default, the name of the Composable function)
 * @property start `true` if this destination is the start destination of the navigation graph
 * @property navGraph route of the navigation graph this destination is a part of.
 * `"root"` is used by default. If this destination should be part of a nested nav graph, then
 * pass the nav graph's route.
 * @property deepLinks array of [DeepLink] which can be used to navigate to this destination
 * @property style class of a [DestinationStyle] subclass which is used to define the style of this destination
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