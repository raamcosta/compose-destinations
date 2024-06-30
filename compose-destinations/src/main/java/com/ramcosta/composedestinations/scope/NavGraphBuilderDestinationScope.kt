package com.ramcosta.composedestinations.scope

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navargs.DestinationsNavType
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
 *
 * When calling this directly (usually when using manually calling your destination
 * Composables), you can go into the corresponding generated Destination and check what
 * [DestinationsNavType] is passed in to the corresponding Composable [ResultBackNavigator]
 * parameter.
 *
 * @param resultNavType [DestinationsNavType] for the [R] type, which will
 * handle serialization of the result.
 */
@Composable
inline fun <R> NavGraphBuilderDestinationScope<*>.resultBackNavigator(
    navController: NavController,
    resultNavType: DestinationsNavType<in R>
): ResultBackNavigator<R> =
    resultBackNavigator(destination, resultNavType, navController, navBackStackEntry)


/**
 * Returns a well typed [ResultRecipient] for this [NavGraphBuilderDestinationScope]
 *
 * When calling this directly (usually when using manually calling your destination
 * Composables), you can go into the corresponding generated Destination and check what
 * [DestinationsNavType] is passed in to the corresponding Composable [ResultBackNavigator]
 * parameter.
 *
 * @param resultNavType [DestinationsNavType] for the [R] type, which will
 * handle serialization of the result.
 */
@Composable
inline fun <reified D : DestinationSpec, R> NavGraphBuilderDestinationScope<*>.resultRecipient(
    resultNavType: DestinationsNavType<in R>
): ResultRecipient<D, R> =
    resultRecipient(navBackStackEntry, D::class, resultNavType)

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