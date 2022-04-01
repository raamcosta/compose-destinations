package com.ramcosta.composedestinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import com.ramcosta.composedestinations.manualcomposablecalls.DestinationLambda
import com.ramcosta.composedestinations.scope.DestinationScopeImpl
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCalls
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.spec.*

/**
 * Returns the default [NavHostEngine] to be used with normal (non-animated)
 * core ("io.github.raamcosta.compose-destinations:core").
 *
 * The [NavHostEngine] is used by default in [com.ramcosta.composedestinations.DestinationsNavHost]
 * call.
 */
@Composable
fun rememberNavHostEngine(): NavHostEngine = remember {
    DefaultNavHostEngine()
}

internal class DefaultNavHostEngine : NavHostEngine {

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
        navController: NavHostController,
        builder: NavGraphBuilder.() -> Unit
    ) {
        androidx.navigation.compose.NavHost(
            navController = navController,
            startDestination = startRoute.route,
            modifier = modifier,
            route = route,
            builder = builder
        )
    }

    override fun NavGraphBuilder.navigation(
        navGraph: NavGraphSpec,
        builder: NavGraphBuilder.() -> Unit
    ) {
        navigation(
            startDestination = navGraph.startRoute.route,
            route = navGraph.route,
            builder = builder
        )
    }

    override fun <T> NavGraphBuilder.composable(
        destination: DestinationSpec<T>,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
        manualComposableCalls: ManualComposableCalls,
    ) {
        when (val destinationStyle = destination.style) {
            is DestinationStyle.Default -> {
                addComposable(
                    destination,
                    navController,
                    dependenciesContainerBuilder,
                    manualComposableCalls
                )
            }

            is DestinationStyle.Dialog -> {
                addDialogComposable(
                    destinationStyle,
                    destination,
                    navController,
                    dependenciesContainerBuilder,
                    manualComposableCalls
                )
            }

            else -> throw IllegalStateException("You need to use 'rememberAnimatedNavHostEngine' to get an engine that can use ${destinationStyle.javaClass.simpleName} and pass that into the 'DestinationsNavHost' ")
        }
    }

    private fun <T> NavGraphBuilder.addComposable(
        destination: DestinationSpec<T>,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
        manualComposableCalls: ManualComposableCalls,
    ) {
        val contentLambda = manualComposableCalls[destination.routeId]

        composable(
            route = destination.route,
            arguments = destination.arguments,
            deepLinks = destination.deepLinks
        ) { navBackStackEntry ->
            CallComposable(
                destination,
                navController,
                navBackStackEntry,
                dependenciesContainerBuilder,
                contentLambda
            )
        }
    }

    private fun <T> NavGraphBuilder.addDialogComposable(
        dialogStyle: DestinationStyle.Dialog,
        destination: DestinationSpec<T>,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
        manualComposableCalls: ManualComposableCalls
    ) {
        val contentLambda = manualComposableCalls[destination.routeId]

        dialog(
            destination.route,
            destination.arguments,
            destination.deepLinks,
            dialogStyle.properties
        ) { navBackStackEntry ->
            CallComposable(
                destination,
                navController,
                navBackStackEntry,
                dependenciesContainerBuilder,
                contentLambda
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Composable
    private fun <T> CallComposable(
        destination: DestinationSpec<T>,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
        contentLambda: DestinationLambda<*>?
    ) {
        val scope = remember {
            DestinationScopeImpl.Default(
                destination,
                navBackStackEntry,
                navController
            )
        }

        if (contentLambda == null) {
            with(destination) { scope.Content(dependenciesContainerBuilder) }
        } else {
            contentLambda as DestinationLambda<T>
            contentLambda(scope)
        }
    }
}