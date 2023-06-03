package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.animations.defaults.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.animations.defaults.NoTransitions
import com.ramcosta.composedestinations.spec.DestinationStyle
import kotlin.reflect.KClass

/**
 * Annotation you can use on annotation classes that will signal
 * the generating task to consider annotated Composable a part of the
 * corresponding navigation graph.
 *
 * Example:
 * ```
 * @RootNavGraph // sets SettingsNavGraph as a nested nav graph of RootNavGraph
 * @NavGraph // marks SettingsNavGraph as a NavGraph annotation
 * annotation class SettingsNavGraph(
 *     val start: Boolean = false
 * )
 *
 * @SettingsNavGraph(start = true)
 * @Destination
 * @Composable
 * fun SettingsScreen() { /*...*/ }
 * ```
 *
 * The above annotation class defines a Navigation graph of name "settings" and a
 * "Settings destination" that will belong to it (and it will be its start destination).
 *
 * You can include another NavGraph annotation for the parent Navigation graph, if this
 * is to be a nested navigation graph.
 * In the above example, since "SettingsNavGraph" is annotated with "RootNavGraph",
 * "settings" will be a nested navigation graph of "root". You can also
 * do, for example, `@RootNavGraph(start = true)` if "settings" is should be the start of "root".
 *
 * If you don't include the annotation of a parent navigation graph, then this will
 * be a top level navigation graph (ideal to pass to a `DestinationsNavHost` call).
 *
 * Annotation classes annotated with this *MUST* have a single parameter named "start"
 * with a default value of "false". This is enforced at compile time by the KSP task.
 *
 * @param defaultTransitions defines the navigation animations that destinations of this navigation graph
 * use when entering/leaving the screen. These animations will only be used on destinations
 * that do not set any specific style with [com.ramcosta.composedestinations.annotation.Destination.style]
 * parameter.
 * @param route unique id name of the nav graph used to register it in the `DestinationsNavHost`.
 * By default the name of the annotation class will be used removing "NavGraph" (case insensitive)
 * and changing it to snake case.
 * @param default pass true, if you want all Destination annotated Composables that are not
 * annotated with a "NavGraph" annotation to be considered as part of this navigation graph.
 * Basically, it will replace the default `@RootNavGraph` that usually takes this role.
 * You will still need to use it once in the start destination (or nav graph), like
 * `@YourNavGraph(start = true)`.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class NavGraph(
    val defaultTransitions: KClass<out DestinationStyle.Animated> = Nothing::class,
    val graphArgs: KClass<out StartRouteArgs<*>> = Nothing::class,
    val deepLinks: Array<DeepLink> = [],
    val route: String = ANNOTATION_NAME,
    val default: Boolean = false
) {
    companion object {
        private const val ANNOTATION_NAME = "@ramcosta.destinations.annotation-navgraph-route@"
    }
}

interface StartRouteArgs<T> {
    val startRouteArgs: T
}

interface StartRouteNoArgs: StartRouteArgs<Unit>  {
    override val startRouteArgs: Unit get() = Unit
}

/**
 * Like [NavGraph] but denotes a top level nav graph, i.e one that is not nested in any other
 * nav graph (aka it doesn't have a parent).
 * These are used to pass to [com.ramcosta.composedestinations.DestinationsNavHost] call.
 *
 * [RootNavGraph] is one such graph that can be used out of the box.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class NavHostGraph(
    val defaultTransitions: KClass<out NavHostAnimatedDestinationStyle>,
    val route: String = ANNOTATION_NAME,
    val default: Boolean = false
) {
    companion object {
        private const val ANNOTATION_NAME = "@ramcosta.destinations.annotation-navgraph-route@"
    }
}

/**
 * Navigation graph annotation that will, by default, correspond to all Destinations that
 * don't specify a navigation graph.
 * If you're using it (i.e, you're not defining your own "NavGraph" annotation with `default = true`),
 * then you must annotate the start destination (or nav graph) with `@RootNavGraph(start = true)`.
 */
@NavHostGraph(
    defaultTransitions = NoTransitions::class,
    default = true
)
annotation class RootNavGraph(
    val start: Boolean = false
)