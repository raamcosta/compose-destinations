package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.*

val coreAnimationsExtensionsTemplate = """
package $PACKAGE_NAME

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.navigation.NavBackStackEntry

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
    val enterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition)? = { EnterTransition.None },
    val exitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition)? = { ExitTransition.None },
    val popEnterTransition: (AnimatedContentScope<NavBackStackEntry>.() -> EnterTransition)? = enterTransition,
    val popExitTransition: (AnimatedContentScope<NavBackStackEntry>.() -> ExitTransition)? = exitTransition,
) {

    companion object {
        val ACCOMPANIST_FADING by lazy {
            DefaultAnimationParams(
                enterTransition = { fadeIn(animationSpec = tween(700)) },
                exitTransition = { fadeOut(animationSpec = tween(700)) }
            )
        }
    }
}

""".trimIndent()