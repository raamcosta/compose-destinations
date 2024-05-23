package com.ramcosta.composedestinations.scope

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DestinationDependenciesContainer
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.result.resultBackNavigator
import com.ramcosta.composedestinations.result.resultRecipient
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.TypedDestinationSpec

/**
 * Scope where a destination screen will be called in.
 */
@Immutable
interface DestinationScope<T>: DestinationScopeWithNoDependencies<T> {

    /**
     * [DestinationSpec] related to this scope
     */
    override val destination: TypedDestinationSpec<T>

    /**
     * [NavBackStackEntry] of the current destination
     */
    override val navBackStackEntry: NavBackStackEntry

    /**
     * [NavController] related to the NavHost
     */
    override val navController: NavController

    /**
     * [DestinationsNavigator] useful to navigate from this destination
     */
    override val destinationsNavigator: DestinationsNavigator

    /**
     * Builds the [DestinationDependenciesContainer] which contains
     * all dependencies by calling `DestinationsNavHost`'s `dependencyContainerBuilder` lambda parameter.
     *
     * When used, it will run the `dependencyContainerBuilder` so even though that lambda
     * is not expected to do any heavy calculations, use it only once per composition. So if you
     * need multiple dependencies, store the result of this in a val first, then use the val each time.
     */
    @Composable
    fun buildDependencies(): DestinationDependenciesContainer

    /**
     * Class holding the navigation arguments passed to this destination
     * or [Unit] if the destination has no arguments
     */
    override val navArgs: T
}

/**
 * Returns a well typed [ResultBackNavigator] for this [DestinationScope]
 */
@Composable
inline fun <R, reified A: R & Any> DestinationScopeWithNoDependencies<*>.resultBackNavigator(): ResultBackNavigator<R> =
    resultBackNavigator(destination, A::class, navController, navBackStackEntry)

/**
 * Returns a well typed [ResultRecipient] for this [DestinationScope]
 */
@Composable
inline fun <reified D : DestinationSpec, R, reified A: R & Any> DestinationScopeWithNoDependencies<*>.resultRecipient(): ResultRecipient<D, R> =
    resultRecipient(navBackStackEntry, D::class, A::class)

/**
 * Like [DestinationScope] but also [AnimatedVisibilityScope] so that
 * you can use this Scope as a receiver of your Animated or Default styled Composable
 */
@Immutable
interface AnimatedDestinationScope<T> : DestinationScope<T>, AnimatedVisibilityScope

/**
 * Like [DestinationScope] but also [ColumnScope] so that
 * if you're using the "bottom-sheet" dependency you can use this Scope as a receiver
 * of your Bottom Sheet styled Composable
 */
@Immutable
interface BottomSheetDestinationScope<T> : DestinationScope<T>, ColumnScope
