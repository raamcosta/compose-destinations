package com.ramcosta.composedestinations.utils

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.spec.NavGraphSpec

/**
 * Register with a [NavGraphSpecHolder] for each top level route passed in in a `DestinationsNavHost`.
 */
internal object NavGraphRegistry {

    private val holderByTopLevelRoute = mutableMapOf<String, NavGraphSpecHolder>()

    fun addGraph(navGraph: NavGraphSpec) {
        if (holderByTopLevelRoute.containsKey(navGraph.route)) {
            error("Calling multiple DestinationsNavHost with a navigation graph containing the same route ('${navGraph.route}')")
        }

        holderByTopLevelRoute[navGraph.route] = NavGraphSpecHolder().apply {
            addGraph(navGraph)
        }
    }

    fun removeGraph(navGraph: NavGraphSpec) {
        holderByTopLevelRoute.remove(navGraph.route)
    }

    operator fun get(topLevelRoute: String): NavGraphSpecHolder? {
        return holderByTopLevelRoute[topLevelRoute]
    }

}

/**
 * A class that holds [NavGraphSpec]s by their routes for a given `DestinationsNavHost` call.
 */
internal class NavGraphSpecHolder {

    private val navGraphSpecsByRoute: MutableMap<String, NavGraphSpec> = mutableMapOf()

    fun addGraph(navGraph: NavGraphSpec) {
        addUnique(navGraph)
        navGraph.nestedNavGraphs.forEach {
            addGraph(it)
        }
    }

    fun topLevelNavGraph(navController: NavController): NavGraphSpec? {
        return navGraphSpecsByRoute[navController.graph.route!!]
    }

    fun closestNavGraph(navBackStackEntry: NavBackStackEntry): NavGraphSpec? {
        return navGraphSpecsByRoute[navBackStackEntry.destination.parent?.route!!]
    }

    private fun addUnique(navGraph: NavGraphSpec) {
        val previousValue = navGraphSpecsByRoute.put(navGraph.route, navGraph)

        require(previousValue == null || previousValue === navGraph) {
            "Registering multiple navigation graphs with same route ('${navGraph.route}') is not allowed."
        }
    }
}