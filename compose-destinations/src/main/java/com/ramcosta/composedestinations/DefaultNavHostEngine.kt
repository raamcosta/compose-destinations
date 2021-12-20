package com.ramcosta.composedestinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.navigation
import com.ramcosta.composedestinations.manualcomposablecalls.DestinationLambda
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCalls
import com.ramcosta.composedestinations.navigation.DestinationDependenciesContainer
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.NavHostEngine

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
        startDestination: DestinationSpec<*>,
        navController: NavHostController,
        builder: NavGraphBuilder.() -> Unit
    ) {
        androidx.navigation.compose.NavHost(
            navController = navController,
            startDestination = startDestination.route,
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
            startDestination = navGraph.startDestination.route,
            route = navGraph.route,
            builder = builder
        )
    }

    override fun <T> NavGraphBuilder.composable(
        destination: DestinationSpec<T>,
        navController: NavHostController,
        manualComposableCalls: ManualComposableCalls,
    ) {
        when (val destinationStyle = destination.style) {
            is DestinationStyle.Default -> {
                addComposable(
                    destination,
                    navController,
                    manualComposableCalls
                )
            }

            is DestinationStyle.Dialog -> {
                addDialogComposable(
                    destinationStyle,
                    destination,
                    navController,
                    manualComposableCalls
                )
            }

            else -> throw IllegalStateException("You need to use 'rememberAnimatedNavHostEngine' to get an engine that can use ${destinationStyle.javaClass.simpleName} and pass that into the 'DestinationsNavHost' ")
        }
    }

    private fun <T> NavGraphBuilder.addComposable(
        destination: DestinationSpec<T>,
        navController: NavHostController,
        manualComposableCalls: ManualComposableCalls,
    ) {
        composable(
            route = destination.route,
            arguments = destination.arguments,
            deepLinks = destination.deepLinks
        ) { navBackStackEntry ->
            CallComposable(
                destination,
                navController,
                navBackStackEntry,
                manualComposableCalls
            )
        }
    }

    private fun <T> NavGraphBuilder.addDialogComposable(
        dialogStyle: DestinationStyle.Dialog,
        destination: DestinationSpec<T>,
        navController: NavHostController,
        manualComposableCalls: ManualComposableCalls
    ) {
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
                manualComposableCalls
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Composable
    private fun <T> CallComposable(
        destination: DestinationSpec<T>,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        manualComposableCalls: ManualComposableCalls
    ) {
        val contentLambda = manualComposableCalls[destination]
        if (contentLambda == null) {
            destination.Content(
                navController,
                navBackStackEntry,
                DestinationDependenciesContainer()
            )
        } else {
            contentLambda as DestinationLambda<T>
            contentLambda(
                destination = destination,
                navBackStackEntry = navBackStackEntry,
                navController = navController,
                receiver = null
            )
        }
    }
}