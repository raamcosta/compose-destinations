@file:OptIn(InternalDestinationsApi::class)
package com.ramcosta.composedestinations.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.ramcosta.composedestinations.annotation.InternalDestinationsApi
import com.ramcosta.composedestinations.scope.AnimatedNavGraphBuilderDestinationScope
import com.ramcosta.composedestinations.scope.AnimatedNavGraphBuilderDestinationScopeImpl
import com.ramcosta.composedestinations.scope.NavGraphBuilderDestinationScope
import com.ramcosta.composedestinations.scope.NavGraphBuilderDestinationScopeImpl
import com.ramcosta.composedestinations.spec.ActivityDestinationSpec
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.TypedDestinationSpec

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
    destination: TypedDestinationSpec<T>,
    content: @Composable AnimatedNavGraphBuilderDestinationScope<T>.() -> Unit
) {
    when (val style = destination.style) {
        is DestinationStyle.Default -> {
            composable(
                route = destination.route,
                arguments = destination.arguments,
                deepLinks = destination.deepLinks
            ) {
                val scope = remember {
                    AnimatedNavGraphBuilderDestinationScopeImpl(
                        destination,
                        it,
                        this
                    )
                }

                scope.content()
            }
        }

        is DestinationStyle.Animated -> with(style) {
            composable(
                route = destination.route,
                arguments = destination.arguments,
                deepLinks = destination.deepLinks,
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { popEnterTransition() },
                popExitTransition = { popExitTransition() }
            ) {
                val scope = remember {
                    AnimatedNavGraphBuilderDestinationScopeImpl(
                        destination,
                        it,
                        this
                    )
                }

                scope.content()
            }
        }

        is DestinationStyle.Dialog -> {
            throw IllegalArgumentException("You need to use `dialogComposable` for Dialog destinations!")
        }

        is DestinationStyle.Activity -> {
            throw IllegalArgumentException("You need to use `activity` for Activity destinations!")
        }
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
    destination: TypedDestinationSpec<T>,
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
        val scope = remember(it) {
            NavGraphBuilderDestinationScopeImpl.Default(
                destination,
                it,
            )
        }

        scope.content()
    }
}

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
) = with(destination.style as DestinationStyle.Activity) {
    addComposable(destination)
}
