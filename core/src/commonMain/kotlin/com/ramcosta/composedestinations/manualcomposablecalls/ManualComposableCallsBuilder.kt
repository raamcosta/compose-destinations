package com.ramcosta.composedestinations.manualcomposablecalls

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import com.ramcosta.composedestinations.annotation.internal.InternalDestinationsApi
import com.ramcosta.composedestinations.scope.AnimatedDestinationScope
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.NavHostEngine
import com.ramcosta.composedestinations.spec.NavHostGraphSpec
import com.ramcosta.composedestinations.spec.TypedDestinationSpec
import kotlin.jvm.JvmSuppressWildcards

/**
 * Registers [content] lambda as the responsible for calling
 * the Composable correspondent to [destination].
 *
 * When [destination] is navigated to, [content] will be called
 * with the correct [AnimatedDestinationScope] containing the navigation
 * arguments, the back stack entry and navigators.
 */
fun <T> ManualComposableCallsBuilder.composable(
    destination: TypedDestinationSpec<T>,
    content: @Composable AnimatedDestinationScope<T>.() -> Unit
) {
    this as ManualComposableCallsBuilderImpl
    if (engineType != NavHostEngine.Type.DEFAULT) {
        error("'composable' can only be called with a 'NavHostEngine'")
    }

    if (destination.style !is DestinationStyle.Animated && destination.style !is DestinationStyle.Default) {
        error("'composable' can only be called for a destination of style 'Animated' or 'Default'")
    }

    add(
        lambda = DestinationLambda.Normal(content),
        destination = destination,
    )
}

/**
 * Registers [content] lambda as the responsible for calling
 * the Composable correspondent to [destination].
 *
 * When [destination] is navigated to, [content] will be called
 * with the correct [DestinationScope] containing the navigation
 * arguments, the back stack entry and navigators.
 */
fun <T> ManualComposableCallsBuilder.dialogComposable(
    destination: TypedDestinationSpec<T>,
    content: @Composable DestinationScope<T>.() -> Unit
) {
    this as ManualComposableCallsBuilderImpl
    if (engineType != NavHostEngine.Type.DEFAULT) {
        error("'dialogComposable' can only be called with a 'NavHostEngine'")
    }

    if (destination.style !is DestinationStyle.Dialog) {
        error("'dialogComposable' can only be called for a destination of style 'Dialog'")
    }

    add(
        lambda = DestinationLambda.Dialog(content),
        destination = destination,
    )
}

sealed interface ManualComposableCallsBuilder {
    /**
     * Overrides the default animations of [this] [NavGraphSpec] at runtime to [animation].
     * You should prefer to use the NavGraph annotation `defaultTransitions` unless there's a specific
     * reason to use this.
     */
    infix fun NavGraphSpec.animateWith(animation: DestinationStyle.Animated)

    /**
     * Overrides the default animations of [this] [NavGraphSpec] at runtime to use [enterTransition],
     * [exitTransition], [popEnterTransition] and [popExitTransition].
     * You should prefer to use the NavGraph annotation `defaultTransitions` unless there's a specific
     * reason to use this.
     */
    fun NavGraphSpec.animateWith(
        enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
            null,
        exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
            null,
        popEnterTransition: (
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
        )? = enterTransition,
        popExitTransition: (
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
        )? = exitTransition,
        sizeTransform: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? = null,
    )

    /**
     * Overrides the style of [this] [DestinationSpec] at runtime to [animation].
     * You should prefer to use the Destination annotation `style` unless there's a specific
     * reason to use this.
     */
    infix fun DestinationSpec.animateWith(animation: DestinationStyle.Animated)

    /**
     * Overrides the style of [this] [DestinationSpec] at runtime to use [enterTransition],
     * [exitTransition], [popEnterTransition] and [popExitTransition].
     * You should prefer to use the Destination annotation `style` unless there's a specific
     * reason to use this.
     */
    fun DestinationSpec.animateWith(
        enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
        exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
        popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = enterTransition,
        popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = exitTransition,
        sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)? = null
    )
}

@InternalDestinationsApi
class ManualComposableCallsBuilderImpl internal constructor(
    @InternalDestinationsApi val engineType: NavHostEngine.Type
) : ManualComposableCallsBuilder {

    /**
     * Overrides the default animations of [this] [NavGraphSpec] at runtime to [animation].
     * You should prefer to use the NavGraph annotation `defaultTransitions` unless there's a specific
     * reason to use this.
     */
    override infix fun NavGraphSpec.animateWith(animation: DestinationStyle.Animated) {
        if (this is NavHostGraphSpec) {
            error("'animateWith' cannot be called for NavHostGraphs. Use DestinationsNavHost's 'defaultTransitions' for the same effect!")
        }
        add(route, animation)
    }

    override fun NavGraphSpec.animateWith(
        enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)?,
        exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)?,
        popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)?,
        popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)?,
        sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)?,
    ) {
        this animateWith object: DestinationStyle.Animated() {
            override val enterTransition = enterTransition
            override val exitTransition = exitTransition
            override val popEnterTransition = popEnterTransition
            override val popExitTransition = popExitTransition
            override val sizeTransform = sizeTransform
        }
    }

    override infix fun DestinationSpec.animateWith(animation: DestinationStyle.Animated) {
        if (style !is DestinationStyle.Default && style !is DestinationStyle.Animated) {
            error("'animateWith' can only be called for a destination of style 'Default' or 'Animated'")
        }
        add(route, animation)
    }

    override fun DestinationSpec.animateWith(
        enterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)?,
        exitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)?,
        popEnterTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)?,
        popExitTransition: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)?,
        sizeTransform: (@JvmSuppressWildcards AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)?,
    ) {
        this animateWith object : DestinationStyle.Animated() {
            override val enterTransition = enterTransition
            override val exitTransition = exitTransition
            override val popEnterTransition = popEnterTransition
            override val popExitTransition = popExitTransition
            override val sizeTransform = sizeTransform
        }
    }

    private val map: MutableMap<String, DestinationLambda<*>> = mutableMapOf()
    private val animations: MutableMap<String, DestinationStyle.Animated> = mutableMapOf()
    private val deepLinks: MutableMap<String, MutableList<NavDeepLink>> = mutableMapOf()

    internal fun add(
        route: String,
        deepLink: NavDeepLink,
    ) {
        deepLinks.getOrPut(route) { mutableListOf() }.add(deepLink)
    }

    internal fun add(
        route: String,
        animation: DestinationStyle.Animated,
    ) {
        animations[route] = animation
    }

    // can be internal once bottom sheet functionality is built in
    @InternalDestinationsApi
    fun add(
        lambda: DestinationLambda<*>,
        destination: DestinationSpec,
    ) {
        map[destination.route] = lambda
    }

    internal fun build() = ManualComposableCalls(map, animations, deepLinks)
}