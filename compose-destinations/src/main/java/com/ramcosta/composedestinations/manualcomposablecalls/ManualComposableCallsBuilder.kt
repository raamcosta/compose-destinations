package com.ramcosta.composedestinations.manualcomposablecalls

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.dynamic.DynamicDestinationSpec
import com.ramcosta.composedestinations.scope.AnimatedDestinationScope
import com.ramcosta.composedestinations.scope.BottomSheetDestinationScope
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.spec.*
import com.ramcosta.composedestinations.utils.allDestinations

/**
 * Registers [content] lambda as the responsible for calling
 * the Composable correspondent to [destination].
 *
 * When [destination] is navigated to, [content] will be called
 * with the correct [DestinationScope] containing the navigation
 * arguments, the back stack entry and navigators.
 */
fun <T> ManualComposableCallsBuilder.composable(
    destination: TypedDestinationSpec<T>,
    content: @Composable DestinationScope<T>.() -> Unit
) {
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
 *
 * Like [composable] but the [content] is scoped in a [AnimatedDestinationScope].
 *
 * Can only be called if you're using "io.github.raamcosta.compose-destinations:animations-core"
 * and the [destination] has a [TypedDestinationSpec.style] of [com.ramcosta.composedestinations.spec.DestinationStyle.Animated]
 * or [com.ramcosta.composedestinations.spec.DestinationStyle.Default].
 */
@ExperimentalAnimationApi
fun <T> ManualComposableCallsBuilder.animatedComposable(
    destination: TypedDestinationSpec<T>,
    content: @Composable AnimatedDestinationScope<T>.() -> Unit
) {
    validateAnimated(destination)

    add(
        lambda = DestinationLambda.Animated(content),
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
 *
 * Like [composable] but the [content] is scoped in a [BottomSheetDestinationScope].
 *
 * Can only be called if you're using "io.github.raamcosta.compose-destinations:animations-core"
 * and the [destination] has a [TypedDestinationSpec.style] of
 * [com.ramcosta.composedestinations.spec.DestinationStyle.BottomSheet]
 */
fun <T> ManualComposableCallsBuilder.bottomSheetComposable(
    destination: TypedDestinationSpec<T>,
    content: @Composable BottomSheetDestinationScope<T>.() -> Unit
) {
    validateBottomSheet(destination)

    add(
        lambda = DestinationLambda.BottomSheet(content),
        destination = destination,
    )
}

class ManualComposableCallsBuilder internal constructor(
    internal val engineType: NavHostEngine.Type,
    navGraph: NavGraphSpec
) {

    private val map: MutableMap<String, DestinationLambda<*>> = mutableMapOf()
    private val dynamicDestinationsBySingletonDestination: Map<DestinationSpec, List<DynamicDestinationSpec<*>>> =
        navGraph.allDestinations
            .filterIsInstance<DynamicDestinationSpec<*>>()
            .groupBy { it.originalDestination }

    internal fun build() = ManualComposableCalls(map)

    @SuppressLint("RestrictedApi")
    internal fun add(
        lambda: DestinationLambda<*>,
        destination: DestinationSpec,
    ) {
        map[destination.baseRoute] = lambda
        dynamicDestinationsBySingletonDestination[destination]?.forEach {
            map[it.baseRoute] = lambda
        }
    }
}

@ExperimentalAnimationApi
private fun ManualComposableCallsBuilder.validateAnimated(
    destination: DestinationSpec
) {
    if (engineType == NavHostEngine.Type.DEFAULT) {
        error("'animatedComposable' can only be called with a 'AnimatedNavHostEngine'")
    }

    if (destination.style !is DestinationStyle.Animated && destination.style !is DestinationStyle.Default) {
        error("'animatedComposable' can only be called for a destination of style 'Animated' or 'Default'")
    }
}

private fun ManualComposableCallsBuilder.validateBottomSheet(
    destination: DestinationSpec
) {
    if (engineType == NavHostEngine.Type.DEFAULT) {
        error("'bottomSheetComposable' can only be called with a 'AnimatedNavHostEngine'")
    }

    if (destination.style !is DestinationStyle.BottomSheet) {
        error("'bottomSheetComposable' can only be called for a destination of style 'BottomSheet'")
    }
}