package com.ramcosta.composedestinations

import androidx.compose.runtime.Composable
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

/**TODO racosta
 * Like [androidx.navigation.compose.NavHost] but includes the destinations of [navGraph].
 * Composables annotated with `@Destination` will belong to a [NavGraphSpec] inside `NavGraphs`
 * generated file. You can also disable the `NavGraphs` automatic generation in build.gradle:
 * ```
 * ksp {
 *     arg("compose-destinations.generateNavGraphs", "false")
 * }
 * ```
 * This might be useful if you need more complex `NavGraphs` then what the usage of the annotation
 * can provide. If you do this, it is advisable that you create your `NavGraphs` in a central and
 * stateless object, so that you can make queries to it more easily.
 *
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
 * @param manualComposableCallsBuilder this will offer a [ManualComposableCallsBuilder] scope where you can
 * make manual calls to specific [DestinationSpec] Composables which belong to this [navGraph].
 * This can be useful if you need to pass non-navigation arguments to those specific Composables which
 * the library cannot provide.
 */
@Composable
fun DestinationsNavHost(
    navGraph: NavGraphSpec,
    modifier: Modifier = Modifier,
    startRoute: Route = navGraph.startRoute,
    engine: NavHostEngine = rememberNavHostEngine(),
    navController: NavHostController = engine.rememberNavController(),
    dependenciesContainerBuilder: DependenciesContainerBuilder<*>.() -> Unit = {},
    manualComposableCallsBuilder: ManualComposableCallsBuilder.() -> Unit = {}
) {
    engine.NavHost(
        modifier = modifier,
        route = navGraph.route,
        startRoute = startRoute,
        navController = navController,
    ) {
        addNavGraphDestinations(
            engine = engine,
            navGraphSpec = navGraph,
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
    navGraphSpec: NavGraphSpec,
    navController: NavHostController,
    dependenciesContainerBuilder: DependenciesContainerBuilder<*>.() -> Unit,
    manualComposableCalls: ManualComposableCalls,
): Unit = with(engine) {

    navGraphSpec.destinationsByRoute.values.forEach { destination ->
        composable(
            destination,
            navController,
            dependenciesContainerBuilder,
            manualComposableCalls
        )
    }

    addNestedNavGraphs(
        engine = engine,
        nestedNavGraphs = navGraphSpec.nestedNavGraphs,
        navController = navController,
        dependenciesContainerBuilder = dependenciesContainerBuilder,
        manualComposableCalls = manualComposableCalls
    )
}

private fun NavGraphBuilder.addNestedNavGraphs(
    engine: NavHostEngine,
    nestedNavGraphs: List<NavGraphSpec>,
    navController: NavHostController,
    dependenciesContainerBuilder: DependenciesContainerBuilder<*>.() -> Unit,
    manualComposableCalls: ManualComposableCalls,
): Unit = with(engine) {

    nestedNavGraphs.forEach { nestedGraph ->
        navigation(nestedGraph) {
            addNavGraphDestinations(
                engine = engine,
                navGraphSpec = nestedGraph,
                navController = navController,
                dependenciesContainerBuilder = dependenciesContainerBuilder,
                manualComposableCalls = manualComposableCalls,
            )
        }
    }
}

//endregion