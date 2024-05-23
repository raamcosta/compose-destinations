package com.ramcosta.composedestinations.spec

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle

/**
 * [ActivityDestinationSpec] that does not contain any navigation arguments.
 * It itself is a [Direction]
 */
interface DirectionActivityDestinationSpec: ActivityDestinationSpec<Unit>, DirectionDestinationSpec {

    override fun argsFrom(savedStateHandle: SavedStateHandle) {
        super<ActivityDestinationSpec>.argsFrom(savedStateHandle)
    }

    override fun argsFrom(bundle: Bundle?) = Unit
}