package com.ramcosta.samples.destinationstodosample.destinations.greeting

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.ramcosta.composedestinations.*

@OptIn(ExperimentalAnimationApi::class)
object GreetingTransitions : AnimatedDestinationStyle {

    override fun AnimatedContentScope<String>.enterTransition(
        initial: Destination?,
        target: Destination?
    ): EnterTransition? {

        return when (initial) {
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
        initial: Destination?,
        target: Destination?
    ): ExitTransition? {

        return when (target) {
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
        initial: Destination?,
        target: Destination?
    ): EnterTransition? {

        return when (initial) {
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
        initial: Destination?,
        target: Destination?
    ): ExitTransition? {

        return when (target) {
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