package com.ramcosta.composedestinations.scope

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.ramcosta.composedestinations.result.ResultRecipient
import com.ramcosta.composedestinations.result.resultBackNavigator
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.result.resultRecipient

@Immutable
interface NavGraphBuilderDestinationScope<T> {

    /**
     * [DestinationSpec] related to this scope
     */
    val destination: DestinationSpec<T>

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
inline fun <reified R> NavGraphBuilderDestinationScope<*>.resultBackNavigator(
    navController: NavController
): ResultBackNavigator<R> =
    resultBackNavigator(destination, R::class.java, navController, navBackStackEntry)


/**
 * Returns a well typed [ResultRecipient] for this [NavGraphBuilderDestinationScope]
 */
@Composable
inline fun <reified D : DestinationSpec<*>, reified R> NavGraphBuilderDestinationScope<*>.resultRecipient(): ResultRecipient<D, R> =
    resultRecipient(navBackStackEntry, D::class.java, R::class.java)

/**
 * Like [NavGraphBuilderDestinationScope] but also [ColumnScope] so that
 * if you're using the "animations-core" you can use this Scope as a receiver
 * of your Bottom Sheet styled Composable
 */
@ExperimentalAnimationApi
@Immutable
interface AnimatedNavGraphBuilderDestinationScope<T> : NavGraphBuilderDestinationScope<T>,
    AnimatedVisibilityScope

/**
 * Like [NavGraphBuilderDestinationScope] but also [ColumnScope] so that
 * if you're using the "animations-core" you can use this Scope as a receiver
 * of your Bottom Sheet styled Composable
 */
@Immutable
interface BottomSheetNavGraphBuilderDestinationScope<T> : NavGraphBuilderDestinationScope<T>,
    ColumnScope