package com.ramcosta.composedestinations.navigation

import androidx.navigation.NavOptionsBuilder
import androidx.navigation.PopUpToBuilder
import com.ramcosta.composedestinations.spec.RouteOrDirection

/**
 * Like [NavOptionsBuilder] but has Compose Destinations friendly
 * version of its APIs.
 */
class DestinationsNavOptionsBuilder(
    private val jetpackBuilder: NavOptionsBuilder
) {

    /**
     * @see [NavOptionsBuilder.launchSingleTop]
     */
    var launchSingleTop
        get() = jetpackBuilder.launchSingleTop
        set(value) {
            jetpackBuilder.launchSingleTop = value
        }

    /**
     * @see [NavOptionsBuilder.restoreState]
     */
    var restoreState
        get() = jetpackBuilder.restoreState
        set(value) {
            jetpackBuilder.restoreState = value
        }

    /**
     * @see [NavOptionsBuilder.popUpToRoute]
     */
    val popUpToRoute: String?
        get() = jetpackBuilder.popUpToRoute

    /**
     * Like [NavOptionsBuilder.popUpTo] but accepting a [com.ramcosta.composedestinations.spec.Route]
     * or [com.ramcosta.composedestinations.spec.Direction] to pop up to.
     *
     * @see [NavOptionsBuilder.popUpTo]
     */
    fun popUpTo(route: RouteOrDirection, popUpToBuilder: PopUpToBuilder.() -> Unit = {}) {
        jetpackBuilder.popUpTo(route.route, popUpToBuilder)
    }
}