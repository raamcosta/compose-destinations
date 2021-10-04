package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.DESTINATIONS_AGGREGATE_CLASS_NAME
import com.ramcosta.composedestinations.codegen.commons.GENERATED_DESTINATION
import com.ramcosta.composedestinations.codegen.commons.GENERATED_NAV_GRAPH

val innerNavHost = """

@Composable
private fun InnerDestinationsNavHost(
    navController: NavHostController,
    modifier: Modifier,
    startDestination: Destination,
    situationalParametersProvider: (Destination) -> MutableMap<KClass<*>, Any> = { mutableMapOf() }
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
        route = $DESTINATIONS_AGGREGATE_CLASS_NAME.${GENERATED_NAV_GRAPH}s.root.route
    ) {
        addNavGraphDestinations(
            navGraphSpec = $DESTINATIONS_AGGREGATE_CLASS_NAME.${GENERATED_NAV_GRAPH}s.root,
            navController = navController,
            addNavigation = { navGraph, builder ->
                navigation(
                    navGraph.startDestination.route,
                    navGraph.route
                ) {
                    this.builder()
                }
            },
            addComposable = { destination ->
                destination as $GENERATED_DESTINATION
                composable(
                    route = destination.route,
                    arguments = destination.arguments,
                    deepLinks = destination.deepLinks
                ) { navBackStackEntry ->
                    destination.Content(navController, navBackStackEntry, situationalParametersProvider(destination))
                }
            }
        )
    }
}
""".trimIndent()