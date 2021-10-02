package com.ramcosta.composedestinations

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import kotlin.reflect.KClass

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
    situationalParametersProvider: (DestinationSpec) -> Map<KClass<*>, Any> = { emptyMap() },
    builder: NavGraphBuilder.() -> Unit = {}
) {

    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
        route = navGraph.route
    ) {
        addNavGraphDestinations(navGraph, navController, situationalParametersProvider)

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
    situationalParametersProvider: (DestinationSpec) -> Map<KClass<*>, Any>,
) {
    navGraphSpec.destinations.values.forEach { destination ->
        addDestination(destination, navController, situationalParametersProvider(destination))
    }

    addNestedNavGraphs(navGraphSpec.nestedNavGraphs, navController, situationalParametersProvider)
}

private fun NavGraphBuilder.addNestedNavGraphs(
    nestedNavGraphs: List<NavGraphSpec>,
    navController: NavHostController,
    situationalParametersProvider: (DestinationSpec) -> Map<KClass<*>, Any>,
) {
    nestedNavGraphs.forEach { nestedGraph ->
        navigation(nestedGraph.startDestination.route, nestedGraph.route) {
            nestedGraph.destinations.forEach {
                addDestination(it.value, navController, situationalParametersProvider(it.value))
            }

            addNestedNavGraphs(nestedGraph.nestedNavGraphs, navController, situationalParametersProvider)
        }
    }
}

private fun NavGraphBuilder.addDestination(
    destination: DestinationSpec,
    navController: NavHostController,
    situationalParameters: Map<KClass<*>, Any>
) {
    composable(
        route = destination.route,
        content = { destination.Content(navController, it, situationalParameters) },
        arguments = destination.arguments,
        deepLinks = destination.deepLinks
    )
}