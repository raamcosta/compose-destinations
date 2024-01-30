package com.ramcosta.destinations.sample.settings

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph

@Destination<RootNavGraph>
@Composable
fun SettingsScreen() {
    Text("Settings")
}