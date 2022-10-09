package com.ramcosta.samples.playground.commons

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.samples.playground.ui.screens.settings.SettingsViewModel

@RootNavGraph
@NavGraph
annotation class SettingsNavGraph(
    val start: Boolean = false
)

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