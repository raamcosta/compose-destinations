package com.ramcosta.composedestinations.manualcomposablecalls

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DestinationsNavController
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationSpec

/**
 * Scope given to the calls related to the [ManualComposableCallsBuilder].
 */
interface DestinationScope<T> {
    /**
     * [NavBackStackEntry] of the current destination
     */
    val navBackStackEntry: NavBackStackEntry

    /**
     * [NavController] related to the NavHost
     */
    val navController: NavController

    /**
     * [DestinationsNavigator] useful to navigate from this destination
     */
    val destinationsNavigator: DestinationsNavigator

    /**
     * Class holding the navigation arguments passed to this destination
     * or [Unit] if the destination has no arguments
     */
    val navArgs: T
}

/**
 * Like [DestinationScope] but also [AnimatedVisibilityScope] so that
 * if you're using the "animations-core" you can use this Scope as a receiver
 * of your Animated Composable
 */
@ExperimentalAnimationApi
interface AnimatedDestinationScope<T> : DestinationScope<T>, AnimatedVisibilityScope

/**
 * Like [DestinationScope] but also [ColumnScope] so that
 * if you're using the "animations-core" you can use this Scope as a receiver
 * of your Bottom Sheet styled Composable
 */
interface BottomSheetDestinationScope<T> : DestinationScope<T>, ColumnScope

//region internal implementations

open class DestinationScopeImpl<T>(
    private val destination: DestinationSpec<T>,
    override val navBackStackEntry: NavBackStackEntry,
    override val navController: NavController,
): DestinationScope<T> {

    override val navArgs: T by lazy(LazyThreadSafetyMode.NONE) {
        destination.argsFrom(navBackStackEntry)
    }

    override val destinationsNavigator: DestinationsNavigator
        get() = DestinationsNavController(navController, navBackStackEntry)
}

@ExperimentalAnimationApi
class AnimatedDestinationScopeImpl<T>(
    destination: DestinationSpec<T>,
    navBackStackEntry: NavBackStackEntry,
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
) : DestinationScopeImpl<T>(
    destination,
    navBackStackEntry,
    navController,
), AnimatedDestinationScope<T>, AnimatedVisibilityScope by animatedVisibilityScope

class BottomSheetDestinationScopeImpl<T>(
    destination: DestinationSpec<T>,
    navBackStackEntry: NavBackStackEntry,
    navController: NavController,
    columnScope: ColumnScope,
) : DestinationScopeImpl<T>(
    destination,
    navBackStackEntry,
    navController,
), BottomSheetDestinationScope<T>, ColumnScope by columnScope

//endregion