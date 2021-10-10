package com.ramcosta.composedestinations.spec

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.window.DialogProperties

/**
 * Controls how the destination is shown when navigated to and navigated away from.
 * You can pass the KClass of an implementation to the
 * [com.ramcosta.composedestinations.annotation.Destination.style].
 */
sealed interface DestinationStyle {

    /**
     * No special animation or style.
     * This is the default style used in case none is specified for a given Destination.
     */
    object Default : DestinationStyle

    /**
     * Marks the destination to be shown with a bottom sheet style.
     * It requires Accompanist Material dependency.
     */
    object BottomSheet : DestinationStyle

    /**
     * Marks the destination to have defined enter/exit transitions
     * when coming from or going to certain destinations.
     * It requires Accompanist Navigation Animation dependency.
     *
     * You will need to create an object which implements this interface
     * and use the KClass in the [com.ramcosta.composedestinations.annotation.Destination.style]
     */
    @ExperimentalAnimationApi
    interface Animated<T : DestinationSpec> : DestinationStyle {

        fun AnimatedContentScope<String>.enterTransition(
            initial: T?,
            target: T?
        ): EnterTransition? {
            return null
        }

        fun AnimatedContentScope<String>.exitTransition(
            initial: T?,
            target: T?
        ): ExitTransition? {
            return null
        }

        fun AnimatedContentScope<String>.popEnterTransition(
            initial: T?,
            target: T?
        ): EnterTransition? {
            return enterTransition(initial, target)
        }

        fun AnimatedContentScope<String>.popExitTransition(
            initial: T?,
            target: T?
        ): ExitTransition? {
            return exitTransition(initial, target)
        }
    }

    /**
     * Marks the destination to be shown as a dialog.
     *
     * You can create implementations that define specific [DialogProperties]
     * or you can use the default values with `style = DestinationStyle.Dialog::class`
     */
    interface Dialog : DestinationStyle {
        companion object Default : Dialog {
            override val properties = DialogProperties()
        }

        val properties: DialogProperties
    }
}
