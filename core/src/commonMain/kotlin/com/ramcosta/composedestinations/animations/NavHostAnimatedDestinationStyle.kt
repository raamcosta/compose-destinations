package com.ramcosta.composedestinations.animations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationStyle

/**
 * Like [DestinationStyle.Animated] but doesn't allow the return of null values.
 *
 * This is mainly used for [com.ramcosta.composedestinations.DestinationsNavHost] call
 * and [com.ramcosta.composedestinations.annotation.NavHostGraph.defaultTransitions] since
 * for NavHost graphs, transitions must be defined (even if they're "No animations").
 */
abstract class NavHostAnimatedDestinationStyle: DestinationStyle.Animated() {

    abstract override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition

    abstract override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition

    override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
        get() = enterTransition

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
        get() = exitTransition
}