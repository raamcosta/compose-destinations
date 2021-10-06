package com.ramcosta.samples.destinationstodosample.destinations.transitions

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.ramcosta.composedestinations.Destination
import com.ramcosta.composedestinations.DestinationTransitions
import com.ramcosta.composedestinations.GreetingDestination

@ExperimentalAnimationApi
object ProfileTransitions : DestinationTransitions {

    override val enterTransition: AnimatedContentScope<String>.(Destination?, Destination?) -> EnterTransition? = { initial, _ ->
        when (initial) {
            GreetingDestination ->
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }

    override val exitTransition: AnimatedContentScope<String>.(Destination?, Destination?) -> ExitTransition? = { _, target ->
        when (target) {
            GreetingDestination ->
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }

    override val popEnterTransition: AnimatedContentScope<String>.(Destination?, Destination?) -> EnterTransition? = { initial, _ ->
        when (initial) {
            GreetingDestination ->
                slideInHorizontally(
                    initialOffsetX = { -1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }

    override val popExitTransition: AnimatedContentScope<String>.(Destination?, Destination?) -> ExitTransition? = { _, target ->
        when (target) {
            GreetingDestination ->
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(700)
                )
            else -> null
        }
    }
}