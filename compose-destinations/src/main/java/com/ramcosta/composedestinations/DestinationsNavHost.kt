package com.ramcosta.composedestinations

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation

/**
 * Like [NavHost] but adds destinations to the nav graph, using
 * the properties of each [DestinationSpec] to do so.
 *
 * Also, it can be passed a [ScaffoldState] if it should be available
 * to each destination. Otherwise `null` can be used.
 * Recommendation is to pass in the [ScaffoldState] always, if this
 * is used inside a Scaffold.
 *
 * @see [NavHost]
 */
@Composable
fun DestinationsNavHost(
    navGraph: NavGraphSpec,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: DestinationSpec = navGraph.startDestination,
    scaffoldState: ScaffoldState?,
    builder: NavGraphBuilder.() -> Unit = {}
) {

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
        route = navGraph.route
    ) {
        addNavGraphDestinations(navGraph, navController, scaffoldState)

        builder()
    }
}

/**
 * Adds all destinations of the [navGraphSpec] to this
 * [NavGraphBuilder] as well as all nested nav graphs.
 */
fun NavGraphBuilder.addNavGraphDestinations(
    navGraphSpec: NavGraphSpec,
    navController: NavHostController,
    scaffoldState: ScaffoldState?
) {
    navGraphSpec.destinations.values.forEach { destination ->
        addDestination(destination, navController, scaffoldState)
    }

    addNestedNavGraphs(navGraphSpec.nestedNavGraphs, navController, scaffoldState)
}

private fun NavGraphBuilder.addNestedNavGraphs(
    nestedNavGraphs: List<NavGraphSpec>,
    navController: NavHostController,
    scaffoldState: ScaffoldState?
) {
    nestedNavGraphs.forEach { nestedGraph ->
        navigation(nestedGraph.startDestination.route, nestedGraph.route) {
            nestedGraph.destinations.forEach {
                addDestination(it.value, navController, scaffoldState)
            }

            addNestedNavGraphs(nestedGraph.nestedNavGraphs, navController, scaffoldState)
        }
    }
}

private fun NavGraphBuilder.addDestination(
    destination: DestinationSpec,
    navController: NavHostController,
    scaffoldState: ScaffoldState?
) {
    composable(
        route = destination.route,
        content = { destination.Content(navController, it, scaffoldState) },
        arguments = destination.arguments,
        deepLinks = destination.deepLinks
    )
}