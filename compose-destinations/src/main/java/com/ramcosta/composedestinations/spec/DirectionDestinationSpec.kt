package com.ramcosta.composedestinations.spec

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle

/**
 * [TypedDestinationSpec] that does not contain any navigation arguments.
 * It itself is a [Direction]
 */
interface DirectionDestinationSpec: TypedDestinationSpec<Unit>, Direction {

    override fun invoke(navArgs: Unit): Direction = this

    override fun argsFrom(bundle: Bundle?) = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle) = Unit
}

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