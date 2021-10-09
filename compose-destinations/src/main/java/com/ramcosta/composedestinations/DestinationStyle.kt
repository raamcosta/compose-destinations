package com.ramcosta.composedestinations

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.window.DialogProperties

sealed interface DestinationStyle {
    object Default : DestinationStyle

    object BottomSheet : DestinationStyle

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

    interface Dialog : DestinationStyle {
        companion object Default : Dialog {
            override val properties = DialogProperties()
        }

        val properties: DialogProperties
    }
}
