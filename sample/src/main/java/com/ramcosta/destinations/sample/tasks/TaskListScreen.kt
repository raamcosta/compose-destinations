package com.ramcosta.destinations.sample.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.destinations.sample.destinations.LoginScreenDestination
import com.ramcosta.destinations.sample.destinations.SettingsScreenDestination

@RootNavGraph(start = true)
@Destination
@Composable
fun TaskListScreen(
    navigator: DestinationsNavigator
) {
    Column {
        Text("TASK LIST")
        Button(onClick = { navigator.navigate(LoginScreenDestination) }) {
            Text("Login")
        }
        Button(onClick = { navigator.navigate(SettingsScreenDestination) }) {
            Text("Settings")
        }
    }
}