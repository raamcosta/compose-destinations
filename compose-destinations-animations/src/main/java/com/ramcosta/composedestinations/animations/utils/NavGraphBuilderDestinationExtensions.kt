package com.ramcosta.composedestinations.animations.utils

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle

/**
 * Like [com.google.accompanist.navigation.animation.composable] but accepts
 * a [DestinationSpec] to get the route, arguments and deep links.
 *
 * The [content] lambda will receive the navigation arguments class ([T]).
 *
 * Useful if you opt to use [com.google.accompanist.navigation.animation.AnimatedNavHost] instead of
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
@ExperimentalAnimationApi
fun <T> NavGraphBuilder.animatedComposable(
    destination: DestinationSpec<T>,
    content: @Composable AnimatedVisibilityScope.(T, NavBackStackEntry) -> Unit
) {
    when (val style = destination.style) {
        is DestinationStyle.Default -> {
            composable(
                route = destination.route,
                arguments = destination.arguments,
                deepLinks = destination.deepLinks
            ) {
                content(remember { destination.argsFrom(it) }, it)
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
                content(remember { destination.argsFrom(it) }, it)
            }
        }

        is DestinationStyle.BottomSheet -> {
            throw RuntimeException("You need to use `bottomSheetComposable` for Bottom Sheet destinations!")
        }

        is DestinationStyle.Dialog -> {
            throw RuntimeException("You need to use `dialogComposable` for Dialog destinations!")
        }
    }
}

/**
 * Like [com.google.accompanist.navigation.animation.composable] but accepts
 * a [DestinationSpec] to get the route, arguments and deep links.
 *
 * Used for destinations with no navigation arguments.
 *
 * Useful if you opt to use [com.google.accompanist.navigation.animation.AnimatedNavHost] instead of
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
@ExperimentalAnimationApi
fun NavGraphBuilder.animatedComposable(
    destination: DestinationSpec<Unit>,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    when (val style = destination.style) {
        is DestinationStyle.Default -> {
            composable(
                route = destination.route,
                arguments = destination.arguments,
                deepLinks = destination.deepLinks
            ) {
                content(it)
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
                content(it)
            }
        }
        is DestinationStyle.BottomSheet -> {
            throw RuntimeException("You need to use `bottomSheetComposable` for Bottom Sheet destinations!")
        }
        is DestinationStyle.Dialog -> {
            throw RuntimeException("You need to use `dialogComposable` for Dialog destinations!")
        }
    }
}

/**
 * Like [com.google.accompanist.navigation.material.bottomSheet] but accepts
 * a [DestinationSpec] to get the route, arguments and deep links.
 *
 * The [content] lambda will receive the navigation arguments class ([T]).
 *
 * Useful if you opt to use [com.google.accompanist.navigation.animation.AnimatedNavHost] instead of
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
@ExperimentalMaterialNavigationApi
fun <T> NavGraphBuilder.bottomSheetComposable(
    destination: DestinationSpec<T>,
    content: @Composable ColumnScope.(T, NavBackStackEntry) -> Unit
) {
    when (destination.style) {
        is DestinationStyle.BottomSheet -> {
            bottomSheet(
                destination.route,
                destination.arguments,
                destination.deepLinks,
            ) {
                content(remember { destination.argsFrom(it) }, it)
            }
        }
        is DestinationStyle.Dialog -> {
            throw RuntimeException("You need to use `dialogComposable` for Dialog destinations!")
        }
        else -> {
            throw RuntimeException("You need to use `animatedComposable` for Animated or Default styled destinations!")
        }
    }
}

/**
 * Like [com.google.accompanist.navigation.material.bottomSheet] but accepts
 * a [DestinationSpec] to get the route, arguments and deep links.
 *
 * Used for destinations with no navigation arguments.
 *
 * Useful if you opt to use [com.google.accompanist.navigation.animation.AnimatedNavHost] instead of
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
@ExperimentalMaterialNavigationApi
fun NavGraphBuilder.bottomSheetComposable(
    destination: DestinationSpec<Unit>,
    content: @Composable ColumnScope.(NavBackStackEntry) -> Unit
) {
    when (destination.style) {
        is DestinationStyle.BottomSheet -> {
            bottomSheet(
                destination.route,
                destination.arguments,
                destination.deepLinks,
            ) {
                content(it)
            }
        }
        is DestinationStyle.Dialog -> {
            throw RuntimeException("You need to use `dialogComposable` for Dialog destinations!")
        }
        else -> {
            throw RuntimeException("You need to use `animatedComposable` for Animated or Default styled destinations!")
        }
    }
}