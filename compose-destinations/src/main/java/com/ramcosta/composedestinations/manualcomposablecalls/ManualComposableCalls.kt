package com.ramcosta.composedestinations.manualcomposablecalls

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ManualComposableCalls internal constructor(
    private val map: Map<String, DestinationLambda<*>>
) {

    operator fun get(routeId: String): DestinationLambda<*>? {
        return map[routeId]
    }
}