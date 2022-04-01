package com.ramcosta.composedestinations.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.ramcosta.composedestinations.scope.NavGraphBuilderDestinationScopeImpl
import com.ramcosta.composedestinations.scope.NavGraphBuilderDestinationScope
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle

/**
 * Like [androidx.navigation.compose.composable] but accepts
 * a [DestinationSpec] to get the route, arguments and deep links.
 *
 * The [content] lambda will receive the navigation arguments class ([T]).
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
fun <T> NavGraphBuilder.composable(
    destination: DestinationSpec<T>,
    content: @Composable NavGraphBuilderDestinationScope<T>.() -> Unit
) = with(destination) {
    composable(
        route,
        arguments,
        deepLinks
    ) {
        val scope = remember {
            NavGraphBuilderDestinationScopeImpl.Default(
                destination,
                it,
            )
        }

        scope.content()
    }
}

/**
 * Like [androidx.navigation.compose.dialog] but accepts
 * a [DestinationSpec] to get the route, arguments and deep links.
 *
 * The [content] lambda will receive the navigation arguments class ([T]).
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
fun <T> NavGraphBuilder.dialogComposable(
    destination: DestinationSpec<T>,
    content: @Composable NavGraphBuilderDestinationScope<T>.() -> Unit
) = with(destination) {
    val style = destination.style
    if (style !is DestinationStyle.Dialog) {
        throw RuntimeException("Need to use `composable` to add non dialog destinations")
    }

    dialog(
        route,
        arguments,
        deepLinks,
        style.properties
    ) {
        val scope = remember {
            NavGraphBuilderDestinationScopeImpl.Default(
                destination,
                it,
            )
        }

        scope.content()
    }
}
