package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.*

const val START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR = "[START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR]"
const val END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR = "[END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR]"
const val START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR = "[START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR]"
const val END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR = "[END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR]"

val coreExtensionsTemplate = """
package $PACKAGE_NAME

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavBackStackEntry
import $PACKAGE_NAME.spec.DestinationSpec
import $PACKAGE_NAME.spec.NavGraphSpec
import $PACKAGE_NAME.spec.Routed
import $PACKAGE_NAME.utils.findDestination

/**
 * Handy typealias of [$GENERATED_DESTINATION] when you don't
 * care about the generic type (probably most cases for app's use)
 */
typealias Destination = $GENERATED_DESTINATION<*>

/**
 * $GENERATED_DESTINATION is a sealed version of [$CORE_DESTINATION_SPEC]
 */
sealed interface $GENERATED_DESTINATION<T>: $CORE_DESTINATION_SPEC<T>

/**
 * Interface for all $GENERATED_DESTINATION with no navigation arguments
 */
sealed interface $GENERATED_NO_ARGS_DESTINATION: $GENERATED_DESTINATION<Unit>, Routed {
    
    override fun argsFrom(navBackStackEntry: NavBackStackEntry) = Unit

    override fun argsFrom(savedStateHandle: SavedStateHandle) = Unit
}

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
    override val destinationsByRoute = destinations.associateBy { it.route }
}
$START_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR
/**
 * Finds the destination correspondent to this [NavBackStackEntry] in the root NavGraph, null if none is found
 * or if no route is set in this back stack entry's destination.
 */
val NavBackStackEntry.navDestination: Destination?
    get() {
        return navDestination()
    }
$END_NO_NAV_GRAPHS_NAV_DESTINATION_ANCHOR
/**
 * Finds the destination correspondent to this [NavBackStackEntry] in [navGraph], null if none is found
 * or if no route is set in this back stack entry's destination.
 */
fun NavBackStackEntry.navDestination(navGraph: $GENERATED_NAV_GRAPH$START_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR = $GENERATED_NAV_GRAPHS_OBJECT.root$END_NAV_DESTINATION_ROOT_DEFAULT_ANCHOR): Destination? {
    return destination.route?.let { navGraph.findDestination(it) as Destination }
}
""".trimIndent()