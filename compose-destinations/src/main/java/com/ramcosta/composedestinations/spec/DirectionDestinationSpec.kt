package com.ramcosta.composedestinations.spec

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry

/**
 * [DestinationSpec] that does not contain any navigation arguments.
 * It itself is a [Direction]
 */
interface DirectionDestinationSpec: DestinationSpec<Unit>, Direction {

    override fun argsFrom(navBackStackEntry: NavBackStackEntry) = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle) = Unit
}