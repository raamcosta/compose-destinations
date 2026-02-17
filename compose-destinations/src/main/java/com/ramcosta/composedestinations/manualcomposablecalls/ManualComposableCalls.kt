package com.ramcosta.composedestinations.manualcomposablecalls

import androidx.annotation.RestrictTo
import androidx.navigation.NavDeepLink
import com.ramcosta.composedestinations.spec.DeepLinksContext
import com.ramcosta.composedestinations.spec.DefaultEnginePolicy
import com.ramcosta.composedestinations.spec.DestinationStyleContext
import com.ramcosta.composedestinations.spec.GraphTransitionsContext
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.NavGraphSpec
import com.ramcosta.composedestinations.spec.Route
import com.ramcosta.composedestinations.spec.TypedDestinationSpec

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ManualComposableCalls internal constructor(
    private val map: Map<String, DestinationLambda<*>>,
    private val animations: Map<String, DestinationStyle.Animated>,
    private val deepLinks: Map<String, List<NavDeepLink>>,
    private val destinationParentRoutes: Map<String, String>,
    internal val enginePolicy: DefaultEnginePolicy,
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

    fun destinationParentGraphRoute(route: String): String? {
        return destinationParentRoutes[route]
    }

    fun resolveDestinationStyle(destination: TypedDestinationSpec<*>): DestinationStyle {
        val manualOverride = manualAnimation(destination.route)
        val policyOverride = enginePolicy.resolveDestinationStyle(
            DestinationStyleContext(
                destination = destination,
                route = destination.route,
                baseRoute = destination.baseRoute,
                parentGraphRoute = destinationParentGraphRoute(destination.route),
                defaultStyle = destination.style,
                manualStyleOverride = manualOverride,
            )
        )
        return policyOverride ?: manualOverride ?: destination.style
    }

    fun resolveGraphTransitions(navGraph: NavGraphSpec): DestinationStyle.Animated? {
        val manualOverride = manualAnimation(navGraph.route)
        return enginePolicy.resolveGraphTransitions(
            GraphTransitionsContext(
                navGraph = navGraph,
                route = navGraph.route,
                defaultTransitions = navGraph.defaultTransitions,
                manualTransitionsOverride = manualOverride,
            )
        ) ?: manualOverride ?: navGraph.defaultTransitions
    }
}

fun Route.allDeepLinks(manualComposableCalls: ManualComposableCalls?): List<NavDeepLink> {
    val manualDeepLinks = manualComposableCalls?.manualDeepLinks(route).orEmpty()
    val baseline = if (manualDeepLinks.isNotEmpty()) {
        manualDeepLinks + deepLinks
    } else {
        deepLinks
    }

    val transformed = manualComposableCalls?.enginePolicy?.transformDeepLinks(
        DeepLinksContext(
            route = this,
            generatedDeepLinks = deepLinks,
            manualDeepLinks = manualDeepLinks,
            baselineDeepLinks = baseline,
        )
    )

    return transformed ?: baseline
}
