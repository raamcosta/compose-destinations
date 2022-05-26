package com.ramcosta.composedestinations.utils

import android.os.Handler
import android.os.Looper
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.spec.NavGraphSpec

/**
 * Register with a [NavGraphSpecHolder] for each top level route passed in in a `DestinationsNavHost`.
 */
internal object NavGraphRegistry {

    private val holderByTopLevelRoute = mutableMapOf<String, NavGraphSpecHolder>()
    private val uniqueCheckRoutes = mutableMapOf<String, Int>()

    private val handler = Handler(Looper.getMainLooper())
    private val runnablesByNavGraph = mutableMapOf<NavGraphSpec, UniqueRouteCheckRunnable>()

    fun addGraph(navGraph: NavGraphSpec) {
        if (holderByTopLevelRoute.containsKey(navGraph.route)) {
            return
        }

        holderByTopLevelRoute[navGraph.route] = NavGraphSpecHolder().apply {
            addGraph(navGraph)
        }
    }

    operator fun get(topLevelRoute: String): NavGraphSpecHolder? {
        return holderByTopLevelRoute[topLevelRoute]
    }

    // region uniqueness logic

    /**
     * We let all additions and removals of a NavGraphSpec happen freely. After 3 seconds
     * we check how many NavHosts we have for a given top level route.
     *
     * Since the DisposableEffect onDispose runs after the call for the new one,
     * we also reset the timer to make sure we always wait after the last addition to see
     * if we're gonna have a matching removal.
     */
    fun checkUniqueness(navGraph: NavGraphSpec) {
        runnablesByNavGraph[navGraph]?.let {
            handler.removeCallbacks(it)
            handler.postDelayed(it, 3000)
            return
        }

        val routeRunnable = UniqueRouteCheckRunnable(navGraph)
        runnablesByNavGraph[navGraph] = routeRunnable
        handler.postDelayed(routeRunnable, 3000)
    }

    fun addGraphForUniquenessCheck(navGraph: NavGraphSpec) {
        uniqueCheckRoutes[navGraph.route] = uniqueCheckRoutes.getOrElse(navGraph.route) { 0 } + 1
    }

    fun removeGraphForUniquenessCheck(navGraph: NavGraphSpec) {
        uniqueCheckRoutes[navGraph.route] = uniqueCheckRoutes.getOrElse(navGraph.route) { 0 } - 1
    }

    private class UniqueRouteCheckRunnable(val navGraph: NavGraphSpec) : Runnable {
        override fun run() {
            runnablesByNavGraph.remove(navGraph)
            if (uniqueCheckRoutes.getOrElse(navGraph.route) { 0 } > 1) {
                error("Calling multiple DestinationsNavHost with a navigation graph containing the same route ('${navGraph.route}')")
            }
        }
    }
    // endregion

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