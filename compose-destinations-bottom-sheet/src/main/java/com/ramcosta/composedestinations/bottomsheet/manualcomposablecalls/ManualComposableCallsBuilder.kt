package com.ramcosta.composedestinations.bottomsheet.manualcomposablecalls

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.bottomsheet.spec.DestinationStyleBottomSheet
import com.ramcosta.composedestinations.manualcomposablecalls.DestinationLambda
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilderImpl
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.scope.BottomSheetDestinationScope
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.NavHostEngine
import com.ramcosta.composedestinations.spec.TypedDestinationSpec

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
 * Can only be called if you're using "io.github.raamcosta.compose-destinations:bottom-sheet"
 * and the [destination] has a [DestinationSpec.style] of
 * [com.ramcosta.composedestinations.bottomsheet.spec.DestinationStyleBottomSheet]
 */
fun <T> ManualComposableCallsBuilder.bottomSheetComposable(
    destination: TypedDestinationSpec<T>,
    content: @Composable BottomSheetDestinationScope<T>.() -> Unit
) {
    validateBottomSheet(destination)
    this as ManualComposableCallsBuilderImpl
    add(
        lambda = DestinationLambda.BottomSheet(content),
        destination = destination,
    )
}

private fun ManualComposableCallsBuilder.validateBottomSheet(
    destination: DestinationSpec
) {
    this as ManualComposableCallsBuilderImpl
    if (engineType != NavHostEngine.Type.DEFAULT) {
        error("'bottomSheetComposable' can only be called with a 'NavHostEngine'")
    }

    if (destination.style !is DestinationStyleBottomSheet) {
        error("'bottomSheetComposable' can only be called for a destination of style 'BottomSheet'")
    }
}