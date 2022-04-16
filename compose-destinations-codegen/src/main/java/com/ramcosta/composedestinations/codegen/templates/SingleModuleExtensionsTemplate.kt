package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.codeGenBasePackageName
import com.ramcosta.composedestinations.codegen.commons.*
import com.ramcosta.composedestinations.codegen.codeGenDestination

const val START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR = "[START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR]"
const val END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR = "[END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR]"
const val START_NAV_DESTINATION_DEPRECATED_ROOT_DEFAULT_ANCHOR = "[START_NAV_DESTINATION_DEPRECATED_ROOT_DEFAULT_ANCHOR]"
const val END_NAV_DESTINATION_DEPRECATED_ROOT_DEFAULT_ANCHOR = "[END_NAV_DESTINATION_DEPRECATED_ROOT_DEFAULT_ANCHOR]"
const val START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR = "[START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR]"
const val END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR = "[END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR]"

val singleModuleExtensionsTemplate = """
package $codeGenBasePackageName

import androidx.navigation.NavBackStackEntry
import $codeGenBasePackageName.destinations.*
import $CORE_PACKAGE_NAME.spec.*
import $CORE_PACKAGE_NAME.utils.startDestination
import $CORE_PACKAGE_NAME.utils.destination$ADDITIONAL_IMPORTS

/**
 * Realization of [$CORE_NAV_GRAPH_SPEC] for the app.
 * It uses [$codeGenDestination] instead of [$CORE_DESTINATION_SPEC].
 * 
 * @see [$CORE_NAV_GRAPH_SPEC]
 */
data class $GENERATED_NAV_GRAPH(
    override val route: String,
    override val startRoute: Route,
    val destinations: List<$typeAliasDestination>,
    override val nestedNavGraphs: List<$GENERATED_NAV_GRAPH> = emptyList()
): $CORE_NAV_GRAPH_SPEC {
    override val destinationsByRoute: Map<String, $typeAliasDestination> = destinations.associateBy { it.route }
}

/**
 * If this [Route] is a [$typeAliasDestination], returns it
 *
 * If this [Route] is a [$GENERATED_NAV_GRAPH], returns its
 * start [$typeAliasDestination].
 */
val Route.startAppDestination: $typeAliasDestination
    get() = startDestination as $typeAliasDestination

/**
 * If this [Route] is a [$typeAliasDestination], returns it
 *
 * If this [Route] is a [$GENERATED_NAV_GRAPH], returns its
 * start [$typeAliasDestination].
 */
@Deprecated(
    message = "Api will be removed! Use `startAppDestination` instead.",
    replaceWith = ReplaceWith("startAppDestination")
)
val Route.startDestination: $typeAliasDestination
    get() = startDestination as $typeAliasDestination
$START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR
/**
 * Finds the destination correspondent to this [NavBackStackEntry] in the root NavGraph, null if none is found
 * or if no route is set in this back stack entry's destination.
 */
@Deprecated(
    message = "Api will be removed! Use `appDestination()` instead.",
    replaceWith = ReplaceWith("appDestination()")
)
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}val NavBackStackEntry.navDestination: $typeAliasDestination?
    get() = appDestination()
$END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR
/**
 * Finds the destination correspondent to this [NavBackStackEntry] in [navGraph], null if none is found
 * or if no route is set in this back stack entry's destination.
 */
@Deprecated(
    message = "Api will be removed! Use `appDestination()` instead.",
    replaceWith = ReplaceWith("appDestination")
)
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}fun NavBackStackEntry.navDestination(navGraph: $GENERATED_NAV_GRAPH$START_NAV_DESTINATION_DEPRECATED_ROOT_DEFAULT_ANCHOR = $GENERATED_NAV_GRAPHS_OBJECT.root$END_NAV_DESTINATION_DEPRECATED_ROOT_DEFAULT_ANCHOR): $typeAliasDestination? {
    return destination(navGraph) as $typeAliasDestination
}

/**
 * Finds the destination correspondent to this [NavBackStackEntry] in [navGraph], null if none is found
 * or if no route is set in this back stack entry's destination.
 */
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}fun NavBackStackEntry.appDestination(navGraph: $GENERATED_NAV_GRAPH$START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR = $GENERATED_NAV_GRAPHS_OBJECT.root$END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR): $typeAliasDestination? {
    return destination(navGraph) as $typeAliasDestination
}
""".trimIndent()