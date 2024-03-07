package com.ramcosta.composedestinations.animations.defaults

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.navigation.NavBackStackEntry

fun interface DestinationEnterTransition {
    fun AnimatedContentTransitionScope<NavBackStackEntry>.enter() : EnterTransition
}

fun interface DestinationExitTransition {
    fun AnimatedContentTransitionScope<NavBackStackEntry>.exit() : ExitTransition
}

fun interface DestinationSizeTransform {
    fun AnimatedContentTransitionScope<NavBackStackEntry>.sizeTransform() : SizeTransform?
}
