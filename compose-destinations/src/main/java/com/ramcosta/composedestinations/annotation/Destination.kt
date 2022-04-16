package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.spec.DestinationStyle
import kotlin.reflect.KClass

/**
 * Marks a `Composable` function as a navigation graph destination.
 * A `Destination` will be generated for each of these which will include
 * the full route, the nav arguments and the `Composable` function which
 * will call the annotated one, when the destination gets navigated to.
 *
 * @param route main route of this destination (by default, the name of the Composable function)
 * @param start (Deprecated: check [NavGraph]) `true` if this destination is the start destination of the navigation graph
 * @param navGraph (Deprecated: check [NavGraph]) route of the navigation graph this destination is a part of.
 * `"root"` is used by default. If this destination should be part of a nested nav graph, then
 * pass that nav graph's route.
 * @param navArgsDelegate class with a primary constructor where all navigation arguments are
 * to be defined. Useful when the arguments are not needed in this Composable or to simplify
 * the Composable function signature when it has a lot of navigation arguments (which should be rare).
 * The generated `Destination` class has `argsFrom` methods that accept a `NavBackStackEntry`
 * or a `SavedStateHandle` (useful inside a ViewModel) and return an instance of this class.
 * @param deepLinks array of [DeepLink] which can be used to navigate to this destination
 * @param style class of a [DestinationStyle] subclass which is used to define the destination style:
 * its transitions animations OR if it is dialog destination OR a bottom sheet destination. For animations
 * and bottom sheet, you need to use the "io.github.raamcosta.compose-destinations:animations-core"
 * dependency instead of the normal "io.github.raamcosta.compose-destinations:core".
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Destination(
    val route: String = COMPOSABLE_NAME,
    @Deprecated("Will be removed! Create annotation classes annotated with @NavGraph instead.") val start: Boolean = false,
    @Deprecated("Will be removed! Create annotation classes annotated with @NavGraph instead.") val navGraph: String = ROOT_NAV_GRAPH_ROUTE,
    val navArgsDelegate: KClass<*> = Nothing::class,
    val deepLinks: Array<DeepLink> = [],
    val style: KClass<out DestinationStyle> = DestinationStyle.Default::class
) {
    companion object {
        const val COMPOSABLE_NAME = "@ramcosta.destinations.composable-name-route@"
        const val ROOT_NAV_GRAPH_ROUTE = "root"
    }
}
