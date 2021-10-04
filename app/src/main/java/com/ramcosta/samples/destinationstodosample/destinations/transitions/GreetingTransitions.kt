package com.ramcosta.samples.destinationstodosample.destinations.transitions

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import com.ramcosta.composedestinations.Destination
import com.ramcosta.composedestinations.DestinationTransitionsSpec
import com.ramcosta.composedestinations.ProfileDestination
import com.ramcosta.composedestinations.annotation.DestinationTransitions

@ExperimentalAnimationApi
@DestinationTransitions("greeting")
object GreetingTransitions : DestinationTransitionsSpec {

    override val enterTransition: AnimatedContentScope<String>.(Destination?, Destination?) -> EnterTransition? =
        { initial, _ ->
            when (initial) {
                ProfileDestination ->
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
                ProfileDestination ->
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
                ProfileDestination ->
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
                ProfileDestination ->
                    slideOutHorizontally(
                        targetOffsetX = { 1000 },
                        animationSpec = tween(700)
                    )
                else -> null
            }
        }
}