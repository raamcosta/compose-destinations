package com.ramcosta.composedestinations.spec

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavBackStackEntry

/**
 * Marks the destination to be shown with a bottom sheet style.
 * It requires "io.github.raamcosta.compose-destinations:animations-core" dependency.
 *
 * You will need to use a `ModalBottomSheetLayout` wrapping your
 * top level Composable.
 * Example:
 * ```
 * val navController = rememberAnimatedNavController()
 * val bottomSheetNavigator = rememberBottomSheetNavigator()
 * navController.navigatorProvider += bottomSheetNavigator
 *
 * ModalBottomSheetLayout(
 *     bottomSheetNavigator = bottomSheetNavigator
 * ) {
 *     //YOUR TOP LEVEL COMPOSABLE LIKE `DestinationsNavHost` or `Scaffold`
 * }
 * ```
 */
object DestinationStyleBottomSheet : DestinationStyle

/**
 * Marks the destination to have defined enter/exit transitions
 * when coming from or going to certain destinations.
 * It requires "io.github.raamcosta.compose-destinations:animations-core" dependency.
 *
 * You will need to create an object which implements this interface
 * and use its KClass in [com.ramcosta.composedestinations.annotation.Destination.style]
 */
@ExperimentalAnimationApi
interface DestinationStyleAnimated : DestinationStyle {

    fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition? {
        return null
    }

    fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition? {
        return null
    }

    fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(): EnterTransition? {
        return enterTransition()
    }

    fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(): ExitTransition? {
        return exitTransition()
    }

    /**
     * Can be used to force no animations for certain destinations, if you've overridden
     * the default animation with `defaultAnimationParams`.
     */
    object None : DestinationStyleAnimated {
        override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition() =
            EnterTransition.None

        override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition() =
            ExitTransition.None
    }
}