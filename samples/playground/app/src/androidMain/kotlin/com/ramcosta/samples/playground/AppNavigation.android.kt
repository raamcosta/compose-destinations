package com.ramcosta.samples.playground

import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder
import com.ramcosta.composedestinations.manualcomposablecalls.addDeepLink
import com.ramcosta.samples.playground.ui.screens.destinations.TestScreenDestination

actual fun ManualComposableCallsBuilder.addPlatformDependentDeepLinks() {
    addDeepLink(TestScreenDestination) { uriPattern = "runtimeschema://${TestScreenDestination.route}" }
}