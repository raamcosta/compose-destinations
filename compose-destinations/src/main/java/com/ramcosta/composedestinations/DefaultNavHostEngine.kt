package com.ramcosta.composedestinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.Navigator
import androidx.navigation.compose.navigation
import com.ramcosta.composedestinations.animations.defaults.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCalls
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.NavHostEngine
import com.ramcosta.composedestinations.spec.Route
import com.ramcosta.composedestinations.spec.TypedDestinationSpec

/**
 * Returns the default [NavHostEngine] to be used with [DestinationsNavHost]
 *
 * The [NavHostEngine] is used by default in [com.ramcosta.composedestinations.DestinationsNavHost]
 * call.
 */
@Composable
fun rememberNavHostEngine(
    navHostContentAlignment: Alignment = Alignment.Center,
): NavHostEngine = remember {
    DefaultNavHostEngine(
        navHostContentAlignment = navHostContentAlignment,
    )
}

internal class DefaultNavHostEngine(
    private val navHostContentAlignment: Alignment,
) : NavHostEngine {

    override val type = NavHostEngine.Type.DEFAULT

    @Composable
    override fun rememberNavController(
        vararg navigators: Navigator<out NavDestination>
    ) = androidx.navigation.compose.rememberNavController(*navigators)

    @Composable
    override fun NavHost(
        modifier: Modifier,
        route: String,
        startRoute: Route,
        defaultTransitions: NavHostAnimatedDestinationStyle,
        navController: NavHostController,
        builder: NavGraphBuilder.() -> Unit,
    ) = with(defaultTransitions) {
        androidx.navigation.compose.NavHost(
            navController = navController,
            startDestination = startRoute.route,
            modifier = modifier,
            route = route,
            contentAlignment = navHostContentAlignment,
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() },
            builder = builder
        )
    }

    override fun NavGraphBuilder.navigation(
        navGraph: NavGraphSpec,
        builder: NavGraphBuilder.() -> Unit
    ) {
        val transitions = navGraph.defaultTransitions
        if (transitions != null) {
            with(transitions) {
                navigation(
                    startDestination = navGraph.startRoute.route,
                    route = navGraph.route,
                    arguments = navGraph.arguments,
                    deepLinks = navGraph.deepLinks,
                    enterTransition = { enterTransition() },
                    exitTransition = { exitTransition() },
                    popEnterTransition = { popEnterTransition() },
                    popExitTransition = { popExitTransition() },
                    builder = builder,
                )
            }
        } else {
            navigation(
                startDestination = navGraph.startRoute.route,
                route = navGraph.route,
                arguments = navGraph.arguments,
                deepLinks = navGraph.deepLinks,
                builder = builder
            )
        }
    }

    override fun <T> NavGraphBuilder.composable(
        destination: TypedDestinationSpec<T>,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
        manualComposableCalls: ManualComposableCalls,
    ) = with(destination.style) {
        addComposable(destination, navController, dependenciesContainerBuilder, manualComposableCalls)
    }
}