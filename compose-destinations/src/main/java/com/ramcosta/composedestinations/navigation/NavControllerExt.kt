package com.ramcosta.composedestinations.navigation

import androidx.annotation.MainThread
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.PopUpToBuilder
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.Route
import com.ramcosta.composedestinations.spec.RouteOrDirection

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
 * Like [NavOptionsBuilder.popUpTo] but uses [Route] or [Direction] instead of a String route,
 * it more "Compose Destinations friendly".
 */
fun NavOptionsBuilder.popUpTo(
    route: RouteOrDirection,
    popUpToBuilder: PopUpToBuilder.() -> Unit = {}
) {
    popUpTo(route.route, popUpToBuilder)
}

/**
 * Like [NavController.popBackStack] but [Route] or [Direction] instead of a String route, making
 * it more "Compose Destinations friendly".
 */
@MainThread
fun NavController.popBackStack(
    route: RouteOrDirection,
    inclusive: Boolean,
    saveState: Boolean = false
): Boolean = popBackStack(route.route, inclusive, saveState)

/**
 * Like [NavController.clearBackStack] but [Route] or [Direction] instead of a String route, making
 * it more "Compose Destinations friendly".
 */
@MainThread
fun NavController.clearBackStack(route: RouteOrDirection): Boolean = clearBackStack(route.route)

/**
 * Like [androidx.navigation.NavController.getBackStackEntry] but uses a
 * [Route] or [Direction] instead of a route string.
 */
fun NavController.getBackStackEntry(
    route: RouteOrDirection
): NavBackStackEntry {
    return getBackStackEntry(route.route)
}
