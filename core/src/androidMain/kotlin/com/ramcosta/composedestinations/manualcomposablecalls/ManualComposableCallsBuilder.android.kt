package com.ramcosta.composedestinations.manualcomposablecalls

import androidx.navigation.NavDeepLinkDslBuilder
import androidx.navigation.navDeepLink
import com.ramcosta.composedestinations.spec.Route

/**
 * Adds deep link created by [deepLinkBuilder] to this [Route] ([NavGraphSpec] or [DestinationSpec]).
 *
 * Useful when you need to create the deep link at runtime.
 */
fun ManualComposableCallsBuilder.addDeepLink(
    route: Route,
    deepLinkBuilder: NavDeepLinkDslBuilder.() -> Unit
) {
    this as ManualComposableCallsBuilderImpl
    add(route.route, navDeepLink(deepLinkBuilder))
}