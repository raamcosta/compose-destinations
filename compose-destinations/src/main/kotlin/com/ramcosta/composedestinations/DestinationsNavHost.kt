package com.ramcosta.composedestinations

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun DestinationsNavHost(
    destinations: Collection<Destination>,
    navController: NavHostController,
    startDestination: Destination,
    modifier: Modifier = Modifier,
    scaffoldState: ScaffoldState?,
    route: String? = null,
    builder: NavGraphBuilder.() -> Unit = {}
) {
    NavHost(navController, startDestination.route, modifier, route) {
        destinations.forEach { destination ->
            composable(
                route = destination.route,
                content = { destination.Content(navController, it, scaffoldState) },
                arguments = destination.arguments
            )
        }

        builder()
    }
}