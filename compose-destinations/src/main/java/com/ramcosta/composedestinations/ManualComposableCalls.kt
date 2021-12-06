package com.ramcosta.composedestinations

import androidx.compose.runtime.internal.ComposableLambda
import com.ramcosta.composedestinations.spec.DestinationSpec

class ManualComposableCalls internal constructor(
    private val map: Map<DestinationSpec<*>, Pair<ComposableLambdaType, ComposableLambda>>
) {

    operator fun get(destinationSpec: DestinationSpec<*>): Pair<ComposableLambdaType, ComposableLambda>? {
        return map[destinationSpec]
    }
}