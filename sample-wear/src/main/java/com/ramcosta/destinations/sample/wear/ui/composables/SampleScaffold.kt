package com.ramcosta.destinations.sample.wear.ui.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import com.ramcosta.composedestinations.spec.Route
import com.ramcosta.destinations.sample.wear.appCurrentDestinationAsState
import com.ramcosta.destinations.sample.wear.destinations.Destination
import com.ramcosta.destinations.sample.wear.shouldShowScaffoldElements
import com.ramcosta.destinations.sample.wear.startAppDestination

@Composable
fun SampleScaffold(
    startRoute: Route,
    navController: NavHostController,
    content: @Composable () -> Unit,
) {
    val destination = navController.appCurrentDestinationAsState().value
        ?: startRoute.startAppDestination

    Scaffold(
        timeText = {
            if (destination.shouldShowScaffoldElements) {
                TimeText()
            }
        },
        content = content
    )
}
