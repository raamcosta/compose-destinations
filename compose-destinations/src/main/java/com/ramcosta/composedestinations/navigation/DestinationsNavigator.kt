package com.ramcosta.composedestinations.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Routed

/**
 * Contract for a navigator of [DestinationSpec].
 * It uses components of [NavController] so implementations
 * will need one to actual navigate.
 *
 * It is meant as a dependency inversion wrapper to make
 * composables that depend on it be testable and "preview-able".
 *
 * [NavGraphSpec] and [DestinationSpec] are [Routed]
 */
interface DestinationsNavigator {

    fun navigate(routed: Routed, builder: NavOptionsBuilder.() -> Unit = {})

    fun navigate(route: String, builder: NavOptionsBuilder.() -> Unit = {})

    fun navigateUp()
}

/**
 * Empty implementation of [DestinationsNavigator]
 * Useful for tests and Composable previews.
 */
object EmptyDestinationsNavigator : DestinationsNavigator {

    override fun navigate(routed: Routed, builder: NavOptionsBuilder.() -> Unit) = Unit

    override fun navigate(route: String, builder: NavOptionsBuilder.() -> Unit) = Unit

    override fun navigateUp() = Unit
}

/**
 * Navigates to the [Routed].
 */
fun NavController.navigateTo(
    routed: Routed,
    navOptionsBuilder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(routed.route, navOptionsBuilder)
}

