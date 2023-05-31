package com.ramcosta.composedestinations.animations.defaults

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

    abstract override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition

    abstract override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(): EnterTransition {
        return enterTransition()
    }

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(): ExitTransition {
        return exitTransition()
    }
}