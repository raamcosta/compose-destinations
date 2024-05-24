package com.ramcosta.composedestinations.manualcomposablecalls

import androidx.annotation.RestrictTo
import androidx.navigation.NavDeepLink
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.Route

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ManualComposableCalls internal constructor(
    private val map: Map<String, DestinationLambda<*>>,
    private val animations: Map<String, DestinationStyle.Animated>,
    private val deepLinks: Map<String, List<NavDeepLink>>
) {

    operator fun get(route: String): DestinationLambda<*>? {
        return map[route]
    }

    fun manualAnimation(route: String): DestinationStyle.Animated? {
        return animations[route]
    }

    fun manualDeepLinks(route: String): List<NavDeepLink>? {
        return deepLinks[route]
    }
}

fun Route.allDeepLinks(manualComposableCalls: ManualComposableCalls?): List<NavDeepLink> {
    val manualDeepLinks = manualComposableCalls?.manualDeepLinks(route)
    return if (manualDeepLinks != null) {
        manualDeepLinks + deepLinks
    } else {
        deepLinks
    }
}