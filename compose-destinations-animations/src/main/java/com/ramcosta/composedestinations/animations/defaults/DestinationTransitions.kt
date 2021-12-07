package com.ramcosta.composedestinations.animations.defaults

import androidx.compose.animation.*
import androidx.navigation.NavBackStackEntry

fun interface DestinationEnterTransition {
    @ExperimentalAnimationApi
    fun AnimatedContentScope<NavBackStackEntry>.enter() : EnterTransition
}

fun interface DestinationExitTransition {
    @ExperimentalAnimationApi
    fun AnimatedContentScope<NavBackStackEntry>.exit() : ExitTransition
}
