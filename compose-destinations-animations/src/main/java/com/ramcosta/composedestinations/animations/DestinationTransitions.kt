package com.ramcosta.composedestinations.animations

import androidx.compose.animation.*
import androidx.navigation.NavBackStackEntry

fun interface DestinationEnterTransition {
    @ExperimentalAnimationApi
    fun AnimatedContentScope<String>.enter(initial: NavBackStackEntry, target: NavBackStackEntry) : EnterTransition
}

fun interface DestinationExitTransition {
    @ExperimentalAnimationApi
    fun AnimatedContentScope<String>.exit(initial: NavBackStackEntry, target: NavBackStackEntry) : ExitTransition
}
