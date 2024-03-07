package com.ramcosta.samples.playground.ui.screens.greeting

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.utils.destination
import com.ramcosta.samples.playground.ui.screens.destinations.ProfileScreenDestination
import com.ramcosta.samples.playground.ui.screens.destinations.SettingsScreenDestination

object GreetingTransitions : DestinationStyle.Animated() {

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
        when (initialState.destination()) {
            SettingsScreenDestination,
            ProfileScreenDestination ->
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }

    override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
        when (targetState.destination()) {
            ProfileScreenDestination,
            SettingsScreenDestination ->
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }

    override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
        when (initialState.destination()) {
            ProfileScreenDestination,
            SettingsScreenDestination ->
                slideInHorizontally(
                    initialOffsetX = { -1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
        when (targetState.destination()) {
            ProfileScreenDestination,
            SettingsScreenDestination ->
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }
}