package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.animations.defaults.NoTransitions
import com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility
import kotlin.reflect.KClass

/**
 * Like [NavGraph] but denotes a top level nav graph, i.e one that is not nested in any other
 * nav graph (aka it doesn't have a parent).
 * These are used to pass to [com.ramcosta.composedestinations.DestinationsNavHost] call.
 *
 * [RootGraph] is one such graph that can be used out of the box.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class NavHostGraph(
    val defaultTransitions: KClass<out NavHostAnimatedDestinationStyle> = NoTransitions::class,
    val route: String = NavGraph.ANNOTATION_NAME,
    val visibility: CodeGenVisibility = CodeGenVisibility.PUBLIC
)

/**
 * Out the box [NavHostGraph] that can be used in destinations or graphs if there
 * isn't a need to specify one of the parameters of [NavHostGraph].
 */
@NavHostGraph
annotation class RootGraph