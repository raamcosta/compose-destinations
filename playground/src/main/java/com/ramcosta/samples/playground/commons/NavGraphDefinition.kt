package com.ramcosta.samples.playground.commons

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.annotation.StartRouteArgs
import com.ramcosta.samples.playground.ui.screens.profile.ProfileScreenNavArgs
import com.ramcosta.samples.playground.ui.screens.settings.SettingsViewModel

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
 * - Type of startRouteArgs matches with nav args from start route destination
 * - There are no collisions in names of nav args (graph vs destination)
 * -
 */
@RootNavGraph
@NavGraph(
    graphArgs = ProfileNavGraph.NavArgs::class
)
annotation class ProfileNavGraph(
    val start: Boolean = false
) {
    data class NavArgs(
        override val startRouteArgs: ProfileScreenNavArgs,
        val graphArg: String
    ): StartRouteArgs<ProfileScreenNavArgs>
}

@SettingsNavGraph
@NavGraph
annotation class ProfileSettingsNavGraph(
    val start: Boolean = false
)

@ProfileSettingsNavGraph(start = true)
@Destination
@Composable
internal fun ProfileSettingsScreen(
    vm: SettingsViewModel
) {
    println("VM toggle ON? ${vm.isToggleOn}")
}
