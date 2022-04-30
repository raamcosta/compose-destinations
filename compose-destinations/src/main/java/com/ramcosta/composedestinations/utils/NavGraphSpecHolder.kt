package com.ramcosta.composedestinations.utils

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.ramcosta.composedestinations.spec.NavGraphSpec

internal object NavGraphSpecHolder {

    private val navGraphSpecsByRoute: MutableMap<String, NavGraphSpec> = mutableMapOf()

    fun addUnique(navGraphSpec: NavGraphSpec) {
        val previousValue = navGraphSpecsByRoute.put(navGraphSpec.route, navGraphSpec)

        require(previousValue == null || previousValue === navGraphSpec) {
            "Registering multiple navigation graphs with same route ('${navGraphSpec.route}') is not allowed."
        }
    }

    fun topLevelNavGraph(navController: NavController): NavGraphSpec? {
        return navGraphSpecsByRoute[navController.graph.route!!]
    }

    fun closestNavGraph(navBackStackEntry: NavBackStackEntry): NavGraphSpec? {
        return navGraphSpecsByRoute[navBackStackEntry.destination.parent?.route!!]
    }
}