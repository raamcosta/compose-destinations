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
import com.ramcosta.composedestinations.result.resultRecipient
import com.ramcosta.composedestinations.spec.DestinationSpec

/**
 * Scope where a destination screen will be called in.
 */
@Immutable
interface DestinationScope<T> {

    /**
     * [DestinationSpec] related to this scope
     */
    val destination: DestinationSpec<T>

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

/**
 * Returns a well typed [ResultBackNavigator] for this [DestinationScope]
 */
@Composable
inline fun <reified R> DestinationScope<*>.resultBackNavigator(): ResultBackNavigator<R> =
    resultBackNavigator(destination, R::class.java, navController, navBackStackEntry)

/**
 * Returns a well typed [ResultRecipient] for this [DestinationScope]
 */
@Composable
inline fun <reified D : DestinationSpec<*>, reified R> DestinationScope<*>.resultRecipient(): ResultRecipient<D, R> =
    resultRecipient(navBackStackEntry, D::class.java, R::class.java)

/**
 * Like [DestinationScope] but also [AnimatedVisibilityScope] so that
 * if you're using the "animations-core" you can use this Scope as a receiver
 * of your Animated Composable
 */
@ExperimentalAnimationApi
@Immutable
interface AnimatedDestinationScope<T> : DestinationScope<T>, AnimatedVisibilityScope

/**
 * Like [DestinationScope] but also [ColumnScope] so that
 * if you're using the "animations-core" you can use this Scope as a receiver
 * of your Bottom Sheet styled Composable
 */
@Immutable
interface BottomSheetDestinationScope<T> : DestinationScope<T>, ColumnScope
