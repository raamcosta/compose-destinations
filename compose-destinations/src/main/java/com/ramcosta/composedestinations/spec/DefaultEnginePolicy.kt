package com.ramcosta.composedestinations.spec

import androidx.navigation.NavDeepLink

/**
 * Policy hooks used by the default [com.ramcosta.composedestinations.DestinationsNavHost] engines
 * to customize runtime destination style resolution, graph transitions and deep links.
 */
interface DefaultEnginePolicy {

    /**
     * Return a non-null [DestinationStyle] to override style resolution for [ctx.destination].
     */
    fun resolveDestinationStyle(ctx: DestinationStyleContext): DestinationStyle? = null

    /**
     * Return non-null transitions to override graph transition resolution for [ctx.navGraph].
     */
    fun resolveGraphTransitions(ctx: GraphTransitionsContext): DestinationStyle.Animated? = null

    /**
     * Return non-null deep links to replace baseline deep links for [ctx.route].
     * Return null to keep [DeepLinksContext.baselineDeepLinks].
     */
    fun transformDeepLinks(ctx: DeepLinksContext): List<NavDeepLink>? = null

    /**
     * No-op policy that keeps existing behavior.
     */
    object None : DefaultEnginePolicy
}

/**
 * Read-only data used to resolve runtime destination style.
 */
data class DestinationStyleContext(
    val destination: DestinationSpec,
    val route: String,
    val baseRoute: String,
    val parentGraphRoute: String?,
    val defaultStyle: DestinationStyle,
    val manualStyleOverride: DestinationStyle.Animated?,
)

/**
 * Read-only data used to resolve runtime graph transitions.
 */
data class GraphTransitionsContext(
    val navGraph: NavGraphSpec,
    val route: String,
    val defaultTransitions: DestinationStyle.Animated?,
    val manualTransitionsOverride: DestinationStyle.Animated?,
)

/**
 * Read-only data used to transform runtime deep links.
 */
data class DeepLinksContext(
    val route: Route,
    val generatedDeepLinks: List<NavDeepLink>,
    val manualDeepLinks: List<NavDeepLink>,
    val baselineDeepLinks: List<NavDeepLink>,
)
