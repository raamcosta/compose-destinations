package com.ramcosta.composedestinations.scope

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.result.resultBackNavigator
import com.ramcosta.composedestinations.result.resultRecipient
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.TypedDestinationSpec

@Immutable
interface NavGraphBuilderDestinationScope<T> {

    /**
     * [DestinationSpec] related to this scope
     */
    val destination: TypedDestinationSpec<T>

    /**
     * [NavBackStackEntry] of the current destination
     */
    val navBackStackEntry: NavBackStackEntry

    /**
     * Class holding the navigation arguments passed to this destination
     * or [Unit] if the destination has no arguments
     */
    val navArgs: T

    /**
     * [DestinationsNavigator] useful to navigate from this destination
     */
    fun destinationsNavigator(navController: NavController): DestinationsNavigator
}

/**
 * Returns a well typed [ResultBackNavigator] for this [NavGraphBuilderDestinationScope]
 */
@Composable
inline fun <R, reified A: R & Any> NavGraphBuilderDestinationScope<*>.resultBackNavigator(
    navController: NavController
): ResultBackNavigator<R> =
    resultBackNavigator(destination, A::class, navController, navBackStackEntry)


/**
 * Returns a well typed [ResultRecipient] for this [NavGraphBuilderDestinationScope]
 */
@Composable
inline fun <reified D : DestinationSpec, R, reified A: R & Any> NavGraphBuilderDestinationScope<*>.resultRecipient(): ResultRecipient<D, R> =
    resultRecipient(navBackStackEntry, D::class, A::class)

/**
 * Like [NavGraphBuilderDestinationScope] but also [AnimatedVisibilityScope] so that
 * you can use this Scope as a receiver of your Animated and Default styled Composable
 */
@Immutable
interface AnimatedNavGraphBuilderDestinationScope<T> : NavGraphBuilderDestinationScope<T>,
    AnimatedVisibilityScope

/**
 * Like [NavGraphBuilderDestinationScope] but also [ColumnScope] so that
 * if you're using the "bottom-sheet" dependency you can use this Scope as a receiver
 * of your Bottom Sheet styled Composable
 */
@Immutable
interface BottomSheetNavGraphBuilderDestinationScope<T> : NavGraphBuilderDestinationScope<T>,
    ColumnScope