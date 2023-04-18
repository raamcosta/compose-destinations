package com.ramcosta.composedestinations.spec

import androidx.compose.ui.window.DialogProperties
import com.ramcosta.composedestinations.annotation.InternalDestinationsApi

/**
 * Controls how the destination is shown when navigated to and navigated away from.
 * You can pass the KClass of an implementation to the
 * [com.ramcosta.composedestinations.annotation.Destination.style].
 */
interface DestinationStyle {

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
