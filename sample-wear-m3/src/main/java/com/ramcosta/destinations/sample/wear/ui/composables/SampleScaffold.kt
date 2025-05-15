package com.ramcosta.destinations.sample.wear.ui.composables

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.TimeText
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.startDestination
import com.ramcosta.destinations.sample.wear.shouldShowScaffoldElements

@Composable
fun SampleScaffold(
    navController: NavHostController,
    content: @Composable (BoxScope.() -> Unit),
) {
    val destination = navController.currentDestinationAsState().value
        ?: NavGraphs.root.startDestination

    AppScaffold(
        timeText = {
            if (destination.shouldShowScaffoldElements) {
                TimeText()
            }
        },
        content = content
    )
}
