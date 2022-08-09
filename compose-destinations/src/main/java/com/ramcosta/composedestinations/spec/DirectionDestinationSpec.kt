package com.ramcosta.composedestinations.spec

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry

/**
 * [TypedDestinationSpec] that does not contain any navigation arguments.
 * It itself is a [Direction]
 */
interface DirectionDestinationSpec: TypedDestinationSpec<Unit>, Direction {

    override fun invoke(navArgs: Unit): Direction = this

    override fun argsFrom(navBackStackEntry: NavBackStackEntry) = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle) = Unit
}