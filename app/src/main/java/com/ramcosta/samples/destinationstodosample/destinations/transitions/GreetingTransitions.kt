package com.ramcosta.samples.destinationstodosample.destinations.transitions

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.ramcosta.composedestinations.*

@ExperimentalAnimationApi
object GreetingTransitions : AnimatedDestinationStyle {

    override val enterTransition: AnimatedContentScope<String>.(Destination?, Destination?) -> EnterTransition? =
        { initial, _ ->
            when (initial) {
                ProfileScreenDestination ->
                    slideInHorizontally(
                        initialOffsetX = { 1000 },
                        animationSpec = tween(700)
                    )
                else -> null
            }
        }

    override val exitTransition: AnimatedContentScope<String>.(Destination?, Destination?) -> ExitTransition? =
        { _, target ->
            when (target) {
                ProfileScreenDestination ->
                    slideOutHorizontally(
                        targetOffsetX = { -1000 },
                        animationSpec = tween(700)
                    )
                else -> null
            }
        }

    override val popEnterTransition: AnimatedContentScope<String>.(Destination?, Destination?) -> EnterTransition? =
        { initial, _ ->
            when (initial) {
                ProfileScreenDestination ->
                    slideInHorizontally(
                        initialOffsetX = { -1000 },
                        animationSpec = tween(700)
                    )
                else -> null
            }
        }

    override val popExitTransition: AnimatedContentScope<String>.(Destination?, Destination?) -> ExitTransition? =
        { _, target ->
            when (target) {
                ProfileScreenDestination ->
                    slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = tween(700)
                    )
                else -> null
            }
        }
}