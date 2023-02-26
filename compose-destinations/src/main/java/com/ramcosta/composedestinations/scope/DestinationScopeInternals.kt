package com.ramcosta.composedestinations.scope

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.navigation.DestinationDependenciesContainer
import com.ramcosta.composedestinations.navigation.DestinationDependenciesContainerImpl
import com.ramcosta.composedestinations.navigation.DestinationsNavController
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationSpec

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class DestinationScopeImpl<T> : DestinationScope<T> {
    abstract val dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit

    override val navArgs: T by lazy(LazyThreadSafetyMode.NONE) {
        destination.argsFrom(navBackStackEntry.arguments)
    }

    override val destinationsNavigator: DestinationsNavigator
        get() = DestinationsNavController(navController, navBackStackEntry)

    @Composable
    override fun buildDependencies(): DestinationDependenciesContainer {
        return remember(navBackStackEntry) { DestinationDependenciesContainerImpl(this) }
            .apply { dependenciesContainerBuilder() }
    }

    internal class Default<T>(
        override val destination: DestinationSpec<T>,
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
        return DestinationsNavController(navController, navBackStackEntry)
    }

    internal class Default<T>(
        override val destination: DestinationSpec<T>,
        override val navBackStackEntry: NavBackStackEntry
    ) : NavGraphBuilderDestinationScopeImpl<T>()
}
