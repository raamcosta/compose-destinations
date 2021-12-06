package com.ramcosta.samples.destinationstodosample.ui.screens.styles

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.GreetingScreenDestination
import com.ramcosta.composedestinations.navDestination
import com.ramcosta.composedestinations.spec.DestinationStyle

@OptIn(ExperimentalAnimationApi::class)
object SettingsTransitions : DestinationStyle.Animated {

    override fun AnimatedContentScope<String>.enterTransition(
        initial: NavBackStackEntry,
        target: NavBackStackEntry
    ): EnterTransition? {

        return when (initial.navDestination) {
            GreetingScreenDestination ->
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
            GreetingScreenDestination ->
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
            GreetingScreenDestination ->
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
            GreetingScreenDestination ->
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }
}