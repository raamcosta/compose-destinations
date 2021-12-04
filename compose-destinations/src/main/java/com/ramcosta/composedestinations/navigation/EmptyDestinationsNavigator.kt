package com.ramcosta.composedestinations.navigation

import androidx.navigation.NavOptionsBuilder

/**
 * Empty implementation of [DestinationsNavigator]
 * Useful for tests and Composable previews.
 */
object EmptyDestinationsNavigator : DestinationsNavigator {

    override fun navigate(
        route: String,
        onlyIfResumed: Boolean,
        builder: NavOptionsBuilder.() -> Unit,
    ) = Unit

    override fun navigateUp() = false

    override fun popBackStack() = false

    override fun popBackStack(
        route: String,
        inclusive: Boolean,
        saveState: Boolean,
    ) = false

    override fun clearBackStack(route: String) = false
}