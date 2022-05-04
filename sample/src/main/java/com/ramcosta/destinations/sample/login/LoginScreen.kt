package com.ramcosta.destinations.sample.login

import androidx.activity.compose.BackHandler
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.destinations.sample.MainViewModel
import com.ramcosta.destinations.sample.NavGraphs
import com.ramcosta.destinations.sample.core.viewmodel.activityViewModel
import com.ramcosta.destinations.sample.destinations.DirectionDestination
import com.ramcosta.destinations.sample.startAppDestination

@Destination
@Composable
fun LoginScreen(
    vm: MainViewModel = activityViewModel(),
    navigator: DestinationsNavigator
) {
    BackHandler(true) { /* We want to disable back clicks */ }

    val isLoggedIn by vm.isLoggedInFlow.collectAsState()
    val hasNavigatedUp = remember { mutableStateOf(false) }

    if (isLoggedIn && !hasNavigatedUp.value) {
        hasNavigatedUp.value = true // avoids double navigation

        if (!navigator.navigateUp()) {
            // Sometimes we are starting on LoginScreen (to avoid UI jumps)
            // In those cases, navigateUp fails, so we just navigate to the registered start destination
            navigator.navigate(NavGraphs.root.startAppDestination as DirectionDestination)
        }
    }

    Button(onClick = { vm.login() }) {
        Text("Login")
    }
}