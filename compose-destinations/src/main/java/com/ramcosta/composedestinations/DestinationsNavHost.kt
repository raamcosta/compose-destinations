package com.ramcosta.composedestinations

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.NavHostEngine


/**
 * Like [androidx.navigation.compose.NavHost] but includes the destinations of [navGraph].
 * Composables annotated with `@Destination` will belong to a [NavGraphSpec] inside `NavGraphs`
 * generated file.
 *
 * @see [androidx.navigation.compose.NavHost]
 *
 * @param modifier [Modifier] to apply to this Composable
 * @param engine [NavHostEngine] to use. If you are not using animation features
 * (which need "io.github.raamcosta.compose-destinations:animations-core" dependency), you don't
 * need to explicitly pass in anything, since the default engine will be used.
 * If using animation features, then you should pass the [NavHostEngine] returned by
 * `rememberAnimatedNavHostEngine` function.
 * @param navController [NavHostController] that can be used to navigate between this NavHost's destinations.
 * @param navGraph [NavGraphSpec] to use the [DestinationSpec]s from.
 * @param startDestination the start destination of the NavHost
 * @param dependenciesContainerBuilder lambda invoked when a destination gets navigated to. It allows
 * the caller to contribute with dependencies that the destination can use.
 */
@Composable
fun DestinationsNavHost(
    modifier: Modifier = Modifier,
    engine: NavHostEngine = rememberNavHostEngine(),
    navController: NavHostController = engine.rememberNavController(),
    navGraph: NavGraphSpec,
    startDestination: DestinationSpec = navGraph.startDestination,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.(NavBackStackEntry) -> Unit = {}
) {
    engine.NavHost(
        modifier = modifier,
        route = navGraph.route,
        startDestination = startDestination,
        navController = navController,
    ) {
        addNavGraphDestinations(
            engine = engine,
            navGraphSpec = navGraph,
            navController = navController,
            dependenciesContainerBuilder = dependenciesContainerBuilder,
        )
    }
}

private fun NavGraphBuilder.addNavGraphDestinations(
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