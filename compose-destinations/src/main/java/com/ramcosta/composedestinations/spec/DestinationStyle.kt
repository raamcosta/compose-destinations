package com.ramcosta.composedestinations.spec

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.annotation.InternalDestinationsApi

/**
 * Controls how the destination is shown when navigated to and navigated away from.
 * You can pass the KClass of an implementation to the
 * [com.ramcosta.composedestinations.annotation.Destination.style].
 */
sealed interface DestinationStyle {

    /**
     * No special animation or style.
     * This is the default style used in case none is specified for a given Destination.
     *
     * If you are using "io.github.raamcosta.compose-destinations:animations-core" dependency, then
     * this default may be overridden through `rememberAnimatedNavHostEngine` call
     * which has a `defaultAnimationParams` argument.
     */
    object Default : DestinationStyle

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
    object BottomSheet : DestinationStyle

    /**
     * Marks the destination to have defined enter/exit transitions
     * when coming from or going to certain destinations.
     * It requires "io.github.raamcosta.compose-destinations:animations-core" dependency.
     *
     * You will need to create an object which implements this interface
     * and use its KClass in [com.ramcosta.composedestinations.annotation.Destination.style]
     */
    @ExperimentalAnimationApi
    interface Animated : DestinationStyle {

        fun AnimatedContentScope<NavBackStackEntry>.enterTransition(): EnterTransition? {
            return null
        }

        fun AnimatedContentScope<NavBackStackEntry>.exitTransition(): ExitTransition? {
            return null
        }

        fun AnimatedContentScope<NavBackStackEntry>.popEnterTransition(): EnterTransition? {
            return enterTransition()
        }

        fun AnimatedContentScope<NavBackStackEntry>.popExitTransition(): ExitTransition? {
            return exitTransition()
        }

        /**
         * Can be used to force no animations for certain destinations, if you've overridden
         * the default animation with `defaultAnimationParams`.
         */
        object None : Animated {
            override fun AnimatedContentScope<NavBackStackEntry>.enterTransition() =
                EnterTransition.None

            override fun AnimatedContentScope<NavBackStackEntry>.exitTransition() =
                ExitTransition.None
        }
    }

    /**
     * Marks the destination to be shown as a dialog.
     *
     * You can create implementations that define specific [DialogProperties]
     * or you can use the default values with `style = DestinationStyle.Dialog::class`
     */
    interface Dialog : DestinationStyle {
        val properties: DialogProperties

        companion object Default : Dialog {
            override val properties = DialogProperties()
        }
    }

    /**
     * Marks the style as "Runtime" defined.
     *
     * This means that for this Destination, the style property will
     * contain a setter and you need to set it before calling `DestinationsNavHost`.
     *
     * This is useful if you want to define the style for a Destination in a
     * different module than the one which has the annotated Composable.
     */
    object Runtime: DestinationStyle


    @InternalDestinationsApi
    object Activity: DestinationStyle
}
