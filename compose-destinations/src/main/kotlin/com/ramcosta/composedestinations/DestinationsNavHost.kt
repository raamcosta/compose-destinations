package com.ramcosta.composedestinations

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * Like [NavHost] but adds [destinations] to the nav graph, using
 * the properties of each [Destination] to do so.
 *
 * Also, it can be passed a [ScaffoldState] if it should be available
 * to each destination. Otherwise `null` can be used.
 * Recommendation is to pass in the [ScaffoldState] always, if this
 * is used inside a Scaffold.
 *
 * @see [NavHost]
 */
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