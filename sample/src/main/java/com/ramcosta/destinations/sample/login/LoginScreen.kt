package com.ramcosta.destinations.sample.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.LoginScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.startDestination
import com.ramcosta.destinations.sample.MainViewModel
import com.ramcosta.destinations.sample.core.viewmodel.activityViewModel

@Destination<RootGraph>
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
            navigator.navigate(NavGraphs.root.startDestination as DirectionDestinationSpec) {
                popUpTo(LoginScreenDestination) {
                    inclusive = true
                }
            }
        }
    }

    LoginScreenContent { vm.login() }
}

@Composable
private fun LoginScreenContent(onLoginClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("WORK IN PROGRESS")

            Spacer(Modifier.height(32.dp))

            Button(onClick = onLoginClick) {
                Text("Login")
            }
        }
    }
}