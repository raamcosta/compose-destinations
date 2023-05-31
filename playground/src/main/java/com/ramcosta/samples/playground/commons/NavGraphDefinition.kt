package com.ramcosta.samples.playground.commons

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.animations.defaults.DefaultFadingTransitions
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.samples.playground.ui.screens.settings.SettingsViewModel
import kotlinx.serialization.Serializable

@RootNavGraph
@NavGraph(
    defaultTransitions = DefaultFadingTransitions::class
)
annotation class SettingsNavGraph(
    val start: Boolean = false
)

@SettingsNavGraph
@NavGraph
annotation class ProfileSettingsNavGraph(
    val start: Boolean = false
)

internal data class InternalNavArgs(
    val cena: ArrayList<InternalDataClass>?
)

@Serializable
internal data class InternalDataClass(
    val cena: String
)

@ProfileSettingsNavGraph(start = true)
@Destination(
    navArgsDelegate = InternalNavArgs::class
)
@Composable
internal fun ProfileSettingsScreen(
    vm: SettingsViewModel
) {
    println("VM toggle ON? ${vm.isToggleOn}")
}
