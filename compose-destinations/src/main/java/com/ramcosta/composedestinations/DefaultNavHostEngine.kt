package com.ramcosta.composedestinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.NavHostEngine

@Composable
fun rememberNavHostEngine(): NavHostEngine = remember {
    DefaultNavHostEngine()
}

internal class DefaultNavHostEngine : NavHostEngine {

    @Composable
    override fun rememberNavController(
        vararg navigators: Navigator<out NavDestination>
    ) = androidx.navigation.compose.rememberNavController(*navigators)

    @Composable
    override fun NavHost(
        modifier: Modifier,
        navGraph: NavGraphSpec,
        startDestination: DestinationSpec,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.(NavBackStackEntry) -> Unit,
        builder: NavGraphBuilder.() -> Unit
    ) {
        androidx.navigation.compose.NavHost(
            navController = navController,
            startDestination = startDestination.route,
            modifier = modifier,
            route = navGraph.route,
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

    override fun NavGraphBuilder.composable(
        destination: DestinationSpec,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.(NavBackStackEntry) -> Unit,
    ) {
        when (val destinationStyle = destination.style) {
            is DestinationStyle.Default -> {
                addComposable(
                    destination,
                    navController,
                    dependenciesContainerBuilder
                )
            }

            is DestinationStyle.Dialog -> {
                addDialogComposable(
                    destinationStyle,
                    destination,
                    navController,
                    dependenciesContainerBuilder
                )
            }

            else -> throw IllegalStateException("You need to use 'rememberAnimatedNavHostEngine' and pass that into the 'DestinationsNavHost' to get an engine that can use ${destinationStyle.javaClass.simpleName}")
        }
    }

    private fun NavGraphBuilder.addComposable(
        destination: DestinationSpec,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.(NavBackStackEntry) -> Unit
    ) {
        composable(
            route = destination.route,
            arguments = destination.arguments,
            deepLinks = destination.deepLinks
        ) { navBackStackEntry ->
            destination.Content(
                navController,
                navBackStackEntry
            ) {
                dependenciesContainerBuilder(navBackStackEntry)
            }
        }
    }
}