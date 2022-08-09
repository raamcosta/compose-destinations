package com.ramcosta.composedestinations.scope

import androidx.annotation.RestrictTo
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DestinationsNavController
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.TypedDestinationSpec

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class DestinationScopeImpl<T>(
    override val destination: TypedDestinationSpec<T>,
    override val navBackStackEntry: NavBackStackEntry,
    override val navController: NavController,
) : DestinationScope<T> {

    override val navArgs: T by lazy(LazyThreadSafetyMode.NONE) {
        destination.argsFrom(navBackStackEntry)
    }

    override val destinationsNavigator: DestinationsNavigator
        get() = DestinationsNavController(navController, navBackStackEntry)

    internal class Default<T>(
        destination: TypedDestinationSpec<T>,
        navBackStackEntry: NavBackStackEntry,
        navController: NavController,
    ) : DestinationScopeImpl<T>(
        destination,
        navBackStackEntry,
        navController,
    )
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class NavGraphBuilderDestinationScopeImpl<T>(
    override val destination: TypedDestinationSpec<T>,
    override val navBackStackEntry: NavBackStackEntry,
) : NavGraphBuilderDestinationScope<T> {

    override val navArgs: T by lazy(LazyThreadSafetyMode.NONE) {
        destination.argsFrom(navBackStackEntry)
    }

    override fun destinationsNavigator(navController: NavController): DestinationsNavigator {
        return DestinationsNavController(navController, navBackStackEntry)
    }

    internal class Default<T>(
        destination: TypedDestinationSpec<T>,
        navBackStackEntry: NavBackStackEntry,
    ) : NavGraphBuilderDestinationScopeImpl<T>(
        destination,
        navBackStackEntry,
    )
}
