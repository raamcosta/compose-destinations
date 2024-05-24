package com.ramcosta.composedestinations.spec

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle

/**
 * [TypedNavGraphSpec] that does not contain any navigation arguments.
 * It itself is a [Direction]
 */
interface DirectionNavGraphSpec: TypedNavGraphSpec<Unit, Unit>, Direction {

    override val baseRoute: String get() = route

    override fun invoke(navArgs: Unit): Direction = this

    operator fun invoke(): Direction = this

    override fun argsFrom(bundle: Bundle?) = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle) = Unit
}
