package com.ramcosta.composedestinations.animations.scope

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.scope.*
import com.ramcosta.composedestinations.spec.DestinationSpec

@ExperimentalAnimationApi
internal class AnimatedDestinationScopeImpl<T>(
    destination: DestinationSpec<T>,
    navBackStackEntry: NavBackStackEntry,
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
) : DestinationScopeImpl<T>(
    destination,
    navBackStackEntry,
    navController,
), AnimatedDestinationScope<T>, AnimatedVisibilityScope by animatedVisibilityScope

internal class BottomSheetDestinationScopeImpl<T>(
    destination: DestinationSpec<T>,
    navBackStackEntry: NavBackStackEntry,
    navController: NavController,
    columnScope: ColumnScope,
) : DestinationScopeImpl<T>(
    destination,
    navBackStackEntry,
    navController,
), BottomSheetDestinationScope<T>, ColumnScope by columnScope

@ExperimentalAnimationApi
internal class AnimatedNavGraphBuilderDestinationScopeImpl<T>(
    destination: DestinationSpec<T>,
    navBackStackEntry: NavBackStackEntry,
    animatedVisibilityScope: AnimatedVisibilityScope,
) : NavGraphBuilderDestinationScopeImpl<T>(
    destination,
    navBackStackEntry,
), AnimatedNavGraphBuilderDestinationScope<T>, AnimatedVisibilityScope by animatedVisibilityScope

internal class BottomSheetNavGraphBuilderDestinationScopeImpl<T>(
    destination: DestinationSpec<T>,
    navBackStackEntry: NavBackStackEntry,
    columnScope: ColumnScope,
) : NavGraphBuilderDestinationScopeImpl<T>(
    destination,
    navBackStackEntry,
), BottomSheetNavGraphBuilderDestinationScope<T>, ColumnScope by columnScope