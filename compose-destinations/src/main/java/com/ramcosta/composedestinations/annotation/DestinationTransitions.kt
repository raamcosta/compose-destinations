package com.ramcosta.composedestinations.annotation

/**
 * Annotation to be used in a object that implements `DestinationTransitionsSpec`
 * that will be generated if you're using accompanist navigation animations dependency.
 *
 * @property route the route of the destination these animations are related to.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class DestinationTransitions(
    val route: String
)