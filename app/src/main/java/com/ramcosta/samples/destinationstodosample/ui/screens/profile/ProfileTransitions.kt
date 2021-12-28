package com.ramcosta.samples.destinationstodosample.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.destinations.GreetingScreenDestination
import com.ramcosta.composedestinations.navDestination
import com.ramcosta.composedestinations.spec.DestinationStyle

@OptIn(ExperimentalAnimationApi::class)
object ProfileTransitions : DestinationStyle.Animated {

    override fun AnimatedContentScope<NavBackStackEntry>.enterTransition(): EnterTransition? {

        return when (initialState.navDestination) {
            GreetingScreenDestination ->
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }

    override fun AnimatedContentScope<NavBackStackEntry>.exitTransition(): ExitTransition? {

        return when (targetState.navDestination) {
            GreetingScreenDestination ->
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }

    override fun AnimatedContentScope<NavBackStackEntry>.popEnterTransition(): EnterTransition? {

        return when (initialState.navDestination) {
            GreetingScreenDestination ->
                slideInHorizontally(
                    initialOffsetX = { -1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }

    override fun AnimatedContentScope<NavBackStackEntry>.popExitTransition(): ExitTransition? {

        return when (targetState.navDestination) {
            GreetingScreenDestination ->
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }
}