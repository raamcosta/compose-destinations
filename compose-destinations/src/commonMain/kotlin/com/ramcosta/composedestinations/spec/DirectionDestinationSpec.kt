package com.ramcosta.composedestinations.spec

import androidx.core.bundle.Bundle
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
