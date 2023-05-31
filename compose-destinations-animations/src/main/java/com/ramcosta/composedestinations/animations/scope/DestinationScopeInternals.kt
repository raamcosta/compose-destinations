package com.ramcosta.composedestinations.animations.scope

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.scope.BottomSheetDestinationScope
import com.ramcosta.composedestinations.scope.BottomSheetNavGraphBuilderDestinationScope
import com.ramcosta.composedestinations.scope.DestinationScopeImpl
import com.ramcosta.composedestinations.scope.NavGraphBuilderDestinationScopeImpl
import com.ramcosta.composedestinations.spec.DestinationSpec

internal class BottomSheetDestinationScopeImpl<T>(
    override val destination: DestinationSpec<T>,
    override val navBackStackEntry: NavBackStackEntry,
    override val navController: NavController,
    columnScope: ColumnScope,
    override val dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
) : DestinationScopeImpl<T>(),
    BottomSheetDestinationScope<T>,
    ColumnScope by columnScope

internal class BottomSheetNavGraphBuilderDestinationScopeImpl<T>(
    override val destination: DestinationSpec<T>,
    override val navBackStackEntry: NavBackStackEntry,
    columnScope: ColumnScope,
) : NavGraphBuilderDestinationScopeImpl<T>(),
    BottomSheetNavGraphBuilderDestinationScope<T>,
    ColumnScope by columnScope