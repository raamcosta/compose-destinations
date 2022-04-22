package com.ramcosta.composedestinations.dynamic

import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.NavGraphSpec

/**
 * [DestinationSpec] created by [routedIn] methods.
 * This is useful if you have one annotated Composable used on multiple [NavGraphSpec]s.
 *
 * For all intents and purposes, it will be an entirely different destination, except
 * that it will have the same navigation arguments and call the same Composable, when
 * it gets navigated to.
 *
 * To navigate to a [DynamicDestinationSpec] you need to use [within],
 * for example:
 * ```
 * navigator.navigate(YourScreenDestination(yourNavArgs) within YourNavGraph)
 * ```
 * Note that the `YourNavGraph` in the above example MUST match the one used when
 * [NavGraphSpec] is defined with [routedIn], otherwise the resulting [DynamicDestinationSpec] will
 * not exist in the navigation graph and it will crash at runtime.
 */
interface DynamicDestinationSpec<T> : DestinationSpec<T> {
    val delegate: DestinationSpec<T>
}

/**
 * Creates a new [DynamicDestinationSpec] routing the [DestinationSpec] receiver in the [navGraph]
 * passed in.
 * Use this when building your [NavGraphSpec] objects. For example:
 *
 * ```
 * val yourNavGraph = object: NavGraphSpec {
 *     override val route = "your_nav_graph"
 *     override val startRoute = YourScreenDestination routedIn this // <-- EXAMPLE HERE
 *     override val destinationsByRoute = listOf(
 *         YourScreenDestination,
 *         AnotherScreenDestination,
 *     ).routedIn(this).associateBy { it.route }
 * }
 * ```
 */
infix fun <T> DestinationSpec<T>.routedIn(navGraph: NavGraphSpec): DestinationSpec<T> {
    return object: DynamicDestinationSpec<T>, DestinationSpec<T> by this {
        override val baseRoute = "${navGraph.route}/${this@routedIn.baseRoute}"

        override val route = "${navGraph.route}/${this@routedIn.route}"

        override val delegate = this@routedIn
    }
}

/**
 * Used to navigate to a [DestinationSpec] that was previously created with [routedIn]
 * with the same [navGraph].
 * For example a destination created by:
 * ```
 * YourScreenDestination routedIn YourNavGraph
 * ```
 *
 * Could be navigated to like this:
 * ```
 * navigator.navigate(YourScreenDestination(yourNavArgs) within YourNavGraph)
 * ```
 */
infix fun Direction.within(navGraph: NavGraphSpec): Direction {
    return object: Direction by this@within {
        override val route = "${navGraph.route}/${this@within.route}"
    }
}

/**
 * Same as [routedIn] but for a whole List of [DestinationSpec]
 */
fun List<DestinationSpec<*>>.routedIn(navGraphSpec: NavGraphSpec): List<DestinationSpec<*>> {
    return map { it routedIn navGraphSpec }
}
