@file:OptIn(InternalDestinationsApi::class)

package com.ramcosta.composedestinations.spec

import android.annotation.SuppressLint
import androidx.annotation.RestrictTo
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.activity
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import com.ramcosta.composedestinations.animations.defaults.DestinationSizeTransform
import com.ramcosta.composedestinations.annotation.InternalDestinationsApi
import com.ramcosta.composedestinations.manualcomposablecalls.DestinationLambda
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCalls
import com.ramcosta.composedestinations.navigation.DependenciesContainerBuilder
import com.ramcosta.composedestinations.scope.AnimatedDestinationScopeImpl
import com.ramcosta.composedestinations.scope.DestinationScopeImpl

/**
 * Controls how the destination is shown when navigated to and navigated away from.
 * You can pass the KClass of an implementation to the
 * [com.ramcosta.composedestinations.annotation.Destination.style].
 */
interface DestinationStyle {

    /**
     * No special animation or style.
     * This is the default style used in case none is specified for a given Destination.
     *
     * Its animations will be inherited from the ones set at the navigation graph level,
     * using `@NavGraph(defaultTransitions = SomeClass::class)` (if the destination belongs to
     * some nested graph) or the [com.ramcosta.composedestinations.DestinationsNavHost]'s
     * `defaultTransitions` parameter for the top level "NavHost Graph".
     */
    object Default : DestinationStyle

    /**
     * Marks the destination to be shown as a dialog.
     *
     * You can create implementations that define specific [DialogProperties]
     * or you can use the default values with `style = DestinationStyle.Dialog::class`
     */
    interface Dialog : DestinationStyle {
        val properties: DialogProperties

        companion object Default : Dialog {
            override val properties = DialogProperties()
        }
    }

    /**
     * Marks the destination to have defined enter/exit transitions
     * when coming from or going to certain destinations.
     *
     * You will need to create an object which implements this interface
     * and use its KClass in [com.ramcosta.composedestinations.annotation.Destination.style]
     */
    interface Animated : DestinationStyle {

        fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition? {
            return null
        }

        fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition? {
            return null
        }

        fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(): EnterTransition? {
            return enterTransition()
        }

        fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(): ExitTransition? {
            return exitTransition()
        }

        val sizeTransform: DestinationSizeTransform? get() = null

        /**
         * Can be used to force no animations for certain destinations, if you've overridden
         * the default animation with `defaultAnimationParams`.
         */
        object None : Animated {
            override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition() =
                EnterTransition.None

            override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition() =
                ExitTransition.None
        }
    }

    /**
     * Marks the style as "Runtime" defined.
     *
     * This means that for this Destination, the style property will
     * contain a setter and you need to set it before calling `DestinationsNavHost`.
     *
     * This is useful if you want to define the style for a Destination in a
     * different module than the one which has the annotated Composable.
     */
    object Runtime: DestinationStyle

    @InternalDestinationsApi
    object Activity: DestinationStyle
}



@Composable
private fun <T> CallDialogComposable(
    destination: DestinationSpec<T>,
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
    contentWrapper: DestinationLambda<T>?
) {
    val scope = remember(navBackStackEntry) {
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

internal typealias AddComposable<T> = (NavGraphBuilder, DestinationSpec<T>, NavHostController, @Composable DependenciesContainerBuilder<*>.() -> Unit, ManualComposableCalls) -> Unit

@InternalDestinationsApi
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
var additionalAddComposable : AddComposable<*>? = null

internal fun <T> DestinationStyle.addDestination(
    navGraphBuilder: NavGraphBuilder,
    destination: DestinationSpec<T>,
    navController: NavHostController,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
    manualComposableCalls: ManualComposableCalls
) {
    when (this) {
        DestinationStyle.Runtime,
        DestinationStyle.Default -> {
            @SuppressLint("RestrictedApi")
            @Suppress("UNCHECKED_CAST")
            val contentWrapper = manualComposableCalls[destination.baseRoute] as? DestinationLambda<T>?

            navGraphBuilder.composable(
                route = destination.route,
                arguments = destination.arguments,
                deepLinks = destination.deepLinks,
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
        is DestinationStyle.Dialog -> {
            @SuppressLint("RestrictedApi")
            @Suppress("UNCHECKED_CAST")
            val contentLambda = manualComposableCalls[destination.baseRoute] as? DestinationLambda<T>?

            navGraphBuilder.dialog(
                destination.route,
                destination.arguments,
                destination.deepLinks,
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
        is DestinationStyle.Animated -> {
            navGraphBuilder.composable(
                route = destination.route,
                arguments = destination.arguments,
                deepLinks = destination.deepLinks,
                enterTransition = { enterTransition() },
                exitTransition = { exitTransition() },
                popEnterTransition = { popEnterTransition() },
                popExitTransition = { popExitTransition() },
                sizeTransform = sizeTransform?.let { transform ->
                    { with(transform) { sizeTransform() } }
                }
            ) { navBackStackEntry ->
                @SuppressLint("RestrictedApi")
                @Suppress("UNCHECKED_CAST")
                val contentWrapper = manualComposableCalls[destination.baseRoute] as? DestinationLambda<T>?

                CallComposable(
                    destination,
                    navController,
                    navBackStackEntry,
                    dependenciesContainerBuilder,
                    contentWrapper,
                )
            }
        }
        DestinationStyle.Activity -> {
            navGraphBuilder.addActivityDestination(destination as ActivityDestinationSpec<T>)
        }
        else -> {
            additionalAddComposable
                ?.invoke(navGraphBuilder, destination, navController, dependenciesContainerBuilder, manualComposableCalls)
                ?: error("Unknown DestinationStyle $this. If you're trying to use a destination with BottomSheet style, use rememberAnimatedNavHostEngine and pass that engine to DestinationsNavHost!")
        }
    }
}

internal fun <T> NavGraphBuilder.addActivityDestination(destination: ActivityDestinationSpec<T>) {
    activity(destination.route) {
        targetPackage = destination.targetPackage
        activityClass = destination.activityClass?.kotlin
        action = destination.action
        data = destination.data
        dataPattern = destination.dataPattern

        destination.deepLinks.forEach { deepLink ->
            deepLink {
                action = deepLink.action
                uriPattern = deepLink.uriPattern
                mimeType = deepLink.mimeType
            }
        }

        destination.arguments.forEach { navArg ->
            argument(navArg.name) {
                if (navArg.argument.isDefaultValuePresent) {
                    defaultValue = navArg.argument.defaultValue
                }
                type = navArg.argument.type
                nullable = navArg.argument.isNullable
            }
        }
    }
}

@Composable
private fun <T> AnimatedVisibilityScope.CallComposable(
    destination: DestinationSpec<T>,
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry,
    dependenciesContainerBuilder: @Composable DependenciesContainerBuilder<*>.() -> Unit,
    contentWrapper: DestinationLambda<T>?,
) {

    val scope = remember(navBackStackEntry) {
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
