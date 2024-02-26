package com.ramcosta.composedestinations.manualcomposablecalls

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.annotation.internal.InternalDestinationsApi
import com.ramcosta.composedestinations.scope.AnimatedDestinationScope
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.NavHostEngine
import com.ramcosta.composedestinations.spec.TypedDestinationSpec

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

class ManualComposableCallsBuilder internal constructor(@InternalDestinationsApi val engineType: NavHostEngine.Type) {

    /**
     * Overrides the style of [this] [DestinationSpec] at runtime to [animation].
     * You should prefer to use the Destination annotation `style` unless there's a specific
     * reason to use this.
     */
    infix fun DestinationSpec.animateWith(animation: DestinationStyle.Animated) {
        if (style !is DestinationStyle.Default && style !is DestinationStyle.Animated) {
            error("'animateWith' can only be called for a destination of style 'Default' or 'Animated'")
        }
        add(this, animation)
    }

    /**
     * Overrides the style of [this] [DestinationSpec] at runtime to use [enterTransition],
     * [exitTransition], [popEnterTransition] and [popExitTransition].
     * You should prefer to use the Destination annotation `style` unless there's a specific
     * reason to use this.
     */
    fun DestinationSpec.animateWith(
        enterTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = null,
        exitTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = null,
        popEnterTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? =
            enterTransition,
        popExitTransition: (@JvmSuppressWildcards
        AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? =
            exitTransition,
    ) {
        this animateWith object : DestinationStyle.Animated() {
            override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition() =
                enterTransition?.invoke(this)

            override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition() =
                exitTransition?.invoke(this)

            override fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition() =
                popEnterTransition?.invoke(this)

            override fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition() =
                popExitTransition?.invoke(this)
        }
    }

    private val map: MutableMap<String, DestinationLambda<*>> = mutableMapOf()
    private val animations: MutableMap<String, DestinationStyle.Animated> = mutableMapOf()

    internal fun add(
        destination: DestinationSpec,
        animation: DestinationStyle.Animated,
    ) {
        animations[destination.route] = animation
    }

    // can be internal once bottom sheet functionality is built in
    @InternalDestinationsApi
    fun add(
        lambda: DestinationLambda<*>,
        destination: DestinationSpec,
    ) {
        map[destination.route] = lambda
    }

    internal fun build() = ManualComposableCalls(map, animations)
}