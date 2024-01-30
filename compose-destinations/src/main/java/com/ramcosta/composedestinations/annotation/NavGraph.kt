package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.annotation.paramtypes.CodeGenVisibility
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
 *     val start: Boolean = false //TODO RACOSTA
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
 * @param start TODO RACOSTA
 * @param navArgs class with a primary constructor where all navigation arguments specific
 * to this navigation graph are to be defined. Note that these nav arguments will be available on
 * the start destination by using `argsFrom` function of the generated Navigation graph.
 * To navigate to this navigation graph, you'll need both this and the start destination's navigation
 * arguments.
 * @param deepLinks array of [DeepLink] which can be used to navigate to this navigation graph
 * @param defaultTransitions defines the navigation animations that destinations of this navigation graph
 * use when entering/leaving the screen. These animations will only be used on destinations
 * that do not set any specific style with [com.ramcosta.composedestinations.annotation.Destination.style]
 * parameter.
 * @param route unique id name of the nav graph used to register it in the `DestinationsNavHost`.
 * By default the name of the annotation class will be used removing "NavGraph" (case insensitive)
 * and changing it to snake case.
 * @param visibility [CodeGenVisibility] of the corresponding generated NavGraph object.
 * Useful to control what the current module exposes to other modules. By default, it is public.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class NavGraph<T: Annotation>(
    val start: Boolean = false,
    val navArgs: KClass<*> = Nothing::class,
    val deepLinks: Array<DeepLink> = [],
    val defaultTransitions: KClass<out DestinationStyle.Animated> = Nothing::class,
    val route: String = ANNOTATION_NAME,
    val visibility: CodeGenVisibility = CodeGenVisibility.PUBLIC
) {
    companion object {
        internal const val ANNOTATION_NAME = "@ramcosta.destinations.annotation-navgraph-route@"
    }
}

/**
 * TODO RACOSTA
 */
@Retention(AnnotationRetention.SOURCE)
annotation class NoParent
