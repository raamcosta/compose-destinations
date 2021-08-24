package com.ramcosta.composedestinations

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun ScreensNavHost(
    screens: Collection<ScreenDestination>,
    navController: NavHostController,
    startDestination: ScreenDestination,
    modifier: Modifier = Modifier,
    route: String? = null,
    builder: NavGraphBuilder.() -> Unit = {}
) {
    NavHost(navController, startDestination.route, modifier, route) {
        screens.forEach { screen ->
            composable(
                route = screen.route,
                content = { screen.Content(navController, it) },
                arguments = screen.arguments
            )
        }

        builder()
    }
}