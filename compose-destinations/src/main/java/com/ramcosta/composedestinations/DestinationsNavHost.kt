package com.ramcosta.composedestinations

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.spec.NavHostEngine
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec


/**
 * Like [androidx.navigation.compose.NavHost] but includes the destinations of [navGraph].
 * Composables annotated with `@Destination` will belong to a [NavGraphSpec] inside `NavGraphs`
 * generated file.
 *
 * @see [androidx.navigation.compose.NavHost]
 *
 * @param modifier [Modifier]
 * @param navGraph [NavGraphSpec] to use
 * @param startDestination the start destination to use
 * @param navController [NavHostController]
 * @param dependenciesContainerBuilder lambda invoked when a destination gets navigated to. It allows
 * the caller to contribute certain dependencies that the destination can use.
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
        navGraph = navGraph,
        startDestination = startDestination,
        navController = navController,
        dependenciesContainerBuilder = dependenciesContainerBuilder,
    ) {
        addNavGraphDestinations(
            engine = engine,
            navGraphSpec = navGraph,
            navController = navController,
            dependenciesContainerBuilder = dependenciesContainerBuilder,
        )
    }
}