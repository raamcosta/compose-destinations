package com.ramcosta.composedestinations.wear

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.wear.compose.navigation.*
import com.ramcosta.composedestinations.animations.defaults.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.manualcomposablecalls.DestinationLambda
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCalls
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.scope.DestinationScopeImpl
import com.ramcosta.composedestinations.spec.*

/**
 * Returns the [WearNavHostEngine] to be used with Wear OS apps.
 */
@Composable
fun rememberWearNavHostEngine(
    state: SwipeDismissableNavHostState = rememberSwipeDismissableNavHostState(),
): NavHostEngine {
    val defaultNavHostEngine = rememberNavHostEngine()

    return remember {
        WearNavHostEngine(defaultNavHostEngine, state)
    }
}

internal class WearNavHostEngine(
    private val defaultNavHostEngine: NavHostEngine,
    private val state: SwipeDismissableNavHostState,
) : NavHostEngine {

    override val type = NavHostEngine.Type.WEAR

    @Composable
    override fun rememberNavController(
        vararg navigators: Navigator<out NavDestination>
    ) =
        androidx.navigation.compose.rememberNavController(remember { WearNavigator() }, *navigators)

    @Composable
    override fun NavHost(
        modifier: Modifier,
        route: String,
        startRoute: Route,
        defaultTransitions: NavHostAnimatedDestinationStyle,
        navController: NavHostController,
        builder: NavGraphBuilder.() -> Unit,
    ) {
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = startRoute.route,
            modifier = modifier,
            route = route,
            state = state,
            builder = builder
        )
    }

    override fun NavGraphBuilder.navigation(
        navGraph: NavGraphSpec,
        builder: NavGraphBuilder.() -> Unit
    ) {
        with(defaultNavHostEngine) { navigation(navGraph, builder) }
    }

    override fun <T> NavGraphBuilder.composable(
        destination: TypedDestinationSpec<T>,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
        manualComposableCalls: ManualComposableCalls,
    ) {
        when (destination.style) {
            is DestinationStyle.Default -> {
                addComposable(
                    destination,
                    navController,
                    dependenciesContainerBuilder,
                    manualComposableCalls
                )
            }

            is DestinationStyle.Activity -> {
                with(defaultNavHostEngine) {
                    composable(destination, navController, dependenciesContainerBuilder, manualComposableCalls)
                }
            }

            is DestinationStyle.Dialog -> {
                throw IllegalStateException("${destination.style.javaClass.name} cannot be used on Wear OS version of the core library!")
            }
        }
    }

    private fun <T> NavGraphBuilder.addComposable(
        destination: TypedDestinationSpec<T>,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
        manualComposableCalls: ManualComposableCalls,
    ) {
        @SuppressLint("RestrictedApi")
        @Suppress("UNCHECKED_CAST")
        val contentLambda = manualComposableCalls[destination.route] as? DestinationLambda<T>?

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

    internal class WearDestinationScope<T>(
        override val destination: TypedDestinationSpec<T>,
        override val navBackStackEntry: NavBackStackEntry,
        override val navController: NavController,
        override val dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
    ) : DestinationScopeImpl<T>()

    @Composable
    private fun <T> CallComposable(
        destination: TypedDestinationSpec<T>,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
        contentWrapper: DestinationLambda<T>?
    ) {
        val scope = remember(navBackStackEntry) {
            WearDestinationScope(
                destination,
                navBackStackEntry,
                navController,
                dependenciesContainerBuilder
            )
        }

        if (contentWrapper == null) {
            with(destination) { scope.Content() }
        } else {
            contentWrapper(scope)
        }
    }
}