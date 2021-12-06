package com.ramcosta.samples.destinationstodosample.ui.screens.greeting

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.*
import com.ramcosta.composedestinations.spec.DestinationStyle

@OptIn(ExperimentalAnimationApi::class)
object GreetingTransitions : DestinationStyle.Animated {

    override fun AnimatedContentScope<String>.enterTransition(
        initial: NavBackStackEntry,
        target: NavBackStackEntry
    ): EnterTransition? {

        return when (initial.navDestination) {
            SettingsDestination,
            ProfileScreenDestination ->
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }

    override fun AnimatedContentScope<String>.exitTransition(
        initial: NavBackStackEntry,
        target: NavBackStackEntry
    ): ExitTransition? {

        return when (target.navDestination) {
            ProfileScreenDestination,
            SettingsDestination ->
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }

    override fun AnimatedContentScope<String>.popEnterTransition(
        initial: NavBackStackEntry,
        target: NavBackStackEntry
    ): EnterTransition? {

        return when (initial.navDestination) {
            ProfileScreenDestination,
            SettingsDestination ->
                slideInHorizontally(
                    initialOffsetX = { -1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }

    override fun AnimatedContentScope<String>.popExitTransition(
        initial: NavBackStackEntry,
        target: NavBackStackEntry
    ): ExitTransition? {

        return when (target.navDestination) {
            ProfileScreenDestination,
            SettingsDestination ->
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }
}