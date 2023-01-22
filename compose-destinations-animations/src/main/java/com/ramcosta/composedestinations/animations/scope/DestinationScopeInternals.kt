package com.ramcosta.composedestinations.animations.scope

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.scope.*
import com.ramcosta.composedestinations.spec.DestinationSpec

@ExperimentalAnimationApi
internal class AnimatedDestinationScopeImpl<T>(
    override val destination: DestinationSpec<T>,
    override val navBackStackEntry: NavBackStackEntry,
    override val navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    override val dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
) : DestinationScopeImpl<T>(),
    AnimatedDestinationScope<T>,
    AnimatedVisibilityScope by animatedVisibilityScope

internal class BottomSheetDestinationScopeImpl<T>(
    override val destination: DestinationSpec<T>,
    override val navBackStackEntry: NavBackStackEntry,
    override val navController: NavController,
    columnScope: ColumnScope,
    override val dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
) : DestinationScopeImpl<T>(),
    BottomSheetDestinationScope<T>,
    ColumnScope by columnScope

@ExperimentalAnimationApi
internal class AnimatedNavGraphBuilderDestinationScopeImpl<T>(
    override val destination: DestinationSpec<T>,
    override val navBackStackEntry: NavBackStackEntry,
    animatedVisibilityScope: AnimatedVisibilityScope,
) : NavGraphBuilderDestinationScopeImpl<T>(),
    AnimatedNavGraphBuilderDestinationScope<T>,
    AnimatedVisibilityScope by animatedVisibilityScope

internal class BottomSheetNavGraphBuilderDestinationScopeImpl<T>(
    override val destination: DestinationSpec<T>,
    override val navBackStackEntry: NavBackStackEntry,
    columnScope: ColumnScope,
) : NavGraphBuilderDestinationScopeImpl<T>(),
    BottomSheetNavGraphBuilderDestinationScope<T>,
    ColumnScope by columnScope