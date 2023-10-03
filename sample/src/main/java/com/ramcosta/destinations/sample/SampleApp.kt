package com.ramcosta.destinations.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.destinations.sample.core.viewmodel.activityViewModel
import com.ramcosta.composedestinations.generated.destinations.LoginScreenDestination
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.destinations.sample.ui.composables.BottomBar
import com.ramcosta.destinations.sample.ui.composables.SampleScaffold
import com.ramcosta.destinations.sample.ui.composables.TopBar

@Composable
fun SampleApp() {
    val navController = rememberNavController()

    val vm = activityViewModel<MainViewModel>()
    // 👇 this avoids a jump in the UI that would happen if we relied only on ShowLoginWhenLoggedOut
    val startRoute = if (!vm.isLoggedIn) LoginScreenDestination else NavGraphs.root.startRoute

    SampleScaffold(
        navController = navController,
        startRoute = startRoute,
        topBar = { dest, backStackEntry ->
            if (dest.shouldShowScaffoldElements) {
                TopBar(dest, backStackEntry)
            }
        },
        bottomBar = {
            if (it.shouldShowScaffoldElements) {
                BottomBar(navController)
            }
        }
    ) {
        DestinationsNavHost(
            navController = navController,
            navGraph = NavGraphs.root,
            modifier = Modifier.padding(it).fillMaxSize(),
            startRoute = startRoute
        )

        // Has to be called after calling DestinationsNavHost because only
        // then does NavController have a graph associated that we need for
        // `appCurrentDestinationAsState` method
        ShowLoginWhenLoggedOut(vm, navController)
    }
}

private val DestinationSpec.shouldShowScaffoldElements get() = this !is LoginScreenDestination

@Composable
private fun ShowLoginWhenLoggedOut(
    vm: MainViewModel,
    navController: NavHostController
) {
    val currentDestination by navController.currentDestinationAsState()
    val isLoggedIn by vm.isLoggedInFlow.collectAsState()

    if (!isLoggedIn && currentDestination != LoginScreenDestination) {
        // everytime destination changes or logged in state we check
        // if we have to show Login screen and navigate to it if so
        navController.navigate(LoginScreenDestination) {
            launchSingleTop = true
        }
    }
}