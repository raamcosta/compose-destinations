package com.ramcosta.composedestinations.spec

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle

/**
 * [DestinationSpec] that does not contain any navigation arguments.
 * It itself is a [Direction]
 */
interface DirectionDestinationSpec: DestinationSpec<Unit>, Direction {

    override fun invoke(navArgs: Unit): Direction = this

    override fun argsFrom(bundle: Bundle?) = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle) = Unit

    override fun toBundle(navArgs: Unit): Bundle = Bundle()
}

/**
 * [ActivityDestinationSpec] that does not contain any navigation arguments.
 * It itself is a [Direction]
 */
interface DirectionActivityDestinationSpec: ActivityDestinationSpec<Unit>, DirectionDestinationSpec {

    override fun argsFrom(savedStateHandle: SavedStateHandle) = Unit

    override fun toBundle(navArgs: Unit): Bundle = Bundle()
}