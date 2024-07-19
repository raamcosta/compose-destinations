package com.ramcosta.composedestinations.spec

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.annotation.internal.InternalDestinationsApi

typealias NavHostGraphSpec = TypedNavHostGraphSpec<*>

/**
 * Like [DirectionNavGraphSpec] but used specifically for top level navigation graphs (i.e they
 * have no parent graph) that are meant to pass to [com.ramcosta.composedestinations.DestinationsNavHost] call.
 */
interface TypedNavHostGraphSpec<START_ROUTE_NAV_ARGS>: TypedNavGraphSpec<START_ROUTE_NAV_ARGS, START_ROUTE_NAV_ARGS> {

    /**
     * Like [TypedNavGraphSpec.defaultTransitions] but not nullable since NavHost level
     * graphs must have animations defined (even if they are defined as "No animations")
     */
    override val defaultTransitions: NavHostAnimatedDestinationStyle

    val defaultStartArgs: START_ROUTE_NAV_ARGS? get() = null

    val defaultStartDirection: Direction

    override val baseRoute: String get() = route

    override fun invoke(navArgs: START_ROUTE_NAV_ARGS): Direction {
        //args cannot have mandatory args on start routes of NavHostGraphs, so this is ok
        return Direction(baseRoute)
    }
}

interface DirectionNavHostGraphSpec : TypedNavHostGraphSpec<Unit>, Direction {

    override val defaultStartArgs get() = Unit

    override val baseRoute: String get() = route

    override fun invoke(navArgs: Unit): Direction = this

    operator fun invoke(): Direction = this

    override fun argsFrom(bundle: Bundle?) = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle) = Unit
}

// to be used by generated code only
@InternalDestinationsApi
fun <T> TypedNavHostGraphSpec<T>.defaultStartDirection(): Direction {
    val args = defaultStartArgs
    return if (args != null) {
        startRoute.invoke(args)
    } else {
        // only possible if start doesn't have any mandatory args (enforced by code generation)
        //
        // also I'm using baseRoute instead of route because if it has
        // non mandatory String args, then it can lead to weird arg values like "{argName}"
        Direction(startRoute.baseRoute)
    }
}