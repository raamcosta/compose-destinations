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
 * Internal details, public only for inline functions.
 *
 *  @see [DestinationScope].
 */
open class DestinationScopeImpl<T>(
    override val destination: DestinationSpec<T>,
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
