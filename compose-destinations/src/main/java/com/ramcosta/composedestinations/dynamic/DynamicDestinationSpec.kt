package com.ramcosta.composedestinations.dynamic

import androidx.navigation.NavDeepLink
import androidx.navigation.NavDeepLinkDslBuilder
import androidx.navigation.navDeepLink
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.TypedDestinationSpec

/**
 * [DestinationSpec] created by [routedIn] / [withDeepLink] methods.
 * These are useful if you have annotated Composable used on multiple [NavGraphSpec]s.
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
interface DynamicDestinationSpec<T> : TypedDestinationSpec<T> {
    val originalDestination: TypedDestinationSpec<T>
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
infix fun <T> TypedDestinationSpec<T>.routedIn(navGraph: NavGraphSpec): TypedDestinationSpec<T> {
    return object : DynamicDestinationSpec<T>, TypedDestinationSpec<T> by this {
        override val baseRoute = "${navGraph.baseRoute}/${this@routedIn.baseRoute}"

        override val route = "${navGraph.baseRoute}/${this@routedIn.route}"

        override val originalDestination = this@routedIn.originalDestination
    }
}

/**
 * Same as [routedIn] but for a whole List of [DestinationSpec]
 */
fun List<DestinationSpec>.routedIn(navGraphSpec: NavGraphSpec): List<DestinationSpec> {
    return map { it routedIn navGraphSpec }
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
    return Direction(route = "${navGraph.route}/${this@within.route}")
}

/**
 * Can be used in conjunction with [routedIn] methods to define deep links specific
 * to one combination of [DestinationSpec]/[NavGraphSpec].
 *
 * Example:
 * ```
 * val yourNavGraph = object: NavGraphSpec {
 *     override val route = "your_nav_graph"
 *     override val startRoute = YourScreenDestination routedIn this
 *     override val destinationsByRoute = listOf(
 *         YourScreenDestination.withDeepLink { uriPattern = "https://myapp.com/yourscreen" },
 *         AnotherScreenDestination,
 *     ).routedIn(this).associateBy { it.route }
 * }
 *
 * val anotherNavGraph = object: NavGraphSpec {
 *     override val route = "another_nav_graph"
 *     override val startRoute = AnotherScreenDestination routedIn this
 *     override val destinationsByRoute = listOf(
 *         YourScreenDestination,
 *         AnotherScreenDestination,
 *     ).routedIn(this).associateBy { it.route }
 * }
 * ```
 *
 * In the above example, "YourScreenDestination" is tied to multiple navigation graphs, and
 * only one has a deep link (the one in "yourNavGraph"). So when that deep link is used, the app
 * will navigate to "YourScreenDestination" that belongs to "yourNavGraph".
 */
fun <T> TypedDestinationSpec<T>.withDeepLink(
    deepLinkBuilder: NavDeepLinkDslBuilder.() -> Unit
): TypedDestinationSpec<T> {
    return object : DynamicDestinationSpec<T>, TypedDestinationSpec<T> by this {
        override val originalDestination = this@withDeepLink.originalDestination

        override val deepLinks = listOf(navDeepLink(deepLinkBuilder))
    }
}

/**
 * Same as [withDeepLink] but can take a list of [NavDeepLink]s instead of a single [NavDeepLinkDslBuilder].
 * If you want to tie the destination to a single deep link, using [withDeepLink] will be more natural and less
 * verbose.
 *
 * Example:
 * ```
 * val yourNavGraph = object: NavGraphSpec {
 *     override val route = "your_nav_graph"
 *     override val startRoute = YourScreenDestination routedIn this
 *     override val destinationsByRoute = listOf(
 *         YourScreenDestination.withDeepLinks(
 *              listOf(
 *                  navDeepLink { uriPattern = "https://myapp.com/yourscreen" },
 *                  navDeepLink { uriPattern = "https://myapp.com/anotheruri" },
 *              )
 *          ),
 *         AnotherScreenDestination,
 *     ).routedIn(this).associateBy { it.route }
 * }
 * ```
 *
 * @see [withDeepLink]
 */
fun <T> TypedDestinationSpec<T>.withDeepLinks(deepLinks: List<NavDeepLink>): TypedDestinationSpec<T> {
    return object : DynamicDestinationSpec<T>, TypedDestinationSpec<T> by this {
        override val originalDestination = this@withDeepLinks.originalDestination

        override val deepLinks = deepLinks
    }
}

@PublishedApi
internal val <T> TypedDestinationSpec<T>.originalDestination
    get(): TypedDestinationSpec<T> =
        if (this is DynamicDestinationSpec<T>) {
            this.originalDestination
        } else {
            this
        }
