package com.ramcosta.composedestinations.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DestinationsNavController
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.NavHostGraphSpec
import com.ramcosta.composedestinations.spec.Route
import com.ramcosta.composedestinations.spec.RouteOrDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform


/**
 * Creates a [DestinationsNavigator] for the [NavController] which has Compose Destinations friendly
 * versions of some [NavController] APIs.
 */
fun NavController.toDestinationsNavigator(): DestinationsNavigator {
    return DestinationsNavController(this)
}

/**
 * Returns a [DestinationsNavigator] for the [NavController] which has Compose Destinations friendly
 * versions of some [NavController] APIs.
 */
@Composable
fun NavController.rememberDestinationsNavigator(): DestinationsNavigator {
    return remember(this) { toDestinationsNavigator() }
}

/**
 * The top level navigation graph associated with this [NavController].
 * Can only be called after [com.ramcosta.composedestinations.DestinationsNavHost].
 */
val NavController.navGraph: NavHostGraphSpec
    get() {
        return NavGraphRegistry[this]?.topLevelNavGraph(this)
            ?: error("Cannot call rootNavGraph before DestinationsNavHost!")
    }

/**
 * Finds the [DestinationSpec] correspondent to this [NavBackStackEntry].
 * Some [NavBackStackEntry] are not [DestinationSpec], but are [NavGraphSpec] instead.
 * If you want a method that works for both, use [route] extension function instead.
 *
 * Use this ONLY if you're sure your [NavBackStackEntry] corresponds to a [DestinationSpec],
 * for example when converting from "current NavBackStackEntry", since a [NavGraphSpec] is never
 * the "current destination" shown on screen.
 */
fun NavBackStackEntry.destination(): DestinationSpec {
    return when (val route = route()) {
        is DestinationSpec -> route
        is NavGraphSpec -> error(
            "Cannot call `destination()` for a NavBackStackEntry which corresponds to a nav graph, use `route()` instead!"
        )
    }
}

/**
 * Finds the [Route] (so either a [DestinationSpec] or a [NavGraphSpec])
 * correspondent to this [NavBackStackEntry].
 */
fun NavBackStackEntry.route(): Route {
    val registry = NavGraphRegistry[this]
        ?: error("Cannot call NavBackStackEntry.route() before DestinationsNavHost!")

    val navGraph = registry.navGraph(this)
    if (navGraph != null) {
        return navGraph
    }

    // If it's not a nav graph, then it must have a parent
    val parentNavGraph = registry.parentNavGraph(this)!!
    return destination.route?.let { parentNavGraph.findDestination(it) }
        ?: parentNavGraph.startDestination
}

/**
 * Finds the [NavGraphSpec] that this [NavBackStackEntry] belongs to.
 * If [NavBackStackEntry] corresponds to the top level nav graph (i.e, there is no parent),
 * then this returns the top level [NavGraphSpec].
 */
fun NavBackStackEntry.navGraph(): NavGraphSpec {
    val registry = NavGraphRegistry[this]
        ?: error("Cannot call NavBackStackEntry.parentNavGraph() before DestinationsNavHost!")

    return registry.parentNavGraph(this) ?: route() as NavGraphSpec
}

/**
 * Emits the currently active [DestinationSpec] whenever it changes. If
 * there is no active [DestinationSpec], no item will be emitted.
 */
val NavController.currentDestinationFlow: Flow<DestinationSpec>
    get() = currentBackStackEntryFlow.transform { navStackEntry ->
        when (val route = navStackEntry.route()) {
            is DestinationSpec -> emit(route)
            is NavGraphSpec -> Unit
        }
    }

/**
 * Gets the current [DestinationSpec] as a [State].
 */
@Composable
fun NavController.currentDestinationAsState(): State<DestinationSpec?> {
    return currentDestinationFlow.collectAsState(initial = null)
}

/**
 * Checks if a given [RouteOrDirection] (which is either [com.ramcosta.composedestinations.spec.NavGraphSpec] or [Direction]
 * or [com.ramcosta.composedestinations.spec.DestinationSpec]) is currently somewhere in the back stack.
 */
fun NavController.isRouteOnBackStack(route: RouteOrDirection): Boolean {
    return toDestinationsNavigator().getBackStackEntry(route) != null
}

/**
 * Same as [isRouteOnBackStack] but provides a [State] which you can use to make sure
 * your Composables get recomposed when this changes.
 */
@Composable
fun NavController.isRouteOnBackStackAsState(route: RouteOrDirection): State<Boolean> {
    val mappedFlow = remember(currentBackStackEntryFlow) {
        currentBackStackEntryFlow.map { isRouteOnBackStack(route) }
    }
    return mappedFlow.collectAsState(initial = isRouteOnBackStack(route))
}
