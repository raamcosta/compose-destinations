package com.ramcosta.samples.playground.commons

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.playground.core.WithDefaultValueArgs
import com.ramcosta.samples.playground.ui.screens.navgraphs.ProfileGraph
import com.ramcosta.samples.playground.ui.screens.navgraphs.ProfileSettingsGraph

@RootNavGraph
@NavGraph(
    defaultTransitions = DefaultFadingTransitions::class
)
annotation class SettingsNavGraph(
    val start: Boolean = false
)

/**
 * TODO RACOSTA:
 *
 * - Validate NavHostGraph vs normal graph with no parent (navgraphs mode) and their start arguments
 *  - NavHostGraph cannot have navargs (given by annotation not having navargs)
 *  - Normal graphs with no parent in navgraphs mode can have navargs or not
 * - Allow internal NavHostGraphs gen on navgraphs mode + use visibility of the NavGraph annotation
 */
@RootNavGraph
@NavGraph(
    navArgs = ProfileNavGraph.NavArgs::class,
    deepLinks = [
        DeepLink(uriPattern = "https://destinationssample.com/$FULL_ROUTE_PLACEHOLDER")
    ]
)
annotation class ProfileNavGraph(
    val start: Boolean = false
) {
    data class NavArgs(
        val graphArg: String,
    )
}

@ProfileNavGraph(start = true)
@NavGraph(
    navArgs = ProfileSettingsNavGraph.NavArgs::class
)
annotation class ProfileSettingsNavGraph(
    val start: Boolean = false
) {
    data class NavArgs(
        val anotherGraphArg: String
    )
}

@ProfileSettingsNavGraph(start = true)
@Destination(
    navArgs = WithDefaultValueArgs::class
)
@Composable
internal fun ProfileSettingsScreen(
//    vm: SettingsViewModel,
    args: WithDefaultValueArgs,
    navBackStackEntry: NavBackStackEntry
) = Column {
//    Text("VM toggle ON? ${vm.isToggleOn}")
    Text("$args")
//    Text("${navBackStackEntry.navArgs<ProfileNavGraph.NavArgs>()}")
    Text("${kotlin.runCatching { ProfileGraph.argsFrom(navBackStackEntry) }}")
    Text("${kotlin.runCatching { ProfileSettingsGraph.argsFrom(navBackStackEntry) } }")
}
