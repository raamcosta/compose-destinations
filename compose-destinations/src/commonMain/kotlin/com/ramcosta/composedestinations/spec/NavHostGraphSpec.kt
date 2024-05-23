package com.ramcosta.composedestinations.spec

import androidx.core.bundle.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle

typealias NavHostGraphSpec = TypedNavHostGraphSpec<*>

/**
 * Like [DirectionNavGraphSpec] but used specifically for top level navigation graphs (i.e they
 * have no parent graph) that are meant to pass to [com.ramcosta.composedestinations.DestinationsNavHost] call.
 */
interface TypedNavHostGraphSpec<START_ROUTE_NAV_ARGS>: TypedNavGraphSpec<START_ROUTE_NAV_ARGS, START_ROUTE_NAV_ARGS> {

    override val baseRoute: String get() = route

    override fun invoke(navArgs: START_ROUTE_NAV_ARGS): Direction {
        //args cannot have mandatory args on start routes of NavHostGraphs, so this is ok
        return Direction(route)
    }

    /**
     * Like [TypedNavGraphSpec.defaultTransitions] but not nullable since NavHost level
     * graphs must have animations defined (even if they are defined as "No animations")
     */
    override val defaultTransitions: NavHostAnimatedDestinationStyle
}

interface DirectionNavHostGraphSpec : TypedNavHostGraphSpec<Unit>, Direction {

    override val baseRoute: String get() = route

    override fun invoke(navArgs: Unit): Direction = this

    operator fun invoke(): Direction = this

    override fun argsFrom(bundle: Bundle?) = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle) = Unit
}