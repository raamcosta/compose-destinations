package com.ramcosta.composedestinations.animations.defaults

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

@ExperimentalAnimationApi
interface NavGraphDefaultAnimationParams {
    val enterTransition: DestinationEnterTransition?
    val exitTransition: DestinationExitTransition?
    val popEnterTransition: DestinationEnterTransition?
    val popExitTransition: DestinationExitTransition?
}

/**
 * Class that can be used to define the default animations for all Destinations with no
 * specific style set.
 * You can create your own and pass it to the
 * [com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine] call.
 *
 * @see [com.google.accompanist.navigation.animation.AnimatedNavHost] for a parameters explanation
 */
@ExperimentalAnimationApi
class RootNavGraphDefaultAnimations(
    override val enterTransition: DestinationEnterTransition = DestinationEnterTransition { EnterTransition.None },
    override val exitTransition: DestinationExitTransition = DestinationExitTransition { ExitTransition.None },
    override val popEnterTransition: DestinationEnterTransition = enterTransition,
    override val popExitTransition: DestinationExitTransition = exitTransition,
): NavGraphDefaultAnimationParams {
    companion object {
        val ACCOMPANIST_FADING by lazy {
            RootNavGraphDefaultAnimations(
                enterTransition = { fadeIn(animationSpec = tween(700)) },
                exitTransition = { fadeOut(animationSpec = tween(700)) }
            )
        }
    }
}

/**
 * Class that can be used to define the default animations for all Destinations with no
 * specific style set and that belong to a specific nested navigation graph.
 * It is used in [com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine] call.
 *
 * @see [com.google.accompanist.navigation.animation.navigation] for a parameters explanation
 */
@ExperimentalAnimationApi
class NestedNavGraphDefaultAnimations(
    override val enterTransition: DestinationEnterTransition? = null,
    override val exitTransition: DestinationExitTransition? = null,
    override val popEnterTransition: DestinationEnterTransition? = enterTransition,
    override val popExitTransition: DestinationExitTransition? = exitTransition,
) : NavGraphDefaultAnimationParams {
    companion object {
        val ACCOMPANIST_FADING by lazy {
            NestedNavGraphDefaultAnimations(
                enterTransition = { fadeIn(animationSpec = tween(700)) },
                exitTransition = { fadeOut(animationSpec = tween(700)) }
            )
        }
    }
}
