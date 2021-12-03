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
import com.ramcosta.composedestinations.spec.NavHostEngine
import com.ramcosta.composedestinations.addDialogComposable
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.NavGraphSpec

@ExperimentalMaterialNavigationApi
@ExperimentalAnimationApi
@Composable
fun rememberAnimatedNavHostEngine(
    defaultAnimationParams: DefaultAnimationParams = DefaultAnimationParams(),
): NavHostEngine = remember {
    AnimatedNavHostEngine(defaultAnimationParams)
}

@ExperimentalMaterialNavigationApi
@ExperimentalAnimationApi
internal class AnimatedNavHostEngine(
    private val defaultAnimationParams: DefaultAnimationParams
) : NavHostEngine {

    @Composable
    override fun rememberNavController(
        vararg navigators: Navigator<out NavDestination>
    ) = rememberAnimatedNavController(*navigators)

    @Composable
    override fun NavHost(
        modifier: Modifier,
        navGraph: NavGraphSpec,
        startDestination: DestinationSpec,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.(NavBackStackEntry) -> Unit,
        builder: NavGraphBuilder.() -> Unit
    ) {
        AnimatedNavHost(
            navController = navController,
            startDestination = startDestination.route,
            modifier = modifier,
            route = navGraph.route,
            contentAlignment = defaultAnimationParams.contentAlignment,
            enterTransition = defaultAnimationParams.enterTransition.toAccompanist(),
            exitTransition = defaultAnimationParams.exitTransition.toAccompanist(),
            popEnterTransition = defaultAnimationParams.popEnterTransition.toAccompanist(),
            popExitTransition = defaultAnimationParams.popExitTransition.toAccompanist(),
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
            builder = builder,
        )
    }

    override fun NavGraphBuilder.composable(
        destination: DestinationSpec,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.(NavBackStackEntry) -> Unit
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

            is DestinationStyle.Animated -> {
                addAnimatedComposable(
                    destinationStyle,
                    destination,
                    navController,
                    dependenciesContainerBuilder
                )
            }

            is DestinationStyle.BottomSheet -> {
                addBottomSheetComposable(
                    destination,
                    navController,
                    dependenciesContainerBuilder
                )
            }
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
                dependency<AnimatedVisibilityScope>(this@composable)
                dependenciesContainerBuilder(navBackStackEntry)
            }
        }
    }

    private fun NavGraphBuilder.addAnimatedComposable(
        animatedStyle: DestinationStyle.Animated,
        destination: DestinationSpec,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.(NavBackStackEntry) -> Unit
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
            destination.Content(
                navController,
                navBackStackEntry
            ) {
                dependency<AnimatedVisibilityScope>(this@composable)
                dependenciesContainerBuilder(navBackStackEntry)
            }
        }
    }

    private fun NavGraphBuilder.addBottomSheetComposable(
        destination: DestinationSpec,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder.(NavBackStackEntry) -> Unit
    ) {
        bottomSheet(
            destination.route,
            destination.arguments,
            destination.deepLinks
        ) { navBackStackEntry ->
            destination.Content(
                navController,
                navBackStackEntry
            ) {
                dependency<ColumnScope>(this@bottomSheet)
                dependenciesContainerBuilder(navBackStackEntry)
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