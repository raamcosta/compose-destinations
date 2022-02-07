package com.ramcosta.composedestinations.spec

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry

/**
 * TODO racosta
 */
interface DirectionDestinationSpec: DestinationSpec<Unit>, Direction {

    override fun argsFrom(navBackStackEntry: NavBackStackEntry) = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle) = Unit
}