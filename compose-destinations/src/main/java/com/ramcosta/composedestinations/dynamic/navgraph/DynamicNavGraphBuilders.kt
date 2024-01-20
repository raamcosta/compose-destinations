package com.ramcosta.composedestinations.dynamic.navgraph

import androidx.navigation.NavDeepLink
import com.ramcosta.composedestinations.annotation.IncludeNavGraph
import com.ramcosta.composedestinations.annotation.InternalDestinationsApi
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.spec.TypedNavGraphSpec

@InternalDestinationsApi
class DynamicNavGraphBuilder<NAV_ARGS, START_ROUTE_NAV_ARGS>(
    val originalNavGraphSpec: TypedNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS>
) {
    var defaultTransitions: DestinationStyle.Animated? = IncludeNavGraph.Companion.NoOverride
    var additionalDeepLinks: List<NavDeepLink>? = null

    fun build(): DynamicNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS> {
        return object : DynamicNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS>, TypedNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS> by originalNavGraphSpec {
            override val originalNavGraph: TypedNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS> = originalNavGraphSpec.originalNavGraph

            override val deepLinks: List<NavDeepLink>
                get() = additionalDeepLinks.orEmpty() + originalNavGraph.deepLinks

            override val defaultTransitions: DestinationStyle.Animated?
                get() = if (this@DynamicNavGraphBuilder.defaultTransitions != IncludeNavGraph.Companion.NoOverride) {
                    this@DynamicNavGraphBuilder.defaultTransitions
                } else {
                    originalNavGraph.defaultTransitions
                }
        }
    }
}

@InternalDestinationsApi
fun <NAV_ARGS, START_ROUTE_NAV_ARGS> TypedNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS>.with(
    builder: DynamicNavGraphBuilder<NAV_ARGS, START_ROUTE_NAV_ARGS>.() -> Unit
): DynamicNavGraphSpec<NAV_ARGS, START_ROUTE_NAV_ARGS> {
    return DynamicNavGraphBuilder(this).apply { builder() }.build()
}