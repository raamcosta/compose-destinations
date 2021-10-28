package com.ramcosta.samples.destinationstodosample.destinations.styles

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.ramcosta.composedestinations.AnimatedDestinationStyle
import com.ramcosta.composedestinations.Destination
import com.ramcosta.composedestinations.GreetingScreenDestination

@OptIn(ExperimentalAnimationApi::class)
object SettingsTransitions : AnimatedDestinationStyle {

    override fun AnimatedContentScope<String>.enterTransition(
        initial: Destination?,
        target: Destination?
    ): EnterTransition? {

        return when (initial) {
            GreetingScreenDestination ->
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

        return popExitTransition(initial, target)
    }

    override fun AnimatedContentScope<String>.popEnterTransition(
        initial: Destination?,
        target: Destination?
    ): EnterTransition? {

        return when (initial) {
            GreetingScreenDestination ->
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
            GreetingScreenDestination ->
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }
}