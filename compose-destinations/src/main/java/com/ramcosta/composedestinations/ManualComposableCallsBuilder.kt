package com.ramcosta.composedestinations

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.internal.ComposableLambda
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.NavHostEngine

/**
 * Registers [content] lambda as the responsible for calling
 * the Composable correspondent to [destination].
 *
 * When [destination] is navigated to, [content] will be called
 * with the correct [T] navigation arguments class.
 */
fun <T> ManualComposableCallsBuilder.composable(
    destination: DestinationSpec<T>,
    content: @Composable (T, NavBackStackEntry) -> Unit
) {
    map[destination] = ComposableLambdaType.NORMAL to content as ComposableLambda
}

/**
 * Registers [content] lambda as the responsible for calling
 * the Composable correspondent to [destination].
 *
 * When [destination] is navigated to, [content] will be called.
 *
 * Like [composable] but for destinations with no navigation arguments.
 */
fun ManualComposableCallsBuilder.composable(
    destination: DestinationSpec<Unit>,
    content: @Composable (NavBackStackEntry) -> Unit
) {
    map[destination] =
        ComposableLambdaType.NORMAL to @Composable { _: Unit, entry: NavBackStackEntry ->
            content(entry)
        } as ComposableLambda
}

/**
 * Registers [content] lambda as the responsible for calling
 * the Composable correspondent to [destination].
 *
 * When [destination] is navigated to, [content] will be called
 * with the correct [T] navigation arguments class.
 *
 * Like [composable] but the [content] is scoped in a [AnimatedVisibilityScope].
 *
 * Can only be called if you're using "io.github.raamcosta.compose-destinations:animations-core"
 * and the [destination] has a [DestinationSpec.style] of [com.ramcosta.composedestinations.spec.DestinationStyle.Animated]
 * or [com.ramcosta.composedestinations.spec.DestinationStyle.Default].
 */
@ExperimentalAnimationApi
fun <T> ManualComposableCallsBuilder.animatedComposable(
    destination: DestinationSpec<T>,
    content: @Composable AnimatedVisibilityScope.(T, NavBackStackEntry) -> Unit
) {
    validateAnimated(destination)

    map[destination] = ComposableLambdaType.ANIMATED to content as ComposableLambda
}

/**
 * Registers [content] lambda as the responsible for calling
 * the Composable correspondent to [destination].
 *
 * When [destination] is navigated to, [content] will be called.
 *
 * Like [animatedComposable] but for destinations with no navigation arguments.
 *
 * Can only be called if you're using "io.github.raamcosta.compose-destinations:animations-core"
 * and the [destination] has a [DestinationSpec.style] of [com.ramcosta.composedestinations.spec.DestinationStyle.Animated]
 * or [com.ramcosta.composedestinations.spec.DestinationStyle.Default].
 */
@ExperimentalAnimationApi
fun ManualComposableCallsBuilder.animatedComposable(
    destination: DestinationSpec<Unit>,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    validateAnimated(destination)

    val auxLambda: @Composable AnimatedVisibilityScope.(Unit, NavBackStackEntry) -> Unit =
        { _, entry ->
            content(entry)
        }
    map[destination] = ComposableLambdaType.ANIMATED to auxLambda as ComposableLambda
}

/**
 * Registers [content] lambda as the responsible for calling
 * the Composable correspondent to [destination].
 *
 * When [destination] is navigated to, [content] will be called
 * with the correct [T] navigation arguments class.
 *
 * Like [composable] but the [content] is scoped in a [ColumnScope].
 *
 * Can only be called if you're using "io.github.raamcosta.compose-destinations:animations-core"
 * and the [destination] has a [DestinationSpec.style] of
 * [com.ramcosta.composedestinations.spec.DestinationStyle.BottomSheet]
 */
fun <T> ManualComposableCallsBuilder.bottomSheetComposable(
    destination: DestinationSpec<T>,
    content: @Composable ColumnScope.(T, NavBackStackEntry) -> Unit
) {
    validateBottomSheet(destination)

    map[destination] = ComposableLambdaType.BOTTOM_SHEET to content as ComposableLambda
}

/**
 * Registers [content] lambda as the responsible for calling
 * the Composable correspondent to [destination].
 *
 * When [destination] is navigated to, [content] will be called.
 *
 * Like [bottomSheetComposable] but for destinations with no navigation arguments.
 *
 * Can only be called if you're using "io.github.raamcosta.compose-destinations:animations-core"
 * and the [destination] has a [DestinationSpec.style] of
 * [com.ramcosta.composedestinations.spec.DestinationStyle.BottomSheet]
 */
fun ManualComposableCallsBuilder.bottomSheetComposable(
    destination: DestinationSpec<Unit>,
    content: @Composable ColumnScope.(NavBackStackEntry) -> Unit
) {
    validateBottomSheet(destination)

    val auxLambda: @Composable ColumnScope.(Unit, NavBackStackEntry) -> Unit = { _, entry ->
        content(entry)
    }
    map[destination] = ComposableLambdaType.BOTTOM_SHEET to auxLambda as ComposableLambda
}

class ManualComposableCallsBuilder internal constructor(
    internal val engineType: NavHostEngine.Type
) {

    internal val map: MutableMap<DestinationSpec<*>, Pair<ComposableLambdaType, ComposableLambda>> =
        mutableMapOf()

    internal fun build() = ManualComposableCalls(map)
}

enum class ComposableLambdaType {
    NORMAL,
    ANIMATED,
    BOTTOM_SHEET
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