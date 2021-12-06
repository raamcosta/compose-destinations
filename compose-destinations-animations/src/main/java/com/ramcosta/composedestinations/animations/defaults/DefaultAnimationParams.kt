package com.ramcosta.composedestinations.animations.defaults

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment

/**
 * Class that can be used to define the default animations for all Destinations with no
 * specific style set and that belong to a specific navigation graph.
 * It is used in [com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine] call.
 *
 * @see [com.google.accompanist.navigation.animation.AnimatedNavHost] for a parameters explanation
 */
@ExperimentalAnimationApi
class NavGraphDefaultAnimationParams(
    val enterTransition: DestinationEnterTransition? = null,
    val exitTransition: DestinationExitTransition? = null,
    val popEnterTransition: DestinationEnterTransition? = enterTransition,
    val popExitTransition: DestinationExitTransition? = exitTransition,
)

/**
 * Class that can be used to define the default animations for all Destinations with no
 * specific style set.
 * You can create your own and pass it to the
 * [com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine] call.
 *
 * @see [com.google.accompanist.navigation.animation.AnimatedNavHost] for a parameters explanation
 */
@ExperimentalAnimationApi
class DefaultAnimationParams(
    val contentAlignment: Alignment = Alignment.Center,
    val enterTransition: DestinationEnterTransition? = DestinationEnterTransition { _, _ -> EnterTransition.None },
    val exitTransition: DestinationExitTransition? = DestinationExitTransition { _, _ -> ExitTransition.None },
    val popEnterTransition: DestinationEnterTransition? = enterTransition,
    val popExitTransition: DestinationExitTransition? = exitTransition,
) {

    companion object {
        val ACCOMPANIST_FADING by lazy {
            DefaultAnimationParams(
                enterTransition = { _, _ -> fadeIn(animationSpec = tween(700)) },
                exitTransition = { _, _ -> fadeOut(animationSpec = tween(700)) }
            )
        }
    }
}