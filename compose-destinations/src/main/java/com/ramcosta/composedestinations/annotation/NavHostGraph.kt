package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.animations.defaults.NoTransitions
import com.ramcosta.composedestinations.annotation.paramtypes.CodeGenVisibility
import kotlin.reflect.KClass

/**
 * Like [NavGraph] but denotes a top level nav graph, i.e one that is not nested in any other
 * nav graph (aka it doesn't have a parent).
 * These are used to pass to [com.ramcosta.composedestinations.DestinationsNavHost] call.
 *
 * [RootNavGraph] is one such graph that can be used out of the box.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class NavHostGraph(
    val default: Boolean = false,
    val defaultTransitions: KClass<out NavHostAnimatedDestinationStyle> = NoTransitions::class,
    val route: String = NavGraph.ANNOTATION_NAME,
    val visibility: CodeGenVisibility = CodeGenVisibility.PUBLIC
)

/**
 * Navigation graph annotation that will, by default, correspond to all Destinations that
 * don't specify a navigation graph.
 * If you're using it (i.e, you're not defining your own "NavGraph" annotation with `default = true`),
 * then you must annotate the start destination (or nav graph) with `@RootNavGraph(start = true)`. // TODO RACOSTA not needed anymore, since we can use start = true in Destination itself
 */
@NavHostGraph(default = true)
annotation class RootNavGraph(
    val start: Boolean = false
)