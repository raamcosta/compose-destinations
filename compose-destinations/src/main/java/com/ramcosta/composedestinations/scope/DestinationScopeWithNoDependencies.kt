package com.ramcosta.composedestinations.scope

import androidx.compose.runtime.Immutable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.TypedDestinationSpec

/**
 * Scope where a destination screen will be called in.
 *
 * This is used when we still have no dependencies
 */
@Immutable
interface DestinationScopeWithNoDependencies<T> {

    /**
     * [DestinationSpec] related to this scope
     */
    val destination: TypedDestinationSpec<T>

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