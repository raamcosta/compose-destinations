@file:OptIn(InternalDestinationsApi::class)

package com.ramcosta.composedestinations.manualcomposablecalls

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.InternalDestinationsApi
import com.ramcosta.composedestinations.dynamic.destination.DynamicDestinationSpec
import com.ramcosta.composedestinations.scope.AnimatedDestinationScope
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.NavHostEngine
import com.ramcosta.composedestinations.spec.TypedDestinationSpec
import com.ramcosta.composedestinations.utils.allDestinations

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
        error("'composable' can only be called with a 'NavHostEngine'")
    }

    if (destination.style !is DestinationStyle.Dialog) {
        error("'dialogComposable' can only be called for a destination of style 'Dialog'")
    }

    add(
        lambda = DestinationLambda.Dialog(content),
        destination = destination,
    )
}

class ManualComposableCallsBuilder internal constructor(
    @InternalDestinationsApi
    val engineType: NavHostEngine.Type,
    navGraph: NavGraphSpec
) {

    private val map: MutableMap<String, DestinationLambda<*>> = mutableMapOf()
    private val dynamicDestinationsBySingletonDestination: Map<DestinationSpec, List<DynamicDestinationSpec<*>>> =
        navGraph.allDestinations
            .filterIsInstance<DynamicDestinationSpec<*>>()
            .groupBy { it.originalDestination }

    internal fun build() = ManualComposableCalls(map)

    @InternalDestinationsApi
    @SuppressLint("RestrictedApi")
    fun add(
        lambda: DestinationLambda<*>,
        destination: DestinationSpec,
    ) {
        map[destination.baseRoute] = lambda
        dynamicDestinationsBySingletonDestination[destination]?.forEach {
            map[it.baseRoute] = lambda
        }
    }
}