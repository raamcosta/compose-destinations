package com.ramcosta.composedestinations.manualcomposablecalls

import androidx.annotation.RestrictTo
import com.ramcosta.composedestinations.spec.DestinationStyle

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ManualComposableCalls internal constructor(
    private val map: Map<String, DestinationLambda<*>>,
    private val animations: MutableMap<String, DestinationStyle.Animated>
) {

    operator fun get(route: String): DestinationLambda<*>? {
        return map[route]
    }

    fun manualAnimation(route: String): DestinationStyle.Animated? {
        return animations[route]
    }
}