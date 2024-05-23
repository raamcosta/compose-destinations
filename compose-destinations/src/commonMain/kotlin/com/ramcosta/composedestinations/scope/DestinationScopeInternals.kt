package com.ramcosta.composedestinations.scope

import androidx.annotation.RestrictTo
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.navigation.DestinationDependenciesContainer
import com.ramcosta.composedestinations.navigation.DestinationDependenciesContainerImpl
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.TypedDestinationSpec
import com.ramcosta.composedestinations.utils.toDestinationsNavigator

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class DestinationScopeImpl<T> : DestinationScope<T> {
    abstract val dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit

    override val navArgs: T by lazy(LazyThreadSafetyMode.NONE) {
        destination.argsFrom(navBackStackEntry.arguments)
    }

    override val destinationsNavigator: DestinationsNavigator
        get() = navController.toDestinationsNavigator()

    @Composable
    override fun buildDependencies(): DestinationDependenciesContainer {
        return remember(navBackStackEntry) { DestinationDependenciesContainerImpl(this) }
            .apply { dependenciesContainerBuilder() }
    }

    private val isCurrentNavBackStackEntryResumed = { navBackStackEntry.lifecycle.currentState == Lifecycle.State.RESUMED }

    internal class Default<T>(
        override val destination: TypedDestinationSpec<T>,
        override val navBackStackEntry: NavBackStackEntry,
        override val navController: NavController,
        override val dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
    ) : DestinationScopeImpl<T>()
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class NavGraphBuilderDestinationScopeImpl<T> : NavGraphBuilderDestinationScope<T> {

    override val navArgs: T by lazy(LazyThreadSafetyMode.NONE) {
        destination.argsFrom(navBackStackEntry.arguments)
    }

    override fun destinationsNavigator(navController: NavController): DestinationsNavigator {
        return navController.toDestinationsNavigator()
    }

    private val isCurrentNavBackStackEntryResumed = { navBackStackEntry.lifecycle.currentState == Lifecycle.State.RESUMED }

    internal class Default<T>(
        override val destination: TypedDestinationSpec<T>,
        override val navBackStackEntry: NavBackStackEntry
    ) : NavGraphBuilderDestinationScopeImpl<T>()
}

internal class AnimatedDestinationScopeImpl<T>(
    override val destination: TypedDestinationSpec<T>,
    override val navBackStackEntry: NavBackStackEntry,
    override val navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    override val dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
) : DestinationScopeImpl<T>(),
    AnimatedDestinationScope<T>,
    AnimatedVisibilityScope by animatedVisibilityScope

internal class AnimatedNavGraphBuilderDestinationScopeImpl<T>(
    override val destination: TypedDestinationSpec<T>,
    override val navBackStackEntry: NavBackStackEntry,
    animatedVisibilityScope: AnimatedVisibilityScope,
) : NavGraphBuilderDestinationScopeImpl<T>(),
    AnimatedNavGraphBuilderDestinationScope<T>,
    AnimatedVisibilityScope by animatedVisibilityScope
