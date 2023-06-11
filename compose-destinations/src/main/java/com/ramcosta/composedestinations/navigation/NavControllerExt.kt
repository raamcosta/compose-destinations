package com.ramcosta.composedestinations.navigation

import androidx.annotation.MainThread
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
 * Like [NavOptionsBuilder.popUpTo] but uses [Route] instead of a String route, making it
 * clear what kind of route we need to use and making it more "Compose Destinations friendly".
 */
fun NavOptionsBuilder.popUpTo(route: Route, popUpToBuilder: PopUpToBuilder.() -> Unit = {}) {
    popUpTo(route.route, popUpToBuilder)
}

/**
 * Like [NavController.popBackStack] but uses [Route] instead of a String route, making it clear
 * what kind of route we need to use and making it more "Compose Destinations friendly".
 */
@MainThread
fun NavController.popBackStack(
    route: Route,
    inclusive: Boolean,
    saveState: Boolean = false
): Boolean = popBackStack(route.route, inclusive, saveState)

/**
 * Like [NavController.clearBackStack] but uses [Route] instead of a String route, making it clear
 * what kind of route we need to use and making it more "Compose Destinations friendly".
 */
@MainThread
fun NavController.clearBackStack(route: Route): Boolean = clearBackStack(route.route)
