package com.ramcosta.composedestinations.spec

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.*
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder

interface NavHostEngine {

    @Composable
    fun rememberNavController(
        vararg navigators: Navigator<out NavDestination>
    ): NavHostController

    @Composable
    fun NavHost(
        modifier: Modifier,
        navGraph: NavGraphSpec,
        startDestination: DestinationSpec,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.(NavBackStackEntry) -> Unit,
        builder: NavGraphBuilder.() -> Unit
    )

    fun NavGraphBuilder.navigation(
        navGraph: NavGraphSpec,
        builder: NavGraphBuilder.() -> Unit
    )

    fun NavGraphBuilder.composable(
        destination: DestinationSpec,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.(NavBackStackEntry) -> Unit,
    )
}