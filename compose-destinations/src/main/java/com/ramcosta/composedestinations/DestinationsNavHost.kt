package com.ramcosta.composedestinations

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

/**
 * Adds all destinations of the [navGraphSpec] to this
 * [NavGraphBuilder] as well as all nested nav graphs.
 */
fun NavGraphBuilder.addNavGraphDestinations(
    navGraphSpec: NavGraphSpec,
    navController: NavHostController,
    addComposable: NavGraphBuilder.(DestinationSpec) -> Unit,
    addNavigation: NavGraphBuilder.(NavGraphSpec, NavGraphBuilder.() -> Unit) -> Unit
) {
    navGraphSpec.destinations.values.forEach { destination ->
        addComposable(destination)
    }

    addNestedNavGraphs(navGraphSpec.nestedNavGraphs, navController/*, situationalParametersProvider*/, addComposable, addNavigation)
}

private fun NavGraphBuilder.addNestedNavGraphs(
    nestedNavGraphs: List<NavGraphSpec>,
    navController: NavHostController,
    addComposable: NavGraphBuilder.(DestinationSpec) -> Unit,
    addNavigation: NavGraphBuilder.(NavGraphSpec, NavGraphBuilder.() -> Unit) -> Unit
) {
    nestedNavGraphs.forEach { nestedGraph ->
        addNavigation(nestedGraph) {
            nestedGraph.destinations.forEach {
                addComposable(it.value)
            }

            addNestedNavGraphs(
                nestedGraph.nestedNavGraphs,
                navController/*, situationalParametersProvider*/,
                addComposable,
                addNavigation
            )
        }
    }
}