package com.ramcosta.composedestinations.spec

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.ramcosta.composedestinations.manualcomposablecalls.DestinationLambda
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCalls
import com.ramcosta.composedestinations.manualcomposablecalls.allDeepLinks
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.scope.AnimatedDestinationScopeImpl
import com.ramcosta.composedestinations.scope.DestinationScopeImpl

/**
 * Controls how the destination is shown when navigated to and navigated away from.
 * You can pass the KClass of an implementation to the
 * [com.ramcosta.composedestinations.annotation.Destination.style].
 */
abstract class DestinationStyle {

    abstract fun <T> NavGraphBuilder.addComposable(
        destination: TypedDestinationSpec<T>,
        navController: NavHostController,
        dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
        manualComposableCalls: ManualComposableCalls
    )

    /**
     * This is the default style used in case none is specified for a given Destination.
     *
     * Its animations will be inherited from the ones set at the navigation graph level,
     * using `@NavGraph(defaultTransitions = SomeClass::class)`, if the destination belongs to
     * a graph defined this way, or the [com.ramcosta.composedestinations.DestinationsNavHost]'s
     * `defaultTransitions` parameter for the top level "NavHost Graph".
     */
    object Default : DestinationStyle() {
        override fun <T> NavGraphBuilder.addComposable(
            destination: TypedDestinationSpec<T>,
            navController: NavHostController,
            dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
            manualComposableCalls: ManualComposableCalls
        ) {
            @Suppress("UNCHECKED_CAST")
            val contentWrapper = manualComposableCalls[destination.route] as? DestinationLambda<T>?

            composable(
                route = destination.route,
                arguments = destination.arguments,
                deepLinks = destination.allDeepLinks(manualComposableCalls),
            ) { navBackStackEntry ->
                CallComposable(
                    destination,
                    navController,
                    navBackStackEntry,
                    dependenciesContainerBuilder,
                    contentWrapper,
                )
            }
        }
    }

    /**
     * Marks the destination to have defined enter/exit transitions
     * when coming from or going to certain destinations.
     *
     * You will need to create an object which implements this interface
     * and use its KClass in [com.ramcosta.composedestinations.annotation.Destination.style]
     */
    abstract class Animated : DestinationStyle() {

        open val enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)?
            get() = null
        open val exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)?
            get() = null
        open val popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)?
            get() = enterTransition
        open val popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)?
            get() = exitTransition
        open val sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)?
            get() = null

        /**
         * Can be used to force no animations for certain destinations, if you've overridden
         * the default animation with `defaultAnimationParams`.
         */
        object None : Animated() {
            override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = { EnterTransition.None }

            override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = { ExitTransition.None }
        }

        final override fun <T> NavGraphBuilder.addComposable(
            destination: TypedDestinationSpec<T>,
            navController: NavHostController,
            dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
            manualComposableCalls: ManualComposableCalls
        ) {
            composable(
                route = destination.route,
                arguments = destination.arguments,
                deepLinks = destination.allDeepLinks(manualComposableCalls),
                enterTransition = enterTransition,
                exitTransition = exitTransition,
                popEnterTransition = popEnterTransition,
                popExitTransition = popExitTransition,
                sizeTransform = sizeTransform
            ) { navBackStackEntry ->
                @Suppress("UNCHECKED_CAST")
                val contentWrapper = manualComposableCalls[destination.route] as? DestinationLambda<T>?

                CallComposable(
                    destination,
                    navController,
                    navBackStackEntry,
                    dependenciesContainerBuilder,
                    contentWrapper,
                )
            }
        }
    }

    /**
     * Marks the destination to be shown as a dialog.
     *
     * You can create implementations that define specific [DialogProperties]
     * or you can use the default values with `style = DestinationStyle.Dialog::class`
     */
    abstract class Dialog : DestinationStyle() {
        abstract val properties: DialogProperties

        companion object Default : Dialog() {
            override val properties = DialogProperties()
        }

        final override fun <T> NavGraphBuilder.addComposable(
            destination: TypedDestinationSpec<T>,
            navController: NavHostController,
            dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
            manualComposableCalls: ManualComposableCalls
        ) {
            @Suppress("UNCHECKED_CAST")
            val contentLambda = manualComposableCalls[destination.route] as? DestinationLambda<T>?

            dialog(
                destination.route,
                destination.arguments,
                destination.allDeepLinks(manualComposableCalls),
                properties
            ) { navBackStackEntry ->
                CallDialogComposable(
                    destination,
                    navController,
                    navBackStackEntry,
                    dependenciesContainerBuilder,
                    contentLambda
                )
            }
        }
    }
}

@Composable
private fun <T> CallDialogComposable(
    destination: TypedDestinationSpec<T>,
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
    contentWrapper: DestinationLambda<T>?
) {
    val scope = remember(destination, navBackStackEntry, navController, dependenciesContainerBuilder) {
        DestinationScopeImpl.Default(
            destination,
            navBackStackEntry,
            navController,
            dependenciesContainerBuilder,
        )
    }

    if (contentWrapper == null) {
        with(destination) { scope.Content() }
    } else {
        contentWrapper(scope)
    }
}

@Composable
private fun <T> AnimatedVisibilityScope.CallComposable(
    destination: TypedDestinationSpec<T>,
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
    contentWrapper: DestinationLambda<T>?,
) {

    val scope = remember(destination, navBackStackEntry, navController, this, dependenciesContainerBuilder) {
        AnimatedDestinationScopeImpl(
            destination,
            navBackStackEntry,
            navController,
            this,
            dependenciesContainerBuilder
        )
    }

    if (contentWrapper == null) {
        with(destination) { scope.Content() }
    } else {
        contentWrapper(scope)
    }
}
