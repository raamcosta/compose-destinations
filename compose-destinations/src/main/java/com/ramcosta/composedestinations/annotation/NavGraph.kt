package com.ramcosta.composedestinations.annotation

import com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility
import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.spec.DestinationStyle
import kotlin.reflect.KClass

/**
 * Annotation you can use on annotation classes that will signal
 * the generating task to consider annotated Composable a part of the
 * corresponding navigation graph.
 *
 * Example:
 * ```
 * @NavGraph<RootNavGraph> // marks SettingsNavGraph as a NavGraph annotation and RootNavGraph as parent
 * annotation class SettingsNavGraph
 *
 * @Destination<SettingsNavGraph>(start = true)
 * @Composable
 * fun SettingsScreen() { /*...*/ }
 * ```
 *
 * The above annotation class defines a Navigation graph of name "settings" and a
 * "Settings destination" that will belong to it (and it will be its start destination).
 *
 * In the above example, since "SettingsNavGraph" is annotated with "@NavGraph<RootNavGraph>",
 * "settings" will be a nested navigation graph of "root". You could also
 * do `"@NavGraph<RootNavGraph>(start = true)` if "settings" should be the start of "root".
 *
 * @param T type of the parent navigation graph the destination should belong to. Should be an
 * annotation annotated with [NavGraph] or [NavHostGraph], such as [RootGraph].
 * @param route unique id name of the nav graph used to register it in the `DestinationsNavHost`.
 * By default the name of the annotation class will be used removing "Graph" or "NavGraph" (case insensitive)
 * and changing it to snake case.
 * @param start whether this navigation graph will be the start of its parent navigation graph,
 * false by default.
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
 * @param visibility [CodeGenVisibility] of the corresponding generated NavGraph object.
 * Useful to control what the current module exposes to other modules. By default, it is public.
 */
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class NavGraph<T: Annotation>(
    val route: String = ANNOTATION_NAME,
    val start: Boolean = false,
    val navArgs: KClass<*> = Nothing::class,
    val deepLinks: Array<DeepLink> = [],
    val defaultTransitions: KClass<out DestinationStyle.Animated> = Nothing::class,
    val visibility: CodeGenVisibility = CodeGenVisibility.PUBLIC
) {
    companion object {
        internal const val ANNOTATION_NAME = "@ramcosta.destinations.annotation-navgraph-route@"
    }
}
