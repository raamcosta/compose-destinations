package com.ramcosta.composedestinations.utils

import androidx.navigation.NavGraphBuilder
import com.ramcosta.composedestinations.spec.ActivityDestinationSpec
import com.ramcosta.composedestinations.spec.ActivityDestinationStyle

/**
 * Like [androidx.navigation.activity] but accepts
 * a [ActivityDestinationSpec] to get the route, arguments and deep links.
 *
 * Useful if you opt to use [androidx.navigation.compose.NavHost] instead of
 * [com.ramcosta.composedestinations.DestinationsNavHost].
 * This way, you can build the navigation graph in the "vanilla compose navigation" way.
 * If you do this, you should also disable the `NavGraphs` generation
 * in build.gradle:
 * ```
 * ksp {
 *     arg("compose-destinations.generateNavGraphs", "false")
 * }
 * ```
 */
fun <T> NavGraphBuilder.activity(
    destination: ActivityDestinationSpec<T>,
) = with(destination.style as ActivityDestinationStyle) {
    addComposable(destination)
}