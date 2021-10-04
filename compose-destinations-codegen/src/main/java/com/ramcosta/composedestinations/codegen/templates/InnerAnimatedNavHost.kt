package com.ramcosta.composedestinations.codegen.templates

import com.ramcosta.composedestinations.codegen.commons.*

val innerAnimatedNavHost = """
    
@ExperimentalAnimationApi
@Composable
private fun InnerDestinationsNavHost(
    navController: NavHostController,
    modifier: Modifier,
    startDestination: Destination,
    situationalParametersProvider: (Destination) -> MutableMap<KClass<*>, Any> = { mutableMapOf() },
    contentAlignment: Alignment = Alignment.Center,
    enterTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition)?,
    exitTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition)?,
    popEnterTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition)?,
    popExitTransition: (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition)?,
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination.route,
        modifier = modifier,
        route = $DESTINATIONS_AGGREGATE_CLASS_NAME.${GENERATED_NAV_GRAPH}s.root.route, 
        contentAlignment = contentAlignment,
        enterTransition = enterTransition,
        exitTransition = exitTransition,
        popEnterTransition = popEnterTransition,
        popExitTransition = popExitTransition,
    ) {
        addNavGraphDestinations(
            navGraphSpec = $DESTINATIONS_AGGREGATE_CLASS_NAME.${GENERATED_NAV_GRAPH}s.root, 
            navController = navController,
            addNavigation = addNavigation(),
            addComposable = addComposable(navController, situationalParametersProvider)
        )
    }
}

@ExperimentalAnimationApi
private fun addComposable(
    navController: NavHostController,
    situationalParametersProvider: (Destination) -> MutableMap<KClass<*>, Any>
): NavGraphBuilder.($CORE_DESTINATION_SPEC) -> Unit {
    return { destination ->
        destination as $GENERATED_DESTINATION
        val transitionType = destination.transitionType
        when (transitionType) {
            is TransitionType.None -> {
                addComposableWithNoAnimation(
                    destination,
                    navController,
                    situationalParametersProvider
                )
            }

            is TransitionType.Animation -> {
                addComposable(
                    transitionType,
                    destination,
                    navController,
                    situationalParametersProvider
                )
            }
        }
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addComposable(
    transitionType: TransitionType.Animation,
    destination: Destination,
    navController: NavHostController,
    situationalParametersProvider: ($GENERATED_DESTINATION) -> MutableMap<KClass<*>, Any>
) = with(transitionType.destinationTransitions) {
    animationComposable(
        route = destination.route,
        arguments = destination.arguments,
        deepLinks = destination.deepLinks,
        enterTransition = enterTransition.mapToAccompanist(),
        exitTransition = exitTransition.mapToAccompanist(),
        popEnterTransition = popEnterTransition.mapToAccompanist(),
        popExitTransition = popExitTransition.mapToAccompanist()
    ) { navBackStackEntry ->
        destination.Content(
            navController,
            navBackStackEntry,
            situationalParametersProvider(destination).apply {
                this[AnimatedVisibilityScope::class] = this@animationComposable
            })
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addComposableWithNoAnimation(
    destination: $GENERATED_DESTINATION,
    navController: NavHostController,
    situationalParametersProvider: ($GENERATED_DESTINATION) -> MutableMap<KClass<*>, Any>
) {
    animationComposable(
        route = destination.route,
        arguments = destination.arguments,
        deepLinks = destination.deepLinks
    ) { navBackStackEntry ->
        destination.Content(
            navController,
            navBackStackEntry,
            situationalParametersProvider(destination)
        )
    }
}

@ExperimentalAnimationApi
private fun addNavigation(): NavGraphBuilder.($CORE_NAV_GRAPH_SPEC, NavGraphBuilder.() -> Unit) -> Unit {
    return { navGraph, builder ->
        animationNavigation(
            navGraph.startDestination.route,
            navGraph.route
        ) {
            this.builder()
        }
    }
}

@ExperimentalAnimationApi
private fun <T> (AnimatedContentScope<String>.($GENERATED_DESTINATION?, $GENERATED_DESTINATION?) -> T)?.mapToAccompanist(): (AnimatedContentScope<String>.(NavBackStackEntry, NavBackStackEntry) -> T)? {
    return this?.let {
        { initial, target ->
            val initialDest = initial.destination.route?.let {
                $DESTINATIONS_AGGREGATE_CLASS_NAME.${GENERATED_NAV_GRAPH}s.root.findDestination(it) as Destination
            }
            val targetDest = target.destination.route?.let {
                $DESTINATIONS_AGGREGATE_CLASS_NAME.${GENERATED_NAV_GRAPH}s.root.findDestination(it) as Destination
            }
            it.invoke(this, initialDest, targetDest)
        }
    }
}
""".trimIndent()