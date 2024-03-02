package com.ramcosta.composedestinations.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.PopUpToBuilder
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.Route

/**
 * Like [NavController.navigate], but uses [Direction] instead of a String route.
 */
fun NavController.navigate(
    direction: Direction,
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(direction.route, navOptionsBuilder)
}

/**
 * Like [NavOptionsBuilder.popUpTo] but uses [Route] instead of a String route,
 * it more "Compose Destinations friendly".
 */
fun NavOptionsBuilder.popUpTo(route: Route, popUpToBuilder: PopUpToBuilder.() -> Unit = {}) {
    popUpTo(route.route, popUpToBuilder)
}

/**
 * Like [popUpTo] but takes in a [Direction].
 *
 * If there are multiple entries in the back stack for the same Destination
 * or NavGraph, then specifying the arguments (which is what [Direction] allows)
 * means a specific entry for the given arguments will be targeted.
 */
fun NavOptionsBuilder.popUpTo(direction: Direction, popUpToBuilder: PopUpToBuilder.() -> Unit = {}) {
    popUpTo(direction.route, popUpToBuilder)
}

/**
 * Like [NavController.popBackStack] but uses [Route] instead of a String route, making
 * it more "Compose Destinations friendly".
 */
@MainThread
fun NavController.popBackStack(
    route: Route,
    inclusive: Boolean,
    saveState: Boolean = false
): Boolean = popBackStack(route.route, inclusive, saveState)

/**
 * Like [popBackStack] but takes in a [Direction].
 *
 * If there are multiple entries in the back stack for the same Destination
 * or NavGraph, then specifying the arguments (which is what [Direction] allows)
 * means a specific entry for the given arguments will be targeted.
 */
@MainThread
fun NavController.popBackStack(
    direction: Direction,
    inclusive: Boolean,
    saveState: Boolean = false
): Boolean = popBackStack(direction.route, inclusive, saveState)

/**
 * Like [NavController.clearBackStack] but uses [Route] instead of a String route, making
 * it more "Compose Destinations friendly".
 */
@MainThread
fun NavController.clearBackStack(route: Route): Boolean = clearBackStack(route.route)

/**
 * Like [NavController.clearBackStack] but takes in a [Direction].
 * If there are multiple entries in the back stack for the same Destination
 * or NavGraph, then specifying the arguments (which is what [Direction] allows)
 * means a specific entry for the given arguments will be targeted.
 */
@MainThread
fun NavController.clearBackStack(direction: Direction): Boolean = clearBackStack(direction.route)

/**
 * Like [androidx.navigation.NavController.getBackStackEntry] but uses a
 * [Route] instead of a route string.
 */
fun NavController.getBackStackEntry(
    route: Route
): NavBackStackEntry {
    return getBackStackEntry(route.route)
}

/**
 * Like [getBackStackEntry] but takes in a [Direction].
 *
 * If there are multiple entries in the back stack for the same Destination
 * or NavGraph, then specifying the arguments (which is what [Direction] allows)
 * means a specific entry for the given arguments will be targeted.
 */
fun NavController.getBackStackEntry(
    direction: Direction
): NavBackStackEntry {
    return getBackStackEntry(direction.route)
}
