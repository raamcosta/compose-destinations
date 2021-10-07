package com.ramcosta.composedestinations

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.ui.window.DialogProperties

sealed interface DestinationStyle {
    object Default: DestinationStyle

    object BottomSheet : DestinationStyle

    @ExperimentalAnimationApi
    interface Animated<T: DestinationSpec> : DestinationStyle {

        val enterTransition: (AnimatedContentScope<String>.(initial: T?, target: T?) -> EnterTransition?)? get() = null

        val exitTransition: (AnimatedContentScope<String>.(initial: T?, target: T?) -> ExitTransition?)? get() = null

        val popEnterTransition: (AnimatedContentScope<String>.(initial: T?, target: T?) -> EnterTransition?)? get() = enterTransition

        val popExitTransition: (AnimatedContentScope<String>.(initial: T?, target: T?) -> ExitTransition?)? get() = exitTransition
    }

    interface Dialog : DestinationStyle {
        companion object Default : Dialog {
            override val properties = DialogProperties()
        }

        val properties: DialogProperties
    }
}
