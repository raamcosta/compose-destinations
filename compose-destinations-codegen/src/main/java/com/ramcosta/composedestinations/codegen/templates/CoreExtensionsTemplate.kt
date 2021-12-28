package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.*

const val START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR = "[START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR]"
const val END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR = "[END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR]"
const val START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR = "[START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR]"
const val END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR = "[END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR]"

val coreExtensionsTemplate = """
package $PACKAGE_NAME

import androidx.navigation.NavBackStackEntry
import $PACKAGE_NAME.destinations.*
import $PACKAGE_NAME.spec.NavGraphSpec
import $PACKAGE_NAME.utils.findDestination
$ADDITIONAL_IMPORTS

/**
 * Realization of [$CORE_NAV_GRAPH_SPEC] for the app.
 * It uses [$GENERATED_DESTINATION] instead of [$CORE_DESTINATION_SPEC].
 * 
 * @see [$CORE_NAV_GRAPH_SPEC]
 */
data class $GENERATED_NAV_GRAPH(
    override val route: String,
    override val startDestination: Destination,
    val destinations: List<Destination>,
    override val nestedNavGraphs: List<$GENERATED_NAV_GRAPH> = emptyList()
): $CORE_NAV_GRAPH_SPEC {
    override val destinationsByRoute: Map<String, Destination> = destinations.associateBy { it.route }
}
$START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR
/**
 * Finds the destination correspondent to this [NavBackStackEntry] in the root NavGraph, null if none is found
 * or if no route is set in this back stack entry's destination.
 */
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}val NavBackStackEntry.navDestination: Destination?
    get() {
        return navDestination()
    }
$END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR
/**
 * Finds the destination correspondent to this [NavBackStackEntry] in [navGraph], null if none is found
 * or if no route is set in this back stack entry's destination.
 */
${REQUIRE_OPT_IN_ANNOTATIONS_PLACEHOLDER}fun NavBackStackEntry.navDestination(navGraph: $GENERATED_NAV_GRAPH$START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR = $GENERATED_NAV_GRAPHS_OBJECT.root$END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR): Destination? {
    return destination.route?.let { navGraph.findDestination(it) as Destination }
}
""".trimIndent()