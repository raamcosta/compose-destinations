package com.ramcosta.composedestinations.spec

import com.ramcosta.composedestinations.animations.defaults.NavHostAnimatedDestinationStyle

/**
 * Like [DirectionNavGraphSpec] but used specifically for top level navigation graphs (i.e they
 * have no parent graph) that are meant to pass to [com.ramcosta.composedestinations.DestinationsNavHost] call.
 */
interface NavHostGraphSpec : DirectionNavGraphSpec {

    /**
     * Like [TypedNavGraphSpec.defaultTransitions] but not nullable since NavHost level
     * graphs must have animations defined (even if they are defined as "No animations")
     */
    override val defaultTransitions: NavHostAnimatedDestinationStyle
}