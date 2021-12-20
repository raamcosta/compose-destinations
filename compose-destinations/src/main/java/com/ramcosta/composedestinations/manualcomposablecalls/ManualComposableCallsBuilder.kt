package com.ramcosta.composedestinations.manualcomposablecalls

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.NavHostEngine

/**
 * Registers [content] lambda as the responsible for calling
 * the Composable correspondent to [destination].
 *
 * When [destination] is navigated to, [content] will be called
 * with the correct [DestinationScope] containing the navigation
 * arguments, the back stack entry and navigators.
 */
fun <T> ManualComposableCallsBuilder.composable(
    destination: DestinationSpec<T>,
    content: @Composable DestinationScope<T>.() -> Unit
) {
    map[destination] = DestinationLambda.Normal(content)
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
 * and the [destination] has a [DestinationSpec.style] of [com.ramcosta.composedestinations.spec.DestinationStyle.Animated]
 * or [com.ramcosta.composedestinations.spec.DestinationStyle.Default].
 */
@ExperimentalAnimationApi
fun <T> ManualComposableCallsBuilder.animatedComposable(
    destination: DestinationSpec<T>,
    content: @Composable AnimatedDestinationScope<T>.() -> Unit
) {
    validateAnimated(destination)

    map[destination] = DestinationLambda.Animated(content)
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
 * and the [destination] has a [DestinationSpec.style] of
 * [com.ramcosta.composedestinations.spec.DestinationStyle.BottomSheet]
 */
fun <T> ManualComposableCallsBuilder.bottomSheetComposable(
    destination: DestinationSpec<T>,
    content: @Composable BottomSheetDestinationScope<T>.() -> Unit
) {
    validateBottomSheet(destination)

    map[destination] = DestinationLambda.BottomSheet(content)
}

class ManualComposableCallsBuilder internal constructor(
    internal val engineType: NavHostEngine.Type
) {

    internal val map: MutableMap<DestinationSpec<*>, DestinationLambda<*>> = mutableMapOf()

    internal fun build() = ManualComposableCalls(map)
}

@ExperimentalAnimationApi
private fun ManualComposableCallsBuilder.validateAnimated(
    destination: DestinationSpec<*>
) {
    if (engineType == NavHostEngine.Type.DEFAULT) {
        throw RuntimeException("'animatedComposable' can only be called with a 'AnimatedNavHostEngine'")
    }

    if (destination.style !is DestinationStyle.Animated && destination.style !is DestinationStyle.Default) {
        throw RuntimeException("'animatedComposable' can only be called for a destination of style 'Animated' or 'Default'")
    }
}

private fun ManualComposableCallsBuilder.validateBottomSheet(
    destination: DestinationSpec<*>
) {
    if (engineType == NavHostEngine.Type.DEFAULT) {
        throw RuntimeException("'bottomSheetComposable' can only be called with a 'AnimatedNavHostEngine'")
    }

    if (destination.style !is DestinationStyle.BottomSheet) {
        throw RuntimeException("'bottomSheetComposable' can only be called for a destination of style 'BottomSheet'")
    }
}