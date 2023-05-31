package com.ramcosta.composedestinations.animations.defaults

import androidx.compose.animation.*
import androidx.navigation.NavBackStackEntry

fun interface DestinationEnterTransition {
    fun AnimatedContentTransitionScope<NavBackStackEntry>.enter() : EnterTransition
}

fun interface DestinationExitTransition {
    fun AnimatedContentTransitionScope<NavBackStackEntry>.exit() : ExitTransition
}
