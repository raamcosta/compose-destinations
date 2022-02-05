package com.ramcosta.composedestinations.manualcomposablecalls

class ManualComposableCalls internal constructor(
    private val map: Map<String, DestinationLambda<*>>
) {

    operator fun get(routeId: String): DestinationLambda<*>? {
        return map[routeId]
    }
}