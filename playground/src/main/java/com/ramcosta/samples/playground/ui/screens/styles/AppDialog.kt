package com.ramcosta.samples.playground.ui.screens.styles

import androidx.compose.ui.window.DialogProperties
import com.ramcosta.composedestinations.spec.DestinationStyle

object AppDialog : DestinationStyle.Dialog() {
    override val properties: DialogProperties
        get() = DialogProperties(
            dismissOnBackPress = false
        )
}