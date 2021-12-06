package com.ramcosta.composedestinations.animations

import androidx.compose.animation.*
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet
import com.ramcosta.composedestinations.ComposableLambdaType
import com.ramcosta.composedestinations.ManualComposableCalls
import com.ramcosta.composedestinations.animations.defaults.DefaultAnimationParams
import com.ramcosta.composedestinations.animations.defaults.DestinationEnterTransition
import com.ramcosta.composedestinations.animations.defaults.DestinationExitTransition
import com.ramcosta.composedestinations.animations.defaults.NavGraphDefaultAnimationParams
import com.ramcosta.composedestinations.navigation.DestinationDependenciesContainer
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.NavHostEngine

/**
 * Remembers and returns an instance of a [NavHostEngine]
 * suitable for navigation animations and bottom sheet styled
 * destinations.
 *
 * @param defaultAnimationParams animations to set as default for all destinations that don't specify
 * others via `Destination` annotation's `style` argument. If [defaultAnimationParams] is not passed
 * in, then no animations will happen by default.
 * @param defaultAnimationsForNestedNavGraph lambda called for each nested navigation graph that
 * allows you to override the default animations of [defaultAnimationParams] with defaults just for
 * that specific nested navigation graph.
 */
@ExperimentalMaterialNavigationApi
@ExperimentalAnimationApi
@Composable
fun rememberAnimatedNavHostEngine(
    defaultAnimationParams: DefaultAnimationParams = DefaultAnimationParams(),
    defaultAnimationsForNestedNavGraph: (NavGraphSpec) -> NavGraphDefaultAnimationParams = { NavGraphDefaultAnimationParams() }
): NavHostEngine {
    val defaultNavHostEngine = rememberNavHostEngine()
    return remember {
        AnimatedNavHostEngine(
            defaultAnimationParams,
            defaultAnimationsForNestedNavGraph,
            defaultNavHostEngine
        )
    }
}

@ExperimentalMaterialNavigationApi
@ExperimentalAnimationApi
internal class AnimatedNavHostEngine(
    private val defaultAnimationParams: DefaultAnimationParams,
    private val defaultAnimationsPerNestedNavGraph: (NavGraphSpec) -> NavGraphDefaultAnimationParams,
    private val defaultNavHostEngine: NavHostEngine
) : NavHostEngine {

    override val type = NavHostEngine.Type.ANIMATED

    @Composable
    override fun rememberNavController(
        vararg navigators: Navigator<out NavDestination>
    ) = rememberAnimatedNavController(*navigators)

    @Composable
    override fun NavHost(
        modifier: Modifier,
        route: String,
        startDestination: DestinationSpec<*>,
        navController: NavHostController,
        builder: NavGraphBuilder.() -> Unit
    ) = with(defaultAnimationParams) {

        AnimatedNavHost(
            navController = navController,
            startDestination = startDestination.route,
            modifier = modifier,
            route = route,
            contentAlignment = contentAlignment,
            enterTransition = enterTransition.toAccompanist(),
            exitTransition = exitTransition.toAccompanist(),
            popEnterTransition = popEnterTransition.toAccompanist(),
            popExitTransition = popExitTransition.toAccompanist(),
            builder = builder
        )
    }

    override fun NavGraphBuilder.navigation(
        navGraph: NavGraphSpec,
        builder: NavGraphBuilder.() -> Unit
    ) = with(defaultAnimationsPerNestedNavGraph(navGraph)) {

        navigation(
            startDestination = navGraph.startDestination.route,
            route = navGraph.route,
            enterTransition = enterTransition.toAccompanist(),
            exitTransition = exitTransition.toAccompanist(),
            popEnterTransition = popEnterTransition.toAccompanist(),
            popExitTransition = popExitTransition.toAccompanist(),
            builder = builder,
        )
    }

    override fun <T> NavGraphBuilder.composable(
        destination: DestinationSpec<T>,
        navController: NavHostController,
        manualComposableCalls: ManualComposableCalls
    ) {
        when (val destinationStyle = destination.style) {
            is DestinationStyle.Default -> {
                addComposable(
                    destination,
                    navController,
                    manualComposableCalls
                )
            }

            is DestinationStyle.Animated -> {
                addAnimatedComposable(
                    destinationStyle,
                    destination,
                    navController,
                    manualComposableCalls
                )
            }

            is DestinationStyle.BottomSheet -> {
                addBottomSheetComposable(
                    destination,
                    navController,
                    manualComposableCalls
                )
            }

            is DestinationStyle.Dialog -> {
                // We delegate this to the default NavHost Engine
                with(defaultNavHostEngine) {
                    composable(
                        destination,
                        navController,
                        manualComposableCalls
                    )
                }
            }
        }
    }

    private fun <T> NavGraphBuilder.addComposable(
        destination: DestinationSpec<T>,
        navController: NavHostController,
        manualComposableCalls: ManualComposableCalls
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
                manualComposableCalls,
            )
        }
    }

    private fun <T> NavGraphBuilder.addAnimatedComposable(
        animatedStyle: DestinationStyle.Animated,
        destination: DestinationSpec<T>,
        navController: NavHostController,
        manualComposableCalls: ManualComposableCalls
    ) = with(animatedStyle) {
        composable(
            route = destination.route,
            arguments = destination.arguments,
            deepLinks = destination.deepLinks,
            enterTransition = { i, t -> enterTransition(i, t) },
            exitTransition = { i, t -> exitTransition(i, t) },
            popEnterTransition = { i, t -> popEnterTransition(i, t) },
            popExitTransition = { i, t -> popExitTransition(i, t) }
        ) { navBackStackEntry ->
            CallComposable(
                destination,
                navController,
                navBackStackEntry,
                manualComposableCalls
            )
        }
    }

    private fun <T> NavGraphBuilder.addBottomSheetComposable(
        destination: DestinationSpec<T>,
        navController: NavHostController,
        manualComposableCalls: ManualComposableCalls
    ) {
        bottomSheet(
            destination.route,
            destination.arguments,
            destination.deepLinks
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
    private fun <T> ColumnScope.CallComposable(
        destination: DestinationSpec<T>,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        manualComposableCalls: ManualComposableCalls
    ) {
        val typeAndLambda = manualComposableCalls[destination]

        if (typeAndLambda == null) {
            destination.Content(
                navController,
                navBackStackEntry,
                DestinationDependenciesContainer().apply { dependency(this@CallComposable) }
            )
        } else {
            val (type, content) = typeAndLambda
            if (type == ComposableLambdaType.BOTTOM_SHEET) {
                (content as @Composable ColumnScope.(T, NavBackStackEntry) -> Unit)(
                    remember { destination.argsFrom(navBackStackEntry) },
                    navBackStackEntry
                )
            } else {
                (content as @Composable (T, NavBackStackEntry) -> Unit)(
                    remember { destination.argsFrom(navBackStackEntry) },
                    navBackStackEntry
                )
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Composable
    private fun <T> AnimatedVisibilityScope.CallComposable(
        destination: DestinationSpec<T>,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        manualComposableCalls: ManualComposableCalls,
    ) {
        val typeAndLambda = manualComposableCalls[destination]

        if (typeAndLambda == null) {
            destination.Content(
                navController,
                navBackStackEntry,
                DestinationDependenciesContainer().apply { dependency(this@CallComposable) }
            )
        } else {
            val (type, content) = typeAndLambda
            if (type == ComposableLambdaType.ANIMATED) {
                (content as @Composable AnimatedVisibilityScope.(T, NavBackStackEntry) -> Unit)(
                    remember { destination.argsFrom(navBackStackEntry) },
                    navBackStackEntry
                )
            } else {
                (content as @Composable (T, NavBackStackEntry) -> Unit)(
                    remember { destination.argsFrom(navBackStackEntry) },
                    navBackStackEntry
                )
            }
        }
    }

    private fun DestinationEnterTransition?.toAccompanist(): (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> EnterTransition)? {
        return this?.run {
            { initial, target ->
                enter(initial, target)
            }
        }
    }

    private fun DestinationExitTransition?.toAccompanist(): (AnimatedContentScope<String>.(initial: NavBackStackEntry, target: NavBackStackEntry) -> ExitTransition)? {
        return this?.run {
            { initial, target ->
                exit(initial, target)
            }
        }
    }
}