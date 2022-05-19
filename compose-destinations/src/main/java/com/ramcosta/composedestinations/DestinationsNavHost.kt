package com.ramcosta.composedestinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCalls
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.NavHostEngine
import com.ramcosta.composedestinations.spec.Route
import com.ramcosta.composedestinations.utils.NavGraphRegistry

/**
 * Like [androidx.navigation.compose.NavHost] but includes the destinations of [navGraph].
 * Composables annotated with `@Destination` will belong to a [NavGraphSpec] inside `NavGraphs`
 * generated file. You can also disable the `NavGraphs` automatic generation in build.gradle:
 * ```
 * ksp {
 *     arg("compose-destinations.generateNavGraphs", "false")
 * }
 * ```
 * This might be useful if you need more complex `NavGraphs` then what the usage of the annotation
 * can provide. If you do this, it is advisable that you create your `NavGraphs` file with your
 * navigation graphs in the form of [NavGraphSpec] implementations.
 *
 * @param navGraph [NavGraphSpec] to use the [DestinationSpec]s from and register the navigation graph.
 *
 * @param modifier [Modifier] to apply to this Composable
 *
 * @param startRoute the start destination of the NavHost. By default, we'll use the `startDestination`
 * of the [navGraph]. This makes it possible to override that default on runtime.
 *
 * @param engine [NavHostEngine] to use. If you are not using animation features
 * (which need "io.github.raamcosta.compose-destinations:animations-core" dependency), you don't
 * need to explicitly pass in anything, since the default engine will be used.
 * If using animation features, then you should pass the [NavHostEngine] returned by
 * `rememberAnimatedNavHostEngine` function.
 *
 * @param navController [NavHostController] that can be used to navigate between this NavHost's destinations.
 * If you need this outside the scope of this function, you should get it from [androidx.navigation.compose.rememberNavController]
 * or, if you're using animation feature, from [com.google.accompanist.navigation.animation.rememberAnimatedNavController].
 * Alternatively, you can also use [NavHostEngine.rememberNavController] that will internally call the correct remember function.
 *
 * @param dependenciesContainerBuilder offers a [DependenciesContainerBuilder] where you can add
 * dependencies by their class via [com.ramcosta.composedestinations.navigation.dependency].
 * The lambda will be called when a Composable screen gets navigated to and
 * [DependenciesContainerBuilder] also implements [com.ramcosta.composedestinations.scope.DestinationScope]
 * so you can access all information about what [DestinationSpec] is being navigated to.
 *
 * @param manualComposableCallsBuilder this will offer a [ManualComposableCallsBuilder] scope where you can
 * make manual calls to specific [DestinationSpec] Composables which belong to this [navGraph].
 * This can be useful if you have some specific case where you want to pass something to a specific screen
 * that would not work (Compose runtime related classes f.e) or would be awkward with [dependenciesContainerBuilder].
 */
@Composable
fun DestinationsNavHost(
    navGraph: NavGraphSpec,
    modifier: Modifier = Modifier,
    startRoute: Route = navGraph.startRoute,
    engine: NavHostEngine = rememberNavHostEngine(),
    navController: NavHostController = engine.rememberNavController(),
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit = {},
    manualComposableCallsBuilder: ManualComposableCallsBuilder.() -> Unit = {}
) {
    DisposableEffect(key1 = navController) {
        NavGraphRegistry.addGraph(navGraph)

        onDispose {
            NavGraphRegistry.removeGraph(navGraph)
        }
    }

    engine.NavHost(
        modifier = modifier,
        route = navGraph.route,
        startRoute = startRoute,
        navController = navController,
    ) {
        addNavGraphDestinations(
            engine = engine,
            navGraph = navGraph,
            navController = navController,
            dependenciesContainerBuilder = dependenciesContainerBuilder,
            manualComposableCalls = ManualComposableCallsBuilder(engine.type, navGraph)
                .apply { manualComposableCallsBuilder() }
                .build(),
        )
    }
}

//region internals

private fun NavGraphBuilder.addNavGraphDestinations(
    engine: NavHostEngine,
    navGraph: NavGraphSpec,
    navController: NavHostController,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
    manualComposableCalls: ManualComposableCalls,
): Unit = with(engine) {

    navGraph.destinationsByRoute.values.forEach { destination ->
        composable(
            destination,
            navController,
            dependenciesContainerBuilder,
            manualComposableCalls
        )
    }

    addNestedNavGraphs(
        engine = engine,
        nestedNavGraphs = navGraph.nestedNavGraphs,
        navController = navController,
        dependenciesContainerBuilder = dependenciesContainerBuilder,
        manualComposableCalls = manualComposableCalls
    )
}

private fun NavGraphBuilder.addNestedNavGraphs(
    engine: NavHostEngine,
    nestedNavGraphs: List<NavGraphSpec>,
    navController: NavHostController,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
    manualComposableCalls: ManualComposableCalls,
): Unit = with(engine) {

    nestedNavGraphs.forEach { nestedGraph ->
        navigation(nestedGraph) {
            addNavGraphDestinations(
                engine = engine,
                navGraph = nestedGraph,
                navController = navController,
                dependenciesContainerBuilder = dependenciesContainerBuilder,
                manualComposableCalls = manualComposableCalls,
            )
        }
    }
}

//endregion