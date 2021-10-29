package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.*

val coreAnimationsExtensionsTemplate = """
package $PACKAGE_NAME

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment

/**
 * Class that can be used to define the default animation for all Destinations with no
 * specific style set.
 * You can create your own and pass it to the `DestinationsNavHost` call.
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

fun interface DestinationEnterTransition {
    @ExperimentalAnimationApi
    fun AnimatedContentScope<String>.enter(initial: Destination?, target: Destination?) : EnterTransition
}

fun interface DestinationExitTransition {
    @ExperimentalAnimationApi
    fun AnimatedContentScope<String>.exit(initial: Destination?, target: Destination?) : ExitTransition
}

""".trimIndent()