package com.ramcosta.destinations.sample.ui.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import com.ramcosta.destinations.sample.core.viewmodel.viewModel
import com.ramcosta.destinations.sample.destinations.*
import com.ramcosta.destinations.sample.tasks.presentation.details.StepDetailsViewModel
import com.ramcosta.destinations.sample.tasks.presentation.details.TaskDetailsViewModel

@Composable
fun TopBar(
    destination: Destination,
    navBackStackEntry: NavBackStackEntry?
) {
    TopAppBar {
        Spacer(Modifier.width(8.dp))

        Text(
            text = destination.topBarTitle(navBackStackEntry),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun Destination.topBarTitle(navBackStackEntry: NavBackStackEntry?): String {
    return when (this) {
        TaskScreenDestination -> {
            // Here you can also call another Composable on another file like TaskScreenTopBar
            // 👇 access the same viewmodel instance the screen is using, by passing the back stack entry
            val task = navBackStackEntry?.let {
                viewModel<TaskDetailsViewModel>(navBackStackEntry).task.collectAsState().value
            }
            task?.title ?: ""
        }
        StepScreenDestination -> {
            // Here you can also call another Composable on another file like StepScreenTopBar
            // 👇 access the same viewmodel instance the screen is using, by passing the back stack entry
            val viewModel = navBackStackEntry?.let { viewModel<StepDetailsViewModel>(navBackStackEntry) }
            val step = viewModel?.let {
                viewModel.step.collectAsState().value
            }
            val task = viewModel?.let {
                viewModel.task.collectAsState().value
            }
            "${task?.title ?: ""}: ${step?.title ?: ""}"
        }
        AddStepDialogDestination,
        AddTaskDialogDestination,
        AccountScreenDestination,
        LoginScreenDestination,
        SettingsScreenDestination,
        TaskListScreenDestination -> javaClass.simpleName.removeSuffix("Destination")
    }
}