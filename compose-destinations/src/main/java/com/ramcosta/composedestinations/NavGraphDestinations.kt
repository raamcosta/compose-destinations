package com.ramcosta.composedestinations

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.NavHostEngine

/**
 * Adds all destinations of the [navGraphSpec] to this
 * [NavGraphBuilder] as well as all nested nav graphs.
 */
fun NavGraphBuilder.addNavGraphDestinations(
    engine: NavHostEngine,
    navGraphSpec: NavGraphSpec,
    navController: NavHostController,
    dependenciesContainerBuilder: @Composable (DependenciesContainerBuilder.(NavBackStackEntry) -> Unit),
): Unit = with(engine) {

    navGraphSpec.destinationsByRoute.values.forEach { destination ->
        composable(
            destination,
            navController,
            dependenciesContainerBuilder
        )
    }

   addNestedNavGraphs(
        engine = engine,
        nestedNavGraphs = navGraphSpec.nestedNavGraphs,
        navController = navController,
        dependenciesContainerBuilder = dependenciesContainerBuilder
    )
}

private fun NavGraphBuilder.addNestedNavGraphs(
    engine: NavHostEngine,
    nestedNavGraphs: List<NavGraphSpec>,
    navController: NavHostController,
    dependenciesContainerBuilder: @Composable (DependenciesContainerBuilder.(NavBackStackEntry) -> Unit),
): Unit = with(engine) {

    nestedNavGraphs.forEach { nestedGraph ->
        navigation(nestedGraph) {
            addNavGraphDestinations(
                engine = engine,
                navGraphSpec = nestedGraph,
                navController = navController,
                dependenciesContainerBuilder = dependenciesContainerBuilder,
            )
        }
    }
}

fun NavGraphBuilder.addDialogComposable(
    dialogStyle: DestinationStyle.Dialog,
    destination: DestinationSpec,
    navController: NavHostController,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.(NavBackStackEntry) -> Unit
) {
    dialog(
        destination.route,
        destination.arguments,
        destination.deepLinks,
        dialogStyle.properties
    ) { navBackStackEntry ->
        destination.Content(
            navController,
            navBackStackEntry
        ) { dependenciesContainerBuilder(navBackStackEntry) }
    }
}