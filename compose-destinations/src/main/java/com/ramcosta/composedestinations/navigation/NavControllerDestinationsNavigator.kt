package com.ramcosta.composedestinations.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder

/**
 * Implementation of [DestinationsNavigator] that uses
 * a [NavController] to navigate.
 */
class NavControllerDestinationsNavigator(
    private val navController: NavController
) : DestinationsNavigator {

    override fun navigate(routed: Routed, builder: NavOptionsBuilder.() -> Unit) {
        navController.navigate(routed.route, builder)
    }

    override fun navigate(route: String, builder: NavOptionsBuilder.() -> Unit) {
        navController.navigate(route, builder)
    }

    override fun navigateUp() {
        navController.navigateUp()
    }
}